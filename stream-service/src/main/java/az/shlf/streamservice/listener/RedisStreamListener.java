package az.shlf.streamservice.listener;

import az.shlf.streamservice.model.enums.StreamType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamListener implements MessageListener {

   private final SimpMessagingTemplate messagingTemplate;
   private final ObjectMapper objectMapper;

   @Override
   public void onMessage(Message message, byte[] pattern) {
      try {
         // 1. Kanal adını alırıq və Enum-u tapırıq (if-else silindi!)
         String channelName = new String(pattern);
         StreamType streamType = StreamType.fromChannel(channelName);

         // 2. Datanı oxuyuruq
         String jsonMelumat = new String(message.getBody());
         JsonNode dataNode = objectMapper.readTree(jsonMelumat);

         // 3. Paylayırıq
         if (dataNode.isArray()) {
            for (JsonNode koin : dataNode) {
               sendToTopic(streamType, koin);
            }
         } else {
            sendToTopic(streamType, dataNode);
         }

      } catch (Exception e) {
         log.error("Redis mesajı emal edilərkən xəta baş verdi: ", e);
      }
   }

   private void sendToTopic(StreamType streamType, JsonNode koinData) {
      if (koinData.has("symbol")) {
         String symbol = koinData.get("symbol").asText().toLowerCase();

         // Enum-dakı topicName istifadə edərək dinamik otaq yaradırıq
         String destination = "/topic/" + streamType.getTopicName() + "/" + symbol;

         messagingTemplate.convertAndSend(destination, koinData.toString());
      }
   }

}