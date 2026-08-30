package az.shlf.walletservice.service;

import az.shlf.walletservice.dto.request.DepositRequest;
import az.shlf.walletservice.dto.response.TransactionResponse;
import az.shlf.walletservice.dto.response.VoidResponse;
import az.shlf.walletservice.dto.response.WalletBalanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
   void createDefaultWallets(String username);
   VoidResponse depositLocalBalance(String username, DepositRequest request);
   Page<WalletBalanceResponse> getBalances(String username, Pageable pageable);
   Page<TransactionResponse> getTransactions(String username, Pageable pageable);


   // ---------------------------------------- gRPC methods ---------------------------------------- \\

   void reserveBalance(String username, String asset, BigDecimal amount, String orderId);
   void releaseBalance(String username, String asset, BigDecimal amount, String orderId);
   void commitBalance(String username, String soldAsset, BigDecimal soldAmount,
                      String boughtAsset, BigDecimal boughtAmount, String orderId);

}
