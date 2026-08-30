package az.shlf.streamservice.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SymbolWatchPublisher {

   private final KafkaTemplate<String, Object> kafkaTemplate;

   @Value("${app.kafka.topics.symbol.name}")
   private String topicName;

   public void publishSymbol(String symbol) {
      try {
         Map<String, String> event = new HashMap<>();
         event.put("symbol", symbol);
         kafkaTemplate.send(topicName, topicName, event);
      } catch (Exception e) {
         log.error("Kafka-ya simvol göndərilərkən xəta baş verdi: {}", symbol, e);
      }
   }
}