package az.shlf.walletservice.entity;

import az.shlf.walletservice.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "wallet_id", nullable = false)
   private Long walletId;

   @Enumerated(EnumType.STRING)
   @Column(name = "type", nullable = false, length = 20)
   private TransactionType type;

   @Column(name = "amount", precision = 36, scale = 18, nullable = false)
   private BigDecimal amount;

   @Column(name = "reference_id")
   private String referenceId; // Binance txId və ya Order ID

   @Column(name = "created_at", updatable = false)
   @CreationTimestamp
   private LocalDateTime createdAt;

}
