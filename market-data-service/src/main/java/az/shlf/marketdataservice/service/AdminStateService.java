package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.model.enums.RedisChannel;

import java.util.Set;

public interface AdminStateService {
   void setStreamActive(boolean isActive);
   boolean isStreamActive();
   Set<String> getActiveSymbols(RedisChannel channel);

   // Yeni əlavə olunanlar
   void stopAllStreams();
   void startAllStreams();
}