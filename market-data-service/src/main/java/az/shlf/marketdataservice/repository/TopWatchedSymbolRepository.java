package az.shlf.marketdataservice.repository;

import az.shlf.marketdataservice.entity.TopWatchedSymbolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopWatchedSymbolRepository extends JpaRepository<TopWatchedSymbolEntity, String>,
        JpaSpecificationExecutor<TopWatchedSymbolEntity> {

   List<TopWatchedSymbolEntity> findTop20ByOrderByWatchCountDesc();
}