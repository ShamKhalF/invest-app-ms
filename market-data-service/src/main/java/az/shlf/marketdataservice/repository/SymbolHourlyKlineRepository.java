package az.shlf.marketdataservice.repository;

import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SymbolHourlyKlineRepository extends JpaRepository<SymbolHourlyKlineEntity, Long>,
        JpaSpecificationExecutor<SymbolHourlyKlineEntity> {

   @Query("SELECT MAX(k.closeTime) FROM SymbolHourlyKlineEntity k WHERE k.symbol = :symbol")
   Long findMaxCloseTimeBySymbol(@Param("symbol") String symbol);
}