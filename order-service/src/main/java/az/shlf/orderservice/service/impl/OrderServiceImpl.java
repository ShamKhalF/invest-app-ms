package az.shlf.orderservice.service.impl;

import az.shlf.orderservice.dto.BinanceExecutionReport;
import az.shlf.orderservice.dto.response.OrderResponse;
import az.shlf.orderservice.dto.response.VoidResponse;
import az.shlf.orderservice.exception.constants.ErrorCodes;
import az.shlf.orderservice.exception.constants.SuccessCode;
import az.shlf.orderservice.exception.custom.CustomException;
import az.shlf.orderservice.publisher.SymbolWatchPublisher;
import az.shlf.orderservice.service.OrderService;
import az.shlf.orderservice.client.BinanceOrderClient;
import az.shlf.orderservice.dto.request.OrderCreateRequest;
import az.shlf.orderservice.entity.Order;
import az.shlf.orderservice.entity.enums.OrderSide;
import az.shlf.orderservice.entity.enums.OrderStatus;
import az.shlf.orderservice.service.grpc.WalletGrpcClientService;
import az.shlf.orderservice.repository.OrderRepository;
import az.shlf.orderservice.util.HmacSignatureUtil;
import az.shlf.orderservice.util.PageableCheckUtil;
import az.shlf.orderservice.util.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

   private final OrderRepository orderRepository;
   private final WalletGrpcClientService walletGrpcClient;
   private final BinanceOrderClient binanceOrderClient;
   private final ResponseMessageHelper messageHelper;
   private final SymbolWatchPublisher symbolWatchPublisher;

   @Value("${binance.api.key}")
   private String apiKey;

   @Value("${binance.api.secret}")
   private String apiSecret;

   @Override
   @Transactional
   public OrderResponse createOrder(String username, String idempotencyKey, OrderCreateRequest request) {

      Optional<Order> existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey);
      if (existingOrder.isPresent()) {
         return mapToResponse(existingOrder.get());
      }

      if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
         throw new CustomException(ErrorCodes.INVALID_AMOUNT);
      }

      // 1. Binance-dan cari real bazar qiymətini çəkirik
      Map<String, Object> tickerResponse;
      try {
         tickerResponse = binanceOrderClient.getTickerPrice(request.getSymbol());
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.INTERNAL_SERVER_ERROR, "Bazar qiymətini çəkmək mümkün olmadı");
      }
      BigDecimal realMarketPrice = new BigDecimal(tickerResponse.get("price").toString());

      // 2. Büdcəni (request.getPrice()) real bazar qiymətinə bölərək kəmiyyəti (quantity) hesablayırıq.
      // LOT_SIZE xətası almamaq üçün kəmiyyət 5 onluq dəqiqliyə qədər yuvarlaqlaşdırılır (aşağıya doğru).
      BigDecimal calculatedQuantity = request.getPrice().divide(realMarketPrice, 5, RoundingMode.DOWN);

      if (calculatedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
         throw new CustomException(ErrorCodes.INVALID_AMOUNT, "Hesablanmış kəmiyyət çox kiçikdir");
      }

      String baseAsset = request.getSymbol().replace("USDT", "");
      String quoteAsset = "USDT";

      // Rezerv ediləcək vəsait: Alış zamanı istifadəçinin göndərdiyi büdcə (USDT), Satış zamanı hesablanmış kəmiyyət (BTC)
      String reserveAsset = request.getSide() == OrderSide.BUY ? quoteAsset : baseAsset;
      BigDecimal reserveAmount = request.getSide() == OrderSide.BUY ? request.getPrice() : calculatedQuantity;

      Order order = Order.builder()
              .idempotencyKey(idempotencyKey)
              .username(username)
              .symbol(request.getSymbol())
              .side(request.getSide())
              .type(request.getType())
              .price(realMarketPrice) // Bazaya real bazar qiyməti yazılır
              .quantity(calculatedQuantity) // Bazaya hesablanmış kəmiyyət yazılır
              .status(OrderStatus.PENDING)
              .build();

      order = orderRepository.save(order);

      try {
         walletGrpcClient.reserveBalance(username, reserveAsset, reserveAmount, order.getId().toString());
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.BALANCE_RESERVE_FAILED);
      }

      try {
         long timestamp = System.currentTimeMillis();

         // Tipə görə parametrlərin dinamik təyini
         boolean isLimitOrder = "LIMIT".equalsIgnoreCase(request.getType().name());
         String timeInForce = isLimitOrder ? "GTC" : null;
         BigDecimal binancePrice = isLimitOrder ? realMarketPrice : null;

         // Query string-in dinamik formalaşdırılması
         StringBuilder queryBuilder = new StringBuilder();
         queryBuilder.append("symbol=").append(request.getSymbol())
                 .append("&side=").append(request.getSide().name())
                 .append("&type=").append(request.getType().name());

         if (timeInForce != null) {
            queryBuilder.append("&timeInForce=").append(timeInForce);
         }

         queryBuilder.append("&quantity=").append(calculatedQuantity.toPlainString());

         if (binancePrice != null) {
            queryBuilder.append("&price=").append(binancePrice.toPlainString());
         }

         queryBuilder.append("&timestamp=").append(timestamp);

         String queryString = queryBuilder.toString();
         String signature = HmacSignatureUtil.generateSignature(queryString, apiSecret);

         // Binance API-yə müraciət
         Object binanceResponse = binanceOrderClient.createOrder(
                 apiKey,
                 request.getSymbol(),
                 request.getSide().name(),
                 request.getType().name(),
                 timeInForce,
                 calculatedQuantity,
                 binancePrice,
                 timestamp,
                 signature
         );

         Map<String, Object> responseMap = (Map<String, Object>) binanceResponse;
         String binanceOrderId = responseMap.get("orderId").toString();
         String binanceStatus = responseMap.get("status").toString();

         OrderStatus mappedStatus = mapBinanceStatusToLocal(binanceStatus);
         order.setBinanceOrderId(binanceOrderId);
         order.setStatus(mappedStatus);

         // Sifariş dərhal icra olunubsa (MARKET order), vəsaiti commit et
         if (mappedStatus == OrderStatus.FILLED) {
            BigDecimal executedQty = new BigDecimal(responseMap.get("executedQty").toString());
            BigDecimal cummulativeQuoteQty = new BigDecimal(responseMap.get("cummulativeQuoteQty").toString());

            String soldAsset;
            BigDecimal soldAmount;
            String boughtAsset;
            BigDecimal boughtAmount;

            if (request.getSide() == OrderSide.BUY) {
               soldAsset = quoteAsset;
               soldAmount = cummulativeQuoteQty;
               boughtAsset = baseAsset;
               boughtAmount = executedQty;
            } else {
               soldAsset = baseAsset;
               soldAmount = executedQty;
               boughtAsset = quoteAsset;
               boughtAmount = cummulativeQuoteQty;
            }

            walletGrpcClient.commitBalance(username, soldAsset, soldAmount, boughtAsset, boughtAmount, order.getId().toString());
            order.setExecutedQuantity(executedQty);
         }

         orderRepository.save(order);

         symbolWatchPublisher.publishSymbol(request.getSymbol());

         return mapToResponse(order);

      } catch (Exception e) {
         log.error("Binance API Error: {}", e.getMessage());
         if (e instanceof feign.FeignException) {
            log.error("Binance Error Response: {}", ((feign.FeignException) e).contentUTF8());
         }

         order.setStatus(OrderStatus.REJECTED);
         orderRepository.save(order);
         try {
            walletGrpcClient.releaseBalance(username, reserveAsset, reserveAmount, order.getId().toString());
         } catch (Exception releaseEx) {
            throw new CustomException(ErrorCodes.INTERNAL_SERVER_ERROR,
                    "Binance xətası və vəsaitin azad edilməsi uğursuz oldu: " + releaseEx.getMessage());
         }
         throw new CustomException(ErrorCodes.BINANCE_EXECUTION_FAILED);
      }
   }

   @Override
   @Transactional
   public VoidResponse cancelOrder(String username, Long orderId) {
      Order order = orderRepository.findByIdAndUsername(orderId, username)
              .orElseThrow(() -> new CustomException(ErrorCodes.ORDER_NOT_FOUND));

      if (order.getStatus() != OrderStatus.OPEN) {
         throw new CustomException(ErrorCodes.ORDER_NOT_OPEN);
      }

      // Binance Testnet REST API-yə DELETE sorğusu
      try {
         long timestamp = System.currentTimeMillis();
         String queryString = String.format("symbol=%s&orderId=%s&timestamp=%d",
                 order.getSymbol(),
                 order.getBinanceOrderId(),
                 timestamp
         );

         String signature = HmacSignatureUtil.generateSignature(queryString, apiSecret);

         binanceOrderClient.cancelOrder(
                 apiKey,
                 order.getSymbol(),
                 order.getBinanceOrderId(),
                 timestamp,
                 signature
         );
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.BINANCE_CANCEL_FAILED);
      }

      // Lokal bazada statusun yenilənməsi
      order.setStatus(OrderStatus.CANCELED);
      orderRepository.save(order);

      // Wallet servisə ReleaseBalance sorğusu
      String baseAsset = order.getSymbol().replace("USDT", "");
      String quoteAsset = "USDT";

      String reserveAsset = order.getSide() == OrderSide.BUY ? quoteAsset : baseAsset;
      BigDecimal reserveAmount = order.getSide() == OrderSide.BUY
              ? order.getPrice().multiply(order.getQuantity())
              : order.getQuantity();

      try {
         walletGrpcClient.releaseBalance(username, reserveAsset, reserveAmount, order.getId().toString());
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.BALANCE_RELEASE_FAILED);
      }

      return messageHelper.getVoidResponse(SuccessCode.ORDER_CANCELED);
   }

   @Override
   @Transactional
   public void handleExecutionReport(BinanceExecutionReport report) {
      if (!"executionReport".equals(report.getEventType()) || !"FILLED".equals(report.getOrderStatus())) {
         return;
      }

      String binanceOrderId = String.valueOf(report.getOrderId());

      Order order = orderRepository.findByBinanceOrderId(binanceOrderId)
              .orElse(null);

      // Əgər order tapılmadısa və ya artıq FILLED statusundadırsa, prosesi dayandır. (İdempotency)
      if (order == null || order.getStatus() == OrderStatus.FILLED) {
         return;
      }

      // Hesablaşma parametrlərinin təyini
      String baseAsset = order.getSymbol().replace("USDT", "");
      String quoteAsset = "USDT";

      String soldAsset;
      BigDecimal soldAmount;
      String boughtAsset;
      BigDecimal boughtAmount;

      if ("BUY".equalsIgnoreCase(report.getSide())) {
         soldAsset = quoteAsset;
         soldAmount = report.getCumulativeQuoteTransactedQuantity();
         boughtAsset = baseAsset;
         boughtAmount = report.getCumulativeFilledQuantity();
      } else {
         soldAsset = baseAsset;
         soldAmount = report.getCumulativeFilledQuantity();
         boughtAsset = quoteAsset;
         boughtAmount = report.getCumulativeQuoteTransactedQuantity();
      }

      // Wallet servisinə CommitBalance göndərilməsi
      try {
         walletGrpcClient.commitBalance(
                 order.getUsername(),
                 soldAsset,
                 soldAmount,
                 boughtAsset,
                 boughtAmount,
                 order.getId().toString()
         );
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.INTERNAL_SERVER_ERROR, "CommitBalance xətası: " + e.getMessage());
      }

      // Lokal bazada statusun yenilənməsi
      order.setStatus(OrderStatus.FILLED);
      order.setExecutedQuantity(report.getCumulativeFilledQuantity());
      orderRepository.save(order);
   }


   @Override
   @Transactional
   public void syncOrderStatus(Order order, Map<String, Object> binanceData) {
      String statusStr = (String) binanceData.get("status");
      OrderStatus currentBinanceStatus = mapBinanceStatusToLocal(statusStr);

      if (order.getStatus() == currentBinanceStatus) {
         return;
      }

      if (currentBinanceStatus == OrderStatus.FILLED) {
         BigDecimal executedQty = new BigDecimal(binanceData.get("executedQty").toString());
         BigDecimal cummulativeQuoteQty = new BigDecimal(binanceData.get("cummulativeQuoteQty").toString());
         String side = (String) binanceData.get("side");

         String baseAsset = order.getSymbol().replace("USDT", "");
         String quoteAsset = "USDT";

         String soldAsset;
         BigDecimal soldAmount;
         String boughtAsset;
         BigDecimal boughtAmount;

         if ("BUY".equalsIgnoreCase(side)) {
            soldAsset = quoteAsset;
            soldAmount = cummulativeQuoteQty;
            boughtAsset = baseAsset;
            boughtAmount = executedQty;
         } else {
            soldAsset = baseAsset;
            soldAmount = executedQty;
            boughtAsset = quoteAsset;
            boughtAmount = cummulativeQuoteQty;
         }

         walletGrpcClient.commitBalance(order.getUsername(), soldAsset, soldAmount, boughtAsset, boughtAmount, order.getId().toString());
         order.setStatus(OrderStatus.FILLED);
         order.setExecutedQuantity(executedQty);
         orderRepository.save(order);

      } else if (currentBinanceStatus == OrderStatus.CANCELED || currentBinanceStatus == OrderStatus.REJECTED) {

         String baseAsset = order.getSymbol().replace("USDT", "");
         String quoteAsset = "USDT";
         String reserveAsset = order.getSide() == OrderSide.BUY ? quoteAsset : baseAsset;

         BigDecimal reserveAmount = order.getSide() == OrderSide.BUY
                 ? order.getPrice().multiply(order.getQuantity())
                 : order.getQuantity();

         walletGrpcClient.releaseBalance(order.getUsername(), reserveAsset, reserveAmount, order.getId().toString());

         order.setStatus(currentBinanceStatus);
         orderRepository.save(order);
      }
   }

   @Override
   @Transactional(readOnly = true)
   public Page<OrderResponse> getOrderHistory(String username, String symbol, OrderStatus status,
                                              LocalDateTime startDate, LocalDateTime endDate,
                                              int page, int size) {
      Specification<Order> spec = (root, query, cb) -> {
         List<Predicate> predicates = new ArrayList<>();

         predicates.add(cb.equal(root.get("username"), username));

         if (symbol != null && !symbol.isBlank()) {
            predicates.add(cb.equal(root.get("symbol"), symbol));
         }
         if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
         }
         if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
         }
         if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
         }

         return cb.and(predicates.toArray(new Predicate[0]));
      };
      Pageable pageable = PageableCheckUtil.getPageable(page, size);
      return orderRepository.findAll(spec, pageable).map(this::mapToResponse);
   }

   private OrderStatus mapBinanceStatusToLocal(String binanceStatus) {
      return switch (binanceStatus) {
         case "NEW" -> OrderStatus.OPEN;
         case "PARTIALLY_FILLED" -> OrderStatus.PARTIALLY_FILLED;
         case "FILLED" -> OrderStatus.FILLED;
         case "CANCELED", "EXPIRED", "EXPIRED_IN_MATCH", "PENDING_CANCEL" -> OrderStatus.CANCELED;
         case "REJECTED" -> OrderStatus.REJECTED;
         default -> OrderStatus.PENDING;
      };
   }

   private OrderResponse mapToResponse(Order order) {
      return OrderResponse.builder()
              .id(order.getId())
              .symbol(order.getSymbol())
              .side(order.getSide())
              .type(order.getType())
              .price(order.getPrice())
              .quantity(order.getQuantity())
              .executedQuantity(order.getExecutedQuantity())
              .status(order.getStatus())
              .createdAt(order.getCreatedAt())
              .build();
   }


}
