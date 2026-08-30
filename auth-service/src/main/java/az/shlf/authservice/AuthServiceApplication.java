package az.shlf.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class AuthServiceApplication {

   public static void main(String[] args) {
      SpringApplication.run(AuthServiceApplication.class, args);
   }

}
