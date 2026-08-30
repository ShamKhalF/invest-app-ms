package az.shlf.marketdataservice.dto;

import java.math.BigDecimal;

public record HourlyKlineDto(
        String symbol,
        Long openTime,
        Long closeTime,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal volume
) {}
