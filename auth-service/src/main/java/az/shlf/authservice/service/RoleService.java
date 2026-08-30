package az.shlf.authservice.service;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.role.RoleRequest;
import az.shlf.authservice.dto.role.RoleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {
   VoidResponse addRole(RoleRequest request);

   VoidResponse updateRole(Long id, RoleRequest request);

   VoidResponse deleteRole(Long id);

   VoidResponse removePermissions(Long roleId, List<Long> permissionIds);

   RoleResponse getRoleById(Long id);

   Page<RoleResponse> getRoles(int page, int size, String search);

}
