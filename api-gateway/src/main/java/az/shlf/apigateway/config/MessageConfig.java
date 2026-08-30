package az.shlf.apigateway.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;
import org.springframework.web.server.i18n.LocaleContextResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class MessageConfig {

   @Bean
   public LocaleContextResolver localeContextResolver() {
      AcceptHeaderLocaleContextResolver localeContextResolver = new AcceptHeaderLocaleContextResolver();
      localeContextResolver.setDefaultLocale(Locale.ENGLISH);
      localeContextResolver.setSupportedLocales(Arrays.asList(
              Locale.of("en"),
              Locale.of("az"),
              Locale.of("ru")
      ));
      return localeContextResolver;
   }

   @Bean
   public MessageSource messageSource() {
      ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
      messageSource.setBasename("classpath:messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
   }
}