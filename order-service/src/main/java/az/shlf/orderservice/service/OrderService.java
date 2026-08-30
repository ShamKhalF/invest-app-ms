package az.shlf.orderservice.service;

import az.shlf.orderservice.dto.BinanceExecutionReport;
import az.shlf.orderservice.dto.request.OrderCreateRequest;
import az.shlf.orderservice.dto.response.OrderResponse;
import az.shlf.orderservice.dto.response.VoidResponse;
import az.shlf.orderservice.entity.Order;
import az.shlf.orderservice.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

public interface OrderService {
   OrderResponse createOrder(String username, String idempotencyKey, OrderCreateRequest request);

   VoidResponse cancelOrder(String username, Long orderId);

   void handleExecutionReport(BinanceExecutionReport report);

   void syncOrderStatus(Order order, Map<String, Object> binanceData);

   Page<OrderResponse> getOrderHistory(String username, String symbol, OrderStatus status,
                                       LocalDateTime startDate, LocalDateTime endDate,
                                       int page, int size);
}
