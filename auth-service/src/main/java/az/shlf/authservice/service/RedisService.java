package az.shlf.authservice.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {

   <T> T get(String key, Class<T> type);

   <T> void set(String key, T value);

   <T> void set(String key, T value, long timeout, TimeUnit unit);

   void delete(String key);

   void publish(String channel, String message);

   Long increment(String key);

   Boolean expire(String key, long timeout, TimeUnit unit);
}
