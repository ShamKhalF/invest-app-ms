package az.shlf.telegramaibot.controller;

import az.shlf.telegramaibot.dto.response.BotLinkResponseDto;
import az.shlf.telegramaibot.dto.response.CommonResponse;
import az.shlf.telegramaibot.security.RequirePermission;
import az.shlf.telegramaibot.service.BotAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram/bot")
@RequiredArgsConstructor
public class BotAuthController {

   private final BotAuthService botAuthService;

   @GetMapping("/link")
   @RequirePermission({"telegram:read"})
   public ResponseEntity<CommonResponse<BotLinkResponseDto>> getBotLink(HttpServletRequest request) {
      String username = request.getHeader("X-Username");
      return botAuthService.generateBotLink(username);
   }

   @PostMapping("/user/delete-request")
   @RequirePermission({"telegram:delete"})
   public ResponseEntity<CommonResponse<Void>> requestDeleteTelegramUser(HttpServletRequest request) {
      String username = request.getHeader("X-Username");
      return botAuthService.initiateDeleteTelegramUser(username);
   }

   @DeleteMapping("/user/delete-confirm")
   @RequirePermission({"telegram:delete"})
   public ResponseEntity<CommonResponse<Void>> confirmDeleteTelegramUser(HttpServletRequest request,
                                                                         @RequestParam("otp") String otp) {
      String username = request.getHeader("X-Username");
      return botAuthService.verifyOtpAndDeleteUser(username, otp);
   }

}