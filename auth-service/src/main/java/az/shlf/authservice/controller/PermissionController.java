package az.shlf.authservice.controller;

import az.shlf.authservice.dto.permission.PermissionRequest;
import az.shlf.authservice.dto.permission.PermissionResponse;
import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.security.RequirePermission;
import az.shlf.authservice.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

   private final PermissionService permissionService;

   @PostMapping
   @RequirePermission("permission:add")
   public ResponseEntity<VoidResponse> addPermission(@RequestBody @Valid PermissionRequest request) {
      return new ResponseEntity<>(permissionService.addPermission(request), HttpStatus.CREATED);
   }

   @PutMapping("/{id}")
   @RequirePermission("permission:update")
   public ResponseEntity<VoidResponse> updatePermission(@PathVariable Long id, @RequestBody @Valid PermissionRequest request) {
      return ResponseEntity.ok(permissionService.updatePermission(id, request));
   }

   @DeleteMapping("/{id}")
   @RequirePermission("permission:delete")
   public ResponseEntity<VoidResponse> deletePermission(@PathVariable Long id) {
      return ResponseEntity.ok(permissionService.deletePermission(id));
   }

   @GetMapping
   @RequirePermission("permission:read")
   public Page<PermissionResponse> getPermissions(
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size,
           @RequestParam(required = false, defaultValue = "") String search) {
      return permissionService.getPermissions(page, size, search);
   }
}