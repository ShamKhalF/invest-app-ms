package az.shlf.authservice.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableCheckUtil {

   public static Pageable getPageable(Integer page, Integer size) {
      int resolvedPage = (page == null || page < 0)
              ? 0
              : page;

      int resolvedSize = (size == null || size < 1)
              ? 10
              : size;

      if (resolvedSize > 100) {
         resolvedSize = 100;
      }


//import org.springframework.data.domain.Sort;
//import org.springframework.data.domain.Sort.Order;
//      Sort sort = Sort.by(
//              Order.asc("id"),
//              Order.asc("username"),
//              Order.desc("createTime"),
//              Order.desc("updateTime")
//      );

      return PageRequest.of(resolvedPage, resolvedSize);
   }


}
