package az.shlf.apigateway.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // SpringDoc-un Gateway üçün dinamik qruplaşdırması. 
    // application.yml-dən static url-lər əvəzinə əgər discovery service istifadə edilərsə 
    // dinamik də edilə bilər. Hal-hazırda .yml faylında manual config verdiyimiz üçün 
    // bu klassda xüsusi bir Bean-ə ehtiyac qalmır, yml yetərlidir.
    // Lakin bəzən gateway üçün xüsusi bean konfiqurasiyası tələb oluna bilər.
}