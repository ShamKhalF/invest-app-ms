package az.shlf.authservice.service.impl;

import az.shlf.authservice.dto.permission.PermissionRequest;
import az.shlf.authservice.dto.permission.PermissionResponse;
import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.entity.Permission;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.constants.SuccessCode;
import az.shlf.authservice.exception.custom.CustomException;
import az.shlf.authservice.mapper.PermissionMapper;
import az.shlf.authservice.repository.PermissionRepository;
import az.shlf.authservice.service.MsPermissionsService;
import az.shlf.authservice.service.PermissionService;
import az.shlf.authservice.util.PageableCheckUtil;
import az.shlf.authservice.util.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

   private final PermissionRepository permissionRepository;
   private final PermissionMapper permissionMapper;
   private final MsPermissionsService msPermissionsService;
   private final ResponseMessageHelper messageHelper;


   @Override
   public VoidResponse addPermission(PermissionRequest request) {
      Permission permission = permissionMapper.toEntity(request);
      permissionRepository.save(permission);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.PERMISSION_ADDED);
   }

   @Override
   public VoidResponse updatePermission(Long id, PermissionRequest request) {
      Permission permission = permissionRepository.findById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.PERMISSION_NOT_FOUND, id));
      permissionMapper.updateEntity(permission, request);
      permissionRepository.save(permission);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.PERMISSION_UPDATED);
   }

   @Override
   @Transactional
   public VoidResponse deletePermission(Long id) {
      permissionRepository.deleteFromRolePermissionByPermissionId(id);
      permissionRepository.deleteById(id);
      msPermissionsService.getGroupedPermissions();
      return messageHelper.getVoidResponse(SuccessCode.PERMISSION_DELETED);
   }

   @Override
   public Page<PermissionResponse> getPermissions(int page, int size, String search) {
      Pageable pageable = PageableCheckUtil.getPageable(page, size);
      Page<Permission> permissions = permissionRepository.findAllByNameContainsIgnoreCase(search, pageable);
      return permissions.map(permissionMapper::toDto);
   }

   @Override
   public Set<Permission> getPermissionsByIds(Set<Long> ids) {
      return permissionRepository.findByIdIn(ids);
   }

}
