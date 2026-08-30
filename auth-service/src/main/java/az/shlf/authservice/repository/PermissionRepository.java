package az.shlf.authservice.repository;

import az.shlf.authservice.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

   @Modifying
   @Query(value = "DELETE FROM role_permission WHERE permission_id = :permissionId", nativeQuery = true)
   void deleteFromRolePermissionByPermissionId(@Param("permissionId") Long permissionId);

   Page<Permission> findAllByNameContainsIgnoreCase(String name, Pageable pageable);

   Set<Permission> findByIdIn(Set<Long> ids);
}
