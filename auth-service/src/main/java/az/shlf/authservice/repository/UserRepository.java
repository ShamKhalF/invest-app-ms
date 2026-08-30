package az.shlf.authservice.repository;

import az.shlf.authservice.contants.entity.Status;
import az.shlf.authservice.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
   Optional<User> findByUsername(String username);

   Optional<User> findByEmail(String email);

   boolean existsByUsername(String username);

   boolean existsByEmail(String email);

   @EntityGraph(attributePaths = {"roles", "roles.permissions"})
   Optional<User> findUserWithRolesAndPermissionsById(Long id);

   Optional<User> findByUsernameOrEmailAndStatusIn(String username, String email, List<Status> status);

   @Modifying
   @Query("DELETE FROM User u WHERE u.status = :status AND u.createdAt < :thresholdDate")
   void deleteByStatusAndCreatedAtBefore(@Param("status") Status status, @Param("thresholdDate") LocalDateTime thresholdDate);

}
