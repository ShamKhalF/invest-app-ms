package az.shlf.walletservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "asset"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

//   @Column(name = "user_id", nullable = false)
//   private Long userId;

   @Column(name = "username", nullable = false)
   private String username;

   @Column(name = "asset", nullable = false, length = 10)
   private String asset; // Məs: USDT, BTC

   @Column(name = "available_balance", precision = 36, scale = 18, nullable = false)
   private BigDecimal availableBalance;

   @Column(name = "locked_balance", precision = 36, scale = 18, nullable = false)
   private BigDecimal lockedBalance;

   @Column(name = "created_at", updatable = false)
   @CreationTimestamp
   private LocalDateTime createdAt;

   @Column(name = "updated_at")
   @UpdateTimestamp
   private LocalDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      if (this.availableBalance == null)
         this.availableBalance = BigDecimal.ZERO;
      if (this.lockedBalance == null)
         this.lockedBalance = BigDecimal.ZERO;
   }

}