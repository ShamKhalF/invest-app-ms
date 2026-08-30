package az.shlf.telegramaibot.provider.impl;

import az.shlf.telegramaibot.dto.CryptoContentDto;
import az.shlf.telegramaibot.provider.RagDataProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoTerminologyProvider implements RagDataProvider {

   private final ObjectMapper objectMapper;

   @Override
   public List<CryptoContentDto> fetchData() {
      List<CryptoContentDto> result = new ArrayList<>();
      try {
         ClassPathResource resource = new ClassPathResource("crypto-terms.json");
         try (InputStream inputStream = resource.getInputStream()) {
            List<Map<String, Object>> terms = objectMapper.readValue(inputStream, new TypeReference<>() {});

            long currentTimestamp = Instant.now().getEpochSecond();
            int index = 1;

            for (Map<String, Object> term : terms) {
               String title = (String) term.get("title");
               String content = (String) term.get("content");
               List<String> symbols = (List<String>) term.get("symbols");

               result.add(new CryptoContentDto(
                       "term-" + index++,
                       title,
                       content,
                       currentTimestamp,
                       symbols
               ));
            }
         }
      } catch (Exception e) {
         log.error("Terminologiya məlumatları oxunarkən xəta baş verdi: {}", e.getMessage());
      }
      return result;
   }
}