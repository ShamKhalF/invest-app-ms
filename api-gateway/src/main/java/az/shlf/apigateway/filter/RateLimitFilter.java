package az.shlf.apigateway.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RateLimitFilter implements GlobalFilter, Ordered {

   private final ProxyManager<String> proxyManager;

   @Override
   public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
      String clientIp = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
      String path = exchange.getRequest().getURI().getPath();

      String bucketKey = "rate_limit:" + clientIp + ":" + path;

      return Mono.defer(() -> Mono.fromFuture(
              proxyManager.asAsync().builder().build(
                      bucketKey,
                      () -> java.util.concurrent.CompletableFuture.completedFuture(
                              BucketConfiguration.builder()
                                      .addLimit(
                                              Bandwidth.builder()
                                                      .capacity(5)
                                                      .refillGreedy(5, Duration.ofSeconds(10))
                                                      .build()
                                      )
                                      .build()
                      )
              ).tryConsume(1)
      )).flatMap(consumed -> {
         if (consumed) {
            return chain.filter(exchange);
         }
         exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
         return exchange.getResponse().setComplete();
      });
   }

   @Override
   public int getOrder() {
      return -1;
   }
}