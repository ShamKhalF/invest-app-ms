package az.shlf.walletservice.service.impl;

import az.shlf.walletservice.dto.request.DepositRequest;
import az.shlf.walletservice.dto.response.TransactionResponse;
import az.shlf.walletservice.dto.response.VoidResponse;
import az.shlf.walletservice.dto.response.WalletBalanceResponse;
import az.shlf.walletservice.entity.Wallet;
import az.shlf.walletservice.entity.WalletTransaction;
import az.shlf.walletservice.entity.enums.TransactionType;
import az.shlf.walletservice.exception.constants.ErrorCodes;
import az.shlf.walletservice.exception.constants.SuccessCode;
import az.shlf.walletservice.exception.custom.CustomException;
import az.shlf.walletservice.repository.WalletRepository;
import az.shlf.walletservice.repository.WalletTransactionRepository;
import az.shlf.walletservice.service.WalletService;
import az.shlf.walletservice.util.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

   private final WalletRepository walletRepository;
   private final WalletTransactionRepository walletTransactionRepository;
   private final ResponseMessageHelper messageHelper;

   @Override
   @Transactional
   public void createDefaultWallets(String username) {
      if (walletRepository.existsByUsername(username)) {
         log.warn("İstifadəçi üçün cüzdanlar artıq mövcuddur. username: {}", username);
         return;
      }

      List<Wallet> defaultWallets = List.of(
              createEmptyWallet(username, "USDT"),
              createEmptyWallet(username, "BTC")
      );

      walletRepository.saveAll(defaultWallets);
      log.info("İstifadəçi üçün default cüzdanlar yaradıldı. username: {}", username);
   }

   @Override
   @Transactional
   public VoidResponse depositLocalBalance(String username, DepositRequest request) {
      if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
         throw new CustomException(ErrorCodes.INVALID_AMOUNT);
      }

      Wallet wallet = walletRepository.findByUsernameAndAsset(username, request.getAsset())
              .orElseGet(() -> walletRepository.save(
                      Wallet.builder()
                              .username(username)
                              .asset(request.getAsset())
                              .availableBalance(BigDecimal.ZERO)
                              .lockedBalance(BigDecimal.ZERO)
                              .build()
              ));

      wallet.setAvailableBalance(wallet.getAvailableBalance().add(request.getAmount()));
      walletRepository.save(wallet);

      WalletTransaction transaction = WalletTransaction.builder()
              .walletId(wallet.getId())
              .type(TransactionType.DEPOSIT)
              .amount(request.getAmount())
              .referenceId("LOCAL_DEP_" + UUID.randomUUID().toString().substring(0, 8))
              .build();

      walletTransactionRepository.save(transaction);
      log.info("Lokal balans artırıldı. username: {}, asset: {}, amount: {}", username, request.getAsset(), request.getAmount());
      return messageHelper.getVoidResponse(SuccessCode.DEPOSIT_SUCCESS);
   }

   @Override
   public Page<WalletBalanceResponse> getBalances(String username, Pageable pageable) {
      Page<Wallet> wallets = walletRepository.findAllByUsername(username, pageable);
      return wallets.map(w -> WalletBalanceResponse.builder()
              .asset(w.getAsset())
              .availableBalance(w.getAvailableBalance())
              .lockedBalance(w.getLockedBalance())
              .build());
   }

   @Override
   public Page<TransactionResponse> getTransactions(String username, Pageable pageable) {
      List<Wallet> wallets = walletRepository.findAllByUsername(username);

      if (wallets.isEmpty()) {
         return Page.empty(pageable);
      }

      List<Long> walletIds = wallets.stream().map(Wallet::getId).collect(Collectors.toList());
      Map<Long, String> walletAssetMap = wallets.stream()
              .collect(Collectors.toMap(Wallet::getId, Wallet::getAsset));

      Page<WalletTransaction> transactions = walletTransactionRepository.findAllByWalletIdInOrderByCreatedAtDesc(walletIds, pageable);

      return transactions.map(t -> TransactionResponse.builder()
              .asset(walletAssetMap.get(t.getWalletId()))
              .type(t.getType().name())
              .amount(t.getAmount())
              .referenceId(t.getReferenceId())
              .createdAt(t.getCreatedAt())
              .build());
   }

   private Wallet createEmptyWallet(String username, String asset) {
      return Wallet.builder()
              .username(username)
              .asset(asset)
              .availableBalance(BigDecimal.ZERO)
              .lockedBalance(BigDecimal.ZERO)
              .build();
   }



// ---------------------------------------- gRPC methods ---------------------------------------- \\

   @Override
   @Transactional
   public void reserveBalance(String username, String asset, BigDecimal amount, String orderId) {
      if (walletTransactionRepository.existsByReferenceIdAndType(orderId, TransactionType.ORDER_RESERVE)) {
         return;
      }

      Wallet wallet = walletRepository.findByUsernameAndAsset(username, asset)
              .orElseThrow(() -> new CustomException(ErrorCodes.WALLET_NOT_FOUND));

      if (wallet.getAvailableBalance().compareTo(amount) < 0) {
         throw new CustomException(ErrorCodes.INSUFFICIENT_AVAILABLE_BALANCE);
      }

      wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
      wallet.setLockedBalance(wallet.getLockedBalance().add(amount));
      walletRepository.save(wallet);

      saveTransaction(wallet.getId(), TransactionType.ORDER_RESERVE, amount, orderId);
   }

   @Override
   @Transactional
   public void releaseBalance(String username, String asset, BigDecimal amount, String orderId) {
      if (walletTransactionRepository.existsByReferenceIdAndType(orderId, TransactionType.ORDER_RELEASE)) {
         return;
      }

      Wallet wallet = walletRepository.findByUsernameAndAsset(username, asset)
              .orElseThrow(() -> new CustomException(ErrorCodes.WALLET_NOT_FOUND));

      if (wallet.getLockedBalance().compareTo(amount) < 0) {
         throw new CustomException(ErrorCodes.INSUFFICIENT_LOCKED_BALANCE);
      }

      wallet.setLockedBalance(wallet.getLockedBalance().subtract(amount));
      wallet.setAvailableBalance(wallet.getAvailableBalance().add(amount));
      walletRepository.save(wallet);

      saveTransaction(wallet.getId(), TransactionType.ORDER_RELEASE, amount, orderId);
   }

   @Override
   @Transactional
   public void commitBalance(String username, String soldAsset, BigDecimal soldAmount,
                             String boughtAsset, BigDecimal boughtAmount, String orderId) {

      if (walletTransactionRepository.existsByReferenceIdAndType(orderId, TransactionType.ORDER_EXECUTE)) {
         return;
      }

      Wallet soldWallet = walletRepository.findByUsernameAndAsset(username, soldAsset)
              .orElseThrow(() -> new CustomException(ErrorCodes.WALLET_NOT_FOUND));

      if (soldWallet.getLockedBalance().compareTo(soldAmount) < 0) {
         throw new CustomException(ErrorCodes.INSUFFICIENT_LOCKED_BALANCE);
      }

      soldWallet.setLockedBalance(soldWallet.getLockedBalance().subtract(soldAmount));
      walletRepository.save(soldWallet);
      saveTransaction(soldWallet.getId(), TransactionType.ORDER_EXECUTE, soldAmount.negate(), orderId);

      Wallet boughtWallet = walletRepository.findByUsernameAndAsset(username, boughtAsset)
              .orElseGet(() -> walletRepository.save(
                      Wallet.builder()
                              .username(username)
                              .asset(boughtAsset)
                              .availableBalance(BigDecimal.ZERO)
                              .lockedBalance(BigDecimal.ZERO)
                              .build()
              ));

      boughtWallet.setAvailableBalance(boughtWallet.getAvailableBalance().add(boughtAmount));
      walletRepository.save(boughtWallet);
      saveTransaction(boughtWallet.getId(), TransactionType.ORDER_EXECUTE, boughtAmount, orderId);
   }

   private void saveTransaction(Long walletId, TransactionType type, BigDecimal amount, String referenceId) {
      WalletTransaction transaction = WalletTransaction.builder()
              .walletId(walletId)
              .type(type)
              .amount(amount)
              .referenceId(referenceId)
              .build();
      walletTransactionRepository.save(transaction);
   }


}