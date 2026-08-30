package az.shlf.authservice.dto.user;

import az.shlf.authservice.contants.entity.Status;
import az.shlf.authservice.util.PageableCheckUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserRequest {

   private String name;
   private String surname;
   private String username;
   private String email;
   private String password;
   private String phone;
   private Status status;

   private String roleName;
   private String permissionName;

   private Integer page;
   private Integer size;

   public Pageable getPageable() {
      return PageableCheckUtil.getPageable(page, size);
   }

}
