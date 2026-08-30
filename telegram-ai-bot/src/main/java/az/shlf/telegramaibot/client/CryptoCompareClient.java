//package az.shlf.telegramaibot.client;
//
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import java.util.Map;
//
//@FeignClient(name = "cryptoCompareClient", url = "https://min-api.cryptocompare.com/data/v2")
//public interface CryptoCompareClient {
//
//   @GetMapping("/news/?lang=EN")
//   Map<String, Object> getLatestNews();
//}