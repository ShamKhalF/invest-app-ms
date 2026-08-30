package az.shlf.marketdataservice.controller;

import az.shlf.marketdataservice.security.RequirePermission;
import az.shlf.marketdataservice.service.AdminStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/stream")
@RequiredArgsConstructor
public class AdminStreamController {

   private final AdminStateService adminStateService;

   @PostMapping("/stop")
   @RequirePermission("stream:stop")
   public ResponseEntity<String> stopAllStreams() {
      adminStateService.stopAllStreams();
      return ResponseEntity.ok("Bütün WebSocket əlaqələri kəsildi və sistem donduruldu.");
   }

   @PostMapping("/start")
   @RequirePermission("stream:start")
   public ResponseEntity<String> startAllStreams() {
      adminStateService.startAllStreams();
      return ResponseEntity.ok("Sistem aktivləşdirildi və gözləyən koinlər üçün axın bərpa olundu.");
   }

}
