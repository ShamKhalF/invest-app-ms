package az.shlf.authservice.service.impl;

import az.shlf.authservice.entity.Permission;
import az.shlf.authservice.entity.Role;
import az.shlf.authservice.repository.RoleRepository;
import az.shlf.authservice.service.MsPermissionsService;
import az.shlf.authservice.service.RedisService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static az.shlf.authservice.contants.redis.RedisKeys.GROUPED_PERMISSIONS_KEY;
import static az.shlf.authservice.contants.redis.RedisKeys.PERMISSIONS_CHANNEL;

@Service
@RequiredArgsConstructor
public class MsPermissionsServiceImpl implements MsPermissionsService {

   private final RoleRepository roleRepository;
   private final RedisService redisService;

   @Async
   @Override
   @PostConstruct
   public CompletableFuture<Map<String, Map<String, List<String>>>> getGroupedPermissions() {
      List<Role> roles = roleRepository.findAllWithPermissions();

      Map<String, Map<String, List<String>>> groupedPermissions = new HashMap<>();

      for (Role role : roles) {
         for (Permission permission : role.getPermissions()) {
            String serviceName = permission.getService();
            groupedPermissions
                    .computeIfAbsent(serviceName, k -> new HashMap<>())
                    .computeIfAbsent(role.getName(), k -> new ArrayList<>())
                    .add(permission.getName());
         }
      }

//      System.out.println(groupedPermissions);

      redisService.set(GROUPED_PERMISSIONS_KEY.getKey(), groupedPermissions);
      redisService.publish(PERMISSIONS_CHANNEL.getKey(), "REFRESH_PERMISSIONS");

      return CompletableFuture.completedFuture(groupedPermissions);
   }
}