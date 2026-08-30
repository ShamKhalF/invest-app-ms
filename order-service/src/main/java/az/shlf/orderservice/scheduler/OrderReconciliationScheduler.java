package az.shlf.orderservice.scheduler;

import az.shlf.orderservice.client.BinanceOrderClient;
import az.shlf.orderservice.entity.Order;
import az.shlf.orderservice.entity.enums.OrderSide;
import az.shlf.orderservice.entity.enums.OrderStatus;
import az.shlf.orderservice.repository.OrderRepository;
import az.shlf.orderservice.service.OrderService;
import az.shlf.orderservice.service.grpc.WalletGrpcClientService;
import az.shlf.orderservice.util.HmacSignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReconciliationScheduler {

   private final OrderRepository orderRepository;
   private final OrderService orderService;
   private final BinanceOrderClient binanceOrderClient;
   private final WalletGrpcClientService walletGrpcClient;

   @Value("${binance.api.key}")
   private String apiKey;

   @Value("${binance.api.secret}")
   private String apiSecret;

   // Hər 5 dəqiqədən bir icra edilir
   @Scheduled(fixedDelay = 300000)
   public void reconcileOpenOrders() {
      List<OrderStatus> openStatuses = Arrays.asList(OrderStatus.PENDING, OrderStatus.OPEN, OrderStatus.PARTIALLY_FILLED);
      List<Order> openOrders = orderRepository.findAllByStatusIn(openStatuses);

      if (openOrders.isEmpty()) {
         return;
      }

      for (Order order : openOrders) {
         if (order.getBinanceOrderId() == null) {
            continue;
         }

         try {
            long timestamp = System.currentTimeMillis();
            String queryString = String.format("symbol=%s&orderId=%s&timestamp=%d",
                    order.getSymbol(),
                    order.getBinanceOrderId(),
                    timestamp);

            String signature = HmacSignatureUtil.generateSignature(queryString, apiSecret);

            Map<String, Object> binanceData = binanceOrderClient.getOrder(
                    apiKey,
                    order.getSymbol(),
                    order.getBinanceOrderId(),
                    timestamp,
                    signature);

            orderService.syncOrderStatus(order, binanceData);

         } catch (Exception e) {
            // Əgər xəta Binance-da sifarişin tapılmamasıdırsa (adətən 400 status və -2013 kodu qaytarır)
            if (e.getMessage().contains("-2013") || e.getMessage().contains("Order does not exist")) {

               String baseAsset = order.getSymbol().replace("USDT", "");
               String quoteAsset = "USDT";
               String reserveAsset = order.getSide() == OrderSide.BUY ? quoteAsset : baseAsset;
               BigDecimal reserveAmount = order.getSide() == OrderSide.BUY
                       ? order.getPrice().multiply(order.getQuantity())
                       : order.getQuantity();

               // Kompensasiya: Vəsaiti azad et və statusu CANCELED (və ya REJECTED) olaraq yenilə
               try {
                  walletGrpcClient.releaseBalance(order.getUsername(), reserveAsset, reserveAmount, order.getId().toString());
                  order.setStatus(OrderStatus.REJECTED);
                  orderRepository.save(order);
               } catch (Exception ex) {
                  log.error("Sinxronizasiya kompensasiyası uğursuz oldu (ID: {}): {}", order.getId(), ex.getMessage());
               }
            } else {
               log.error("Order sinxronizasiya xətası (ID: {}): {}", order.getId(), e.getMessage());
            }
         }
      }
   }
}