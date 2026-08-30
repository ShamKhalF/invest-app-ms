package az.shlf.authservice.mapper;

import az.shlf.authservice.dto.user.*;
import az.shlf.authservice.entity.User;
import az.shlf.authservice.util.NormalizePhoneUtil;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        uses = RoleMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

   @Mapping(target = "phone", source = "phone", qualifiedByName = "normalizePhone")
   User toEntityRegister(RegisterUserRequest request);

   @Mapping(target = "phone", source = "phone", qualifiedByName = "normalizePhone")
   User toEntityAdd(AddUserRequest request);

   @Mapping(target = "phone", source = "phone", qualifiedByName = "normalizePhone")
   @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
   void updateEntity(@MappingTarget User user, UpdateUserRequest request);

   UserResponse toDto(User user);

   UserResponseAll toDtoAll(User user);

   @Named("normalizePhone")
   default String normalizePhone(String phone) {
      return NormalizePhoneUtil.normalize(phone);
   }

}