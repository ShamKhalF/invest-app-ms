package az.shlf.authservice.mapper;

import az.shlf.authservice.dto.role.RoleRequest;
import az.shlf.authservice.dto.role.RoleResponse;
import az.shlf.authservice.entity.Role;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PermissionMapper.class)
public interface RoleMapper {

   Role toEntity(RoleRequest request);

   RoleResponse toDto(Role entity);

   @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   void updateEntity(@MappingTarget Role role, RoleRequest request);

}
