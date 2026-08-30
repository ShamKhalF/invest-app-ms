package az.shlf.orderservice.repository;

import az.shlf.orderservice.entity.Order;
import az.shlf.orderservice.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
   
   Optional<Order> findByIdAndUsername(Long id, String username);

   Optional<Order> findByBinanceOrderId(String binanceOrderId);

   Optional<Order> findByIdempotencyKey(String idempotencyKey);

   List<Order> findAllByStatusIn(List<OrderStatus> statuses);
}
