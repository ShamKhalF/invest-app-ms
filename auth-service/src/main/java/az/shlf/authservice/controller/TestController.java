package az.shlf.authservice.controller;

import az.shlf.authservice.service.MsPermissionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class TestController {

   private final MsPermissionsService service;

   @GetMapping("/test")
   public CompletableFuture<Map<String, Map<String, List<String>>>> test() {
      return service.getGroupedPermissions();
   }

}
