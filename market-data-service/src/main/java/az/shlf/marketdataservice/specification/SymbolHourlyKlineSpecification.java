package az.shlf.marketdataservice.specification;

import az.shlf.marketdataservice.dto.KlineSearchDto;
import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SymbolHourlyKlineSpecification {

   public static Specification<SymbolHourlyKlineEntity> getFilter(KlineSearchDto dto) {
      return (root, query, cb) -> {
         List<Predicate> predicates = new ArrayList<>();

         if (dto.getSymbol() != null && !dto.getSymbol().trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("symbol")), "%" + dto.getSymbol().toLowerCase() + "%"));
         }

         if (dto.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("closePrice"), dto.getMinPrice()));
         }

         if (dto.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("closePrice"), dto.getMaxPrice()));
         }

         if (dto.getStartTime() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("openTime"), dto.getStartTime()));
         }

         if (dto.getEndTime() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("closeTime"), dto.getEndTime()));
         }

         return cb.and(predicates.toArray(new Predicate[0]));
      };
   }
}