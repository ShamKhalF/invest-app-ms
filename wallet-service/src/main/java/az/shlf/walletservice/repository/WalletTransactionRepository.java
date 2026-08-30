package az.shlf.walletservice.repository;

import az.shlf.walletservice.entity.WalletTransaction;
import az.shlf.walletservice.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
//   List<WalletTransaction> findAllByWalletIdInOrderByCreatedAtDesc(List<Long> walletIds);
   Page<WalletTransaction> findAllByWalletIdInOrderByCreatedAtDesc(List<Long> walletIds, Pageable pageable);
   boolean existsByReferenceIdAndType(String referenceId, TransactionType type);
}
