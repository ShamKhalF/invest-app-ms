package az.shlf.telegramaibot.entity;

import az.shlf.telegramaibot.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "telegram_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class TelegramUser {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "telegram_id", unique = true)
   private Long telegramId;

   @Column(name = "username")
   private String username;

   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
   @ToString.Exclude
   private Set<ChatSession> sessions;

   @Enumerated(EnumType.STRING)
   @Column(nullable = false)
   @Builder.Default
   private UserStatus status = UserStatus.INACTIVE;

   @CreationTimestamp
   private LocalDateTime createdAt;

   @Override
   public final boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
      Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
      if (thisEffectiveClass != oEffectiveClass) return false;
      TelegramUser that = (TelegramUser) o;
      return getId() != null && Objects.equals(getId(), that.getId());
   }

   @Override
   public final int hashCode() {
      return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
   }
}