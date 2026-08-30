package az.shlf.telegramaibot.provider;

import az.shlf.telegramaibot.dto.CryptoContentDto;

import java.util.List;

public interface RagDataProvider {
   List<CryptoContentDto> fetchData();
}
