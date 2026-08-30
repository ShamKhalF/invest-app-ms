package az.shlf.walletservice.controller;

import az.shlf.walletservice.dto.request.DepositRequest;
import az.shlf.walletservice.dto.response.TransactionResponse;
import az.shlf.walletservice.dto.response.VoidResponse;
import az.shlf.walletservice.dto.response.WalletBalanceResponse;
import az.shlf.walletservice.security.RequirePermission;
import az.shlf.walletservice.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

   private final WalletService walletService;

   @PostMapping("/deposit")
   @RequirePermission({"wallet:write"})
   public ResponseEntity<VoidResponse> deposit(
           @RequestBody DepositRequest request,
           HttpServletRequest servletRequest) {

      String username = servletRequest.getHeader("X-Username");
      return ResponseEntity.ok(walletService.depositLocalBalance(username, request));
   }

   @GetMapping("/balance")
   @RequirePermission({"wallet:read"})
   public ResponseEntity<Page<WalletBalanceResponse>> getBalances(
           HttpServletRequest servletRequest,
           Pageable pageable) {

      String username = servletRequest.getHeader("X-Username");
      Page<WalletBalanceResponse> balances = walletService.getBalances(username, pageable);
      return ResponseEntity.ok(balances);
   }

   @GetMapping("/transactions")
   @RequirePermission({"wallet:read"})
   public ResponseEntity<Page<TransactionResponse>> getTransactions(
           HttpServletRequest servletRequest,
           Pageable pageable) {

      String username = servletRequest.getHeader("X-Username");
      Page<TransactionResponse> transactions = walletService.getTransactions(username, pageable);
      return ResponseEntity.ok(transactions);
   }
}