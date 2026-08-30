package az.shlf.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "binanceOrderClient", url = "${binance.api.base-url}")
public interface BinanceOrderClient {

   @PostMapping("/api/v3/order")
   Object createOrder(
           @RequestHeader("X-MBX-APIKEY") String apiKey,
           @RequestParam("symbol") String symbol,
           @RequestParam("side") String side,
           @RequestParam("type") String type,
           @RequestParam(value = "timeInForce", required = false) String timeInForce,
           @RequestParam("quantity") BigDecimal quantity,
           @RequestParam(value = "price", required = false) BigDecimal price,
           @RequestParam("timestamp") long timestamp,
           @RequestParam("signature") String signature
   );

   @DeleteMapping("/api/v3/order")
   Object cancelOrder(
           @RequestHeader("X-MBX-APIKEY") String apiKey,
           @RequestParam("symbol") String symbol,
           @RequestParam("orderId") String orderId,
           @RequestParam("timestamp") long timestamp,
           @RequestParam("signature") String signature
   );

   @GetMapping("/api/v3/order")
   Map<String, Object> getOrder(
           @RequestHeader("X-MBX-APIKEY") String apiKey,
           @RequestParam("symbol") String symbol,
           @RequestParam("orderId") String orderId,
           @RequestParam("timestamp") long timestamp,
           @RequestParam("signature") String signature
   );

   @PostMapping("/api/v3/userDataStream")
   Map<String, String> startUserDataStream(@RequestHeader("X-MBX-APIKEY") String apiKey);

   @PutMapping("/api/v3/userDataStream")
   void keepAliveUserDataStream(@RequestHeader("X-MBX-APIKEY") String apiKey, @RequestParam("listenKey") String listenKey);

   @GetMapping("/api/v3/ticker/price")
   Map<String, Object> getTickerPrice(@RequestParam("symbol") String symbol);
}