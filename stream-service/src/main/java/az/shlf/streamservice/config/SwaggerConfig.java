package az.shlf.streamservice.config;

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

      // Swagger-də HTML səhifəsinə keçid linki yaradılır
      String description = "API documentation for Stream Service.<br><br>" +
              "<b><a href=\"/stream/ws-ui/index\" target=\"_blank\">➡️ Canlı Qrafik (WebSocket UI) Səhifəsinə Keçid</a></b>";

      return new OpenAPI()
              .info(new Info()
                      .title("Stream Service API")
                      .version("1.0")
                      .description(description))
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