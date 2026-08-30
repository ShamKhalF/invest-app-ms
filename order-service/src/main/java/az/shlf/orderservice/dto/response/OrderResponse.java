package az.shlf.orderservice.dto.response;

import az.shlf.orderservice.entity.enums.OrderSide;
import az.shlf.orderservice.entity.enums.OrderStatus;
import az.shlf.orderservice.entity.enums.OrderType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
   private Long id;
   private String symbol;
   private OrderSide side;
   private OrderType type;
   private BigDecimal price;
   private BigDecimal quantity;
   private BigDecimal executedQuantity;
   private OrderStatus status;
   private LocalDateTime createdAt;
}