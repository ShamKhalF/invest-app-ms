package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.dto.KlineSearchDto;
import az.shlf.marketdataservice.dto.TopSymbolSearchDto;
import az.shlf.marketdataservice.dto.response.SymbolHourlyKlineResponseDto;
import az.shlf.marketdataservice.dto.response.TopWatchedSymbolResponseDto;
import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import az.shlf.marketdataservice.entity.TopWatchedSymbolEntity;
import az.shlf.marketdataservice.repository.SymbolHourlyKlineRepository;
import az.shlf.marketdataservice.repository.TopWatchedSymbolRepository;
import az.shlf.marketdataservice.specification.SymbolHourlyKlineSpecification;
import az.shlf.marketdataservice.specification.TopWatchedSymbolSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class MarketDataAnalyticsService {

   private final TopWatchedSymbolRepository topWatchedSymbolRepository;
   private final SymbolHourlyKlineRepository symbolHourlyKlineRepository;

   public Page<TopWatchedSymbolResponseDto> getTopSymbols(TopSymbolSearchDto dto) {
      Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.DESC, "watchCount"));
      Specification<TopWatchedSymbolEntity> spec = TopWatchedSymbolSpecification.getFilter(dto);

      return topWatchedSymbolRepository.findAll(spec, pageable).map(this::mapToTopSymbolDto);
   }

   public Page<SymbolHourlyKlineResponseDto> getKlinesWithFilters(KlineSearchDto dto) {
      Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize(), Sort.by(Sort.Direction.DESC, "closeTime"));
      Specification<SymbolHourlyKlineEntity> spec = SymbolHourlyKlineSpecification.getFilter(dto);

      return symbolHourlyKlineRepository.findAll(spec, pageable).map(this::mapToKlineDto);
   }

   private TopWatchedSymbolResponseDto mapToTopSymbolDto(TopWatchedSymbolEntity entity) {
      return TopWatchedSymbolResponseDto.builder()
              .symbol(entity.getSymbol())
              .name(entity.getName())
              .watchCount(entity.getWatchCount())
              .build();
   }

   private SymbolHourlyKlineResponseDto mapToKlineDto(SymbolHourlyKlineEntity entity) {
      return SymbolHourlyKlineResponseDto.builder()
              .id(entity.getId())
              .symbol(entity.getSymbol())
              .openTime(convertToLocalDateTime(entity.getOpenTime()))
              .closeTime(convertToLocalDateTime(entity.getCloseTime()))
              .openPrice(entity.getOpenPrice())
              .highPrice(entity.getHighPrice())
              .lowPrice(entity.getLowPrice())
              .closePrice(entity.getClosePrice())
              .volume(entity.getVolume())
              .build();
   }

   private LocalDateTime convertToLocalDateTime(Long timestampMillis) {
      if (timestampMillis == null) return null;
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestampMillis), ZoneId.systemDefault());
   }
}