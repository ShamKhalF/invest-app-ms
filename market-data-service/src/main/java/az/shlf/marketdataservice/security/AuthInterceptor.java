package az.shlf.marketdataservice.security;

import az.shlf.marketdataservice.exception.constants.ErrorCodes;
import az.shlf.marketdataservice.exception.custom.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

import static az.shlf.marketdataservice.security.AuthKeys.X_USERNAME;
import static az.shlf.marketdataservice.security.AuthKeys.X_USER_PERMISSIONS;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

   @Override
   public boolean preHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler) throws Exception {

      if (!(handler instanceof HandlerMethod handlerMethod)) {
         return true;
      }

      RequirePermission requirePermission = handlerMethod.getMethodAnnotation(RequirePermission.class);

      // Əgər RequirePermission annotasiyası yoxdursa, public endpoint kimi qəbul edib icazə veririk
      if (requirePermission == null) {
         return true;
      }

      // Yalnız annotasiya olan halda header-lər yoxlanılır
      String userId = request.getHeader(X_USERNAME.getKey());
      if (userId == null || userId.isEmpty()) {
         throw new CustomException(ErrorCodes.UNAUTHORIZED);
      }

      String permissionsStr = request.getHeader(X_USER_PERMISSIONS.getKey());
      List<String> permissions = permissionsStr != null && !permissionsStr.isEmpty() ? Arrays.asList(permissionsStr.split(",")) : List.of();

      String[] requiredPerms = requirePermission.value();
      boolean hasPermission = Arrays.stream(requiredPerms).anyMatch(permissions::contains);

      if (!hasPermission) {
         throw new CustomException(ErrorCodes.ACCESS_DENIED);
      }

      return true;
   }
}