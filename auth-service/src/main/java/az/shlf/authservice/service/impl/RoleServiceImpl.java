package az.shlf.authservice.service.impl;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.role.RoleRequest;
import az.shlf.authservice.dto.role.RoleResponse;
import az.shlf.authservice.entity.Permission;
import az.shlf.authservice.entity.Role;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.constants.SuccessCode;
import az.shlf.authservice.exception.custom.CustomException;
import az.shlf.authservice.mapper.RoleMapper;
import az.shlf.authservice.repository.RoleRepository;
import az.shlf.authservice.service.MsPermissionsService;
import az.shlf.authservice.service.PermissionService;
import az.shlf.authservice.service.RoleService;
import az.shlf.authservice.util.PageableCheckUtil;
import az.shlf.authservice.util.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

   private final RoleRepository roleRepository;
   private final RoleMapper roleMapper;
   private final PermissionService permissionService;
   private final MsPermissionsService msPermissionsService;
   private final ResponseMessageHelper messageHelper;


   @Override
   public VoidResponse addRole(RoleRequest request) {
      Role role = roleMapper.toEntity(request);
      role.setPermissions(permissionService.getPermissionsByIds(request.getPermissionIds()));
      roleRepository.save(role);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.ROLE_ADDED);
   }

   @Override
   @Transactional
   public VoidResponse updateRole(Long id, RoleRequest request) {
      Role role = roleRepository.findRoleWithPermissionsById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.ROLE_NOT_FOUND, id));

      roleMapper.updateEntity(role, request);
      if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
         Set<Permission> permissions = permissionService.getPermissionsByIds(request.getPermissionIds());
         role.getPermissions().addAll(permissions);
      }
      roleRepository.save(role);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.ROLE_UPDATED);
   }

   @Override
   @Transactional
   public VoidResponse deleteRole(Long id) {
      roleRepository.deleteFromRolePermissionByRoleId(id);
      roleRepository.deleteById(id);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.ROLE_DELETED);
   }

   @Override
   @Transactional
   public VoidResponse removePermissions(Long roleId, List<Long> permissionIds) {
      roleRepository.deletePermissionsFromRole(roleId, permissionIds);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.PERMISSIONS_REMOVED_FROM_ROLE);
   }

   @Override
   public RoleResponse getRoleById(Long id) {
      Role role = roleRepository.findRoleWithPermissionsById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.ROLE_NOT_FOUND, id));
      return roleMapper.toDto(role);
   }

   @Override
   public Page<RoleResponse> getRoles(int page, int size, String search) {
      Pageable pageable = PageableCheckUtil.getPageable(page, size);
      Page<Role> roles = roleRepository.findAllByNameContainsIgnoreCase(search, pageable);
      return roles.map(this::toDto);
   }


   private RoleResponse toDto(Role role) {
      RoleResponse roleResponse = new RoleResponse();
      roleResponse.setId(role.getId());
      roleResponse.setName(role.getName());
      return roleResponse;
   }

}
