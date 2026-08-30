package az.shlf.telegramaibot.dto;

import java.util.List;

public record CryptoContentDto(
        String uniqueId,       // Mənbənin təmin etdiyi ID və ya URL
        String title,          // Başlıq
        String content,        // Əsas mətn
        Long publishedAt,      // Unix Timestamp (Saniyə və ya Millisaniyə)
        List<String> symbols   // Aid olduğu koinlər (məs: BTC, ETH)
) {}