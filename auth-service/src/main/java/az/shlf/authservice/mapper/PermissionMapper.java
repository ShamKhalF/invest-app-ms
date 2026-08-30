package az.shlf.authservice.mapper;

import az.shlf.authservice.dto.permission.PermissionRequest;
import az.shlf.authservice.dto.permission.PermissionResponse;
import az.shlf.authservice.entity.Permission;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

   Permission toEntity(PermissionRequest request);

   PermissionResponse toDto(Permission entity);

   @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   void updateEntity(@MappingTarget Permission permission, PermissionRequest request);

}
