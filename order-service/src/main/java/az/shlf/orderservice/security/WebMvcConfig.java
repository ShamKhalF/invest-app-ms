package az.shlf.orderservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

   private final AuthInterceptor authInterceptor;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(authInterceptor)
              .addPathPatterns("/**")
              .excludePathPatterns(
                      "/v3/api-docs/**",
                      "/swagger-ui/**",
                      "/swagger-ui.html"
              );
   }

   @Override
   public void configurePathMatch(PathMatchConfigurer configurer) {
      configurer.addPathPrefix("/auth",
              HandlerTypePredicate.forAnnotation(RestController.class)
                      .and(HandlerTypePredicate.forBasePackage("az.shlf.authservice")));
   }
}
