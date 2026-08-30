package az.shlf.orderservice.dto.request;

import az.shlf.orderservice.entity.enums.OrderSide;
import az.shlf.orderservice.entity.enums.OrderType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderCreateRequest {
   private String symbol;
   private OrderSide side;
   private OrderType type;
   private BigDecimal quantity;
   private BigDecimal price;
}