package az.shlf.authservice.repository;

import az.shlf.authservice.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

   @Modifying
   @Query(value = "DELETE FROM role_permission WHERE role_id = :roleId",
           nativeQuery = true)
   void deleteFromRolePermissionByRoleId(@Param("roleId") Long roleId);

   @Modifying
   @Query(value = "DELETE FROM role_permission WHERE role_id = :roleId AND permission_id IN :permissionIds",
           nativeQuery = true)
   void deletePermissionsFromRole(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

   //   @EntityGraph(attributePaths = {"permissions"})
   Page<Role> findAllByNameContainsIgnoreCase(String name, Pageable pageable);

   @EntityGraph(attributePaths = {"permissions"})
   Optional<Role> findRoleWithPermissionsById(Long id);

   @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permissions")
   List<Role> findAllWithPermissions();

   Optional<Role> findByName(String name);
}
