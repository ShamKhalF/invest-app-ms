package az.shlf.marketdataservice.specification;

import az.shlf.marketdataservice.dto.TopSymbolSearchDto;
import az.shlf.marketdataservice.entity.TopWatchedSymbolEntity;
import org.springframework.data.jpa.domain.Specification;

public class TopWatchedSymbolSpecification {

   public static Specification<TopWatchedSymbolEntity> getFilter(TopSymbolSearchDto dto) {
      return (root, query, cb) -> {
         if (dto.getKeyword() == null || dto.getKeyword().trim().isEmpty()) {
            return cb.conjunction();
         }

         String pattern = "%" + dto.getKeyword().toLowerCase() + "%";
         return cb.or(
                 cb.like(cb.lower(root.get("symbol")), pattern),
                 cb.like(cb.lower(root.get("name")), pattern)
         );
      };
   }
}