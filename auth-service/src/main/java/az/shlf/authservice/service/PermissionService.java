package az.shlf.authservice.service;

import az.shlf.authservice.dto.permission.PermissionRequest;
import az.shlf.authservice.dto.permission.PermissionResponse;
import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.entity.Permission;
import org.springframework.data.domain.Page;

import java.util.Set;

public interface PermissionService {
   VoidResponse addPermission(PermissionRequest request);

   VoidResponse updatePermission(Long id, PermissionRequest request);

   VoidResponse deletePermission(Long id);

   Page<PermissionResponse> getPermissions(int page, int size, String search);

   Set<Permission> getPermissionsByIds(Set<Long> ids);
}
