package az.shlf.walletservice.repository;

import az.shlf.walletservice.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
   boolean existsByUsername(String username);

   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<Wallet> findByUsernameAndAsset(String username, String asset);

   List<Wallet> findAllByUsername(String username);
   Page<Wallet> findAllByUsername(String username, Pageable pageable);
}
