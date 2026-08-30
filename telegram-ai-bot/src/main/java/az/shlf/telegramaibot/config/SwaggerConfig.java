package az.shlf.telegramaibot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

   @Bean
   public OpenAPI customOpenAPI() {
      final String securitySchemeName = "bearerAuth";
      return new OpenAPI()
              .info(new Info()
                      .title("Invest App ms (auth-service) API")
                      .version("1.0")
                      .description("API documentation for Invest App's Auth-service with Multilingual Support"))
              // Gateway URL-ni Server kimi əlavə edirik ki, Swagger UI-dən gələn sorğular 8070 portuna (Gateway-ə) getsin
              .servers(List.of(new Server().url("http://localhost:8070").description("API Gateway")))
              .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
              .components(new Components()
                      .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                              .name(securitySchemeName)
                              .type(SecurityScheme.Type.HTTP)
                              .scheme("bearer")
                              .bearerFormat("JWT")));
   }

   @Bean
   public OperationCustomizer customGlobalHeaders() {
      return (operation, handlerMethod) -> {
         Parameter acceptLanguageHeader = new Parameter()
                 .in("header")
                 .name("Accept-Language")
                 .description("Preferred language for responses (e.g., az, en, ru)")
                 .required(false)
                 .schema(new StringSchema()._default("en"));

         operation.addParametersItem(acceptLanguageHeader);
         return operation;
      };
   }

}