package az.shlf.orderservice.controller;

import az.shlf.orderservice.dto.request.OrderCreateRequest;
import az.shlf.orderservice.dto.response.OrderResponse;
import az.shlf.orderservice.dto.response.VoidResponse;
import az.shlf.orderservice.entity.enums.OrderStatus;
import az.shlf.orderservice.security.RequirePermission;
import az.shlf.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

   private final OrderService orderService;

   @PostMapping
   @RequirePermission({"order:write"})
   public ResponseEntity<OrderResponse> createOrder(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                                    @RequestBody OrderCreateRequest request,
                                                    HttpServletRequest servletRequest) {

      String username = servletRequest.getHeader("X-Username");
      return ResponseEntity.ok(orderService.createOrder(username, idempotencyKey, request));
   }

   @DeleteMapping("/{id}")
   @RequirePermission({"order:write"})
   public ResponseEntity<VoidResponse> cancelOrder(
           @PathVariable Long id,
           HttpServletRequest servletRequest) {

      String username = servletRequest.getHeader("X-Username");
      return ResponseEntity.ok(orderService.cancelOrder(username, id));
   }

   @GetMapping
   @RequirePermission({"order:read"})
   public ResponseEntity<Page<OrderResponse>> getOrderHistory(
           HttpServletRequest servletRequest,
           @RequestParam(required = false) String symbol,
           @RequestParam(required = false) OrderStatus status,
           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
           @RequestParam(required = false, defaultValue = "0") int page,
           @RequestParam(required = false, defaultValue = "20") int size) {

      String username = servletRequest.getHeader("X-Username");
      Page<OrderResponse> response = orderService.getOrderHistory(username, symbol, status, startDate, endDate, page, size);

      return ResponseEntity.ok(response);
   }

}