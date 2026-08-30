package az.shlf.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Permission {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private String name;

   private String service;

   @ToString.Exclude
   @ManyToMany(mappedBy = "permissions")
   private Set<Role> roles = new LinkedHashSet<>();


   @Override
   public final boolean equals(Object o) {
      if (this == o) return true;
      if (o == null) return false;
      Class<?> oEffectiveClass = o instanceof HibernateProxy
              ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
              : o.getClass();
      Class<?> thisEffectiveClass = this instanceof HibernateProxy
              ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
              : this.getClass();
      if (thisEffectiveClass != oEffectiveClass) return false;
      Permission that = (Permission) o;
      return getId() != null && Objects.equals(getId(), that.getId());
   }

   @Override
   public final int hashCode() {
      return this instanceof HibernateProxy
              ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
              : getClass().hashCode();
   }
}
