package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.dto.HourlyKlineDto;
import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataKafkaProducer {

   private final KafkaTemplate<String, Object> kafkaTemplate;

   @Value("${app.kafka.topics.hourly-kline.name}")
   private String hourlyKlineTopicName;

   public void sendKlineData(SymbolHourlyKlineEntity entity) {
//      HourlyKlineDto dto = new HourlyKlineDto(
//              entity.getSymbol(),
//              entity.getOpenTime(),
//              entity.getCloseTime(),
//              entity.getOpenPrice(),
//              entity.getHighPrice(),
//              entity.getLowPrice(),
//              entity.getClosePrice(),
//              entity.getVolume()
//      );
//
//      kafkaTemplate.send(hourlyKlineTopicName, dto.symbol(), dto);
//      log.info("Bazar məlumatı Kafka-ya göndərildi. Koin: {}", dto.symbol());
   }

}