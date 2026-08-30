package az.shlf.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Role {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private String name;

   @ToString.Exclude
   @ManyToMany(mappedBy = "roles")
   private Set<User> users = new LinkedHashSet<>();

   @ToString.Exclude
   @ManyToMany(fetch = FetchType.LAZY)
   @JoinTable(name = "role_permission",
           joinColumns = @JoinColumn(name = "role_id"),
           inverseJoinColumns = @JoinColumn(name = "permission_id"))
   private Set<Permission> permissions = new LinkedHashSet<>();


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
      Role role = (Role) o;
      return getId() != null && Objects.equals(getId(), role.getId());
   }

   @Override
   public final int hashCode() {
      return this instanceof HibernateProxy
              ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
              : getClass().hashCode();
   }

}
