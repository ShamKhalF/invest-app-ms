package az.shlf.orderservice.entity;

import az.shlf.orderservice.entity.enums.OrderSide;
import az.shlf.orderservice.entity.enums.OrderStatus;
import az.shlf.orderservice.entity.enums.OrderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "username", nullable = false)
   private String username;

   @Column(name = "symbol", nullable = false, length = 20)
   private String symbol; // Məs: BTCUSDT

   @Enumerated(EnumType.STRING)
   @Column(name = "side", nullable = false, length = 10)
   private OrderSide side;

   @Enumerated(EnumType.STRING)
   @Column(name = "type", nullable = false, length = 10)
   private OrderType type;

   @Column(name = "price", precision = 36, scale = 18)
   private BigDecimal price; // Market order üçün null ola bilər

   @Column(name = "quantity", precision = 36, scale = 18, nullable = false)
   private BigDecimal quantity;

   @Column(name = "executed_quantity", precision = 36, scale = 18)
   private BigDecimal executedQuantity; // Qismən icra (Partially filled) olan halları izləmək üçün

   @Enumerated(EnumType.STRING)
   @Column(name = "status", nullable = false, length = 20)
   private OrderStatus status;

   @Column(name = "binance_order_id")
   private String binanceOrderId; // Binance tərəfindən qaytarılan ID

   @Column(name = "idempotency_key", unique = true, nullable = false, updatable = false)
   private String idempotencyKey;
   
   @Column(name = "created_at", updatable = false)
   @CreationTimestamp
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   @UpdateTimestamp
   private LocalDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      if (this.executedQuantity == null)
         this.executedQuantity = BigDecimal.ZERO;
   }

}
