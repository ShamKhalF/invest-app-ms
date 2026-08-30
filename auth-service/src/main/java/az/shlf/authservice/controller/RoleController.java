package az.shlf.authservice.controller;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.role.RoleRequest;
import az.shlf.authservice.dto.role.RoleResponse;
import az.shlf.authservice.security.RequirePermission;
import az.shlf.authservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

   private final RoleService roleService;

   @PostMapping
   @RequirePermission("role:add")
   public ResponseEntity<VoidResponse> addRole(@RequestBody @Valid RoleRequest request) {
      return new ResponseEntity<>(roleService.addRole(request), HttpStatus.CREATED);
   }

   @PutMapping("/{id}")
   @RequirePermission("role:update")
   public ResponseEntity<VoidResponse> updateRole(@PathVariable Long id, @RequestBody @Valid RoleRequest request) {
      return ResponseEntity.ok(roleService.updateRole(id, request));
   }

   @DeleteMapping("/{id}")
   @RequirePermission("role:delete")
   public ResponseEntity<VoidResponse> deleteRole(@PathVariable Long id) {
      return ResponseEntity.ok(roleService.deleteRole(id));
   }

   @DeleteMapping("/{roleId}/permissions")
   @RequirePermission("role:remove_permissions")
   public ResponseEntity<VoidResponse> removePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
      return ResponseEntity.ok(roleService.removePermissions(roleId, permissionIds));
   }

   @GetMapping("/{id}")
   @RequirePermission("role:read")
   public RoleResponse getRoleById(@PathVariable Long id) {
      return roleService.getRoleById(id);
   }

   @GetMapping
   @RequirePermission("role:read")
   public Page<RoleResponse> getRoles(
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size,
           @RequestParam(required = false, defaultValue = "") String search) {
      return roleService.getRoles(page, size, search);
   }
}