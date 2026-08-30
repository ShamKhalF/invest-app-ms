package az.shlf.authservice.specifications;

import az.shlf.authservice.dto.user.SearchUserRequest;
import az.shlf.authservice.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


import static az.shlf.authservice.contants.fields.SpecificationFields.*;

@Component
public class UserSpecification {

   public Specification<User> search(SearchUserRequest request) {
      return (root, query, criteriaBuilder) -> {
         List<Predicate> predicates = new ArrayList<>();

         if (request.getName() != null && !request.getName().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(NAME.getValue())), "%" + request.getName().toLowerCase() + "%"));
         }
         if (request.getSurname() != null && !request.getSurname().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(SURNAME.getValue())), "%" + request.getSurname().toLowerCase() + "%"));
         }
         if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(USERNAME.getValue())), "%" + request.getUsername().toLowerCase() + "%"));
         }
         if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(EMAIL.getValue())), "%" + request.getEmail().toLowerCase() + "%"));
         }
         if (request.getStatus() != null) {
            predicates.add(criteriaBuilder.equal(root.get(STATUS.getValue()), request.getStatus()));
         }

         return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
      };
   }

}
