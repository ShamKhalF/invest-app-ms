package az.shlf.walletservice.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class MessageConfig {

   @Bean
   public LocaleResolver localeResolver() {
      AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
      localeResolver.setDefaultLocale(Locale.ENGLISH);
      localeResolver.setSupportedLocales(Arrays.asList(
              Locale.of("en"),
              Locale.of("az"),
              Locale.of("ru")
      ));
      return localeResolver;
   }

   @Bean
   public MessageSource messageSource() {
      ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
      messageSource.setBasename("classpath:messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
   }

   @Bean
   public LocalValidatorFactoryBean getValidator() {
      LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
      bean.setValidationMessageSource(messageSource());
      return bean;
   }

}
