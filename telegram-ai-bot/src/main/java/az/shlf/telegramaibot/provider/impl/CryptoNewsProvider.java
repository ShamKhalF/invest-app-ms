package az.shlf.telegramaibot.provider.impl;

import az.shlf.telegramaibot.dto.CryptoContentDto;
import az.shlf.telegramaibot.provider.RagDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CryptoNewsProvider implements RagDataProvider {

   private final RestTemplate restTemplate;

   private static final List<String> RSS_FEED_URLS = List.of(
           "https://cointelegraph.com/rss",
           "https://www.coindesk.com/arc/outboundfeeds/rss/",
           "https://news.bitcoin.com/feed/",
           "https://cryptoslate.com/feed/"
   );
   private static final String COINDESK_API_URL = "https://data-api.coindesk.com/news/v1/article/list?lang=EN&limit=100";

   public CryptoNewsProvider() {
      this.restTemplate = new RestTemplate();
   }

   @Override
   public List<CryptoContentDto> fetchData() {
      List<CryptoContentDto> result = new ArrayList<>();

      result.addAll(fetchFromRss2Json());
      result.addAll(fetchFromCoinDeskApi());

      log.info("Bütün mənbələrdən ümumilikdə {} ədəd güncəl xəbər toplandı.", result.size());
      return result;
   }

   private List<CryptoContentDto> fetchFromRss2Json() {
      List<CryptoContentDto> result = new ArrayList<>();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      long oneMonthAgo = LocalDateTime.now().minusMonths(1).toEpochSecond(ZoneOffset.UTC);

      for (String rssUrl : RSS_FEED_URLS) {
         try {
            String apiUrl = "https://api.rss2json.com/v1/api.json?rss_url=" + rssUrl;
            Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);

            if (response == null || !"ok".equals(response.get("status")) || !response.containsKey("items")) {
               log.warn("{} (RSS2JSON) mənbəsindən məlumat çəkilə bilmədi.", rssUrl);
               continue;
            }

            List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("items");

            for (Map<String, Object> article : articles) {
               String title = (String) article.get("title");
               String description = (String) article.get("description");
               String link = (String) article.get("link");
               String pubDateStr = (String) article.get("pubDate");

               long publishedOn = LocalDateTime.parse(pubDateStr, formatter).toEpochSecond(ZoneOffset.UTC);

               if (publishedOn < oneMonthAgo) {
                  continue;
               }

               List<String> categories = (List<String>) article.get("categories");
               if (categories == null || categories.isEmpty()) {
                  categories = List.of("CRYPTO");
               }

               String cleanContent = description != null ? description.replaceAll("<[^>]*>", "").trim() : title;

               result.add(new CryptoContentDto(
                       link,
                       title,
                       cleanContent,
                       publishedOn,
                       categories
               ));
            }
            log.info("{} mənbəsindən {} xəbər çəkildi.", rssUrl, articles.size());
         } catch (Exception e) {
            log.error("{} linki üçün RSS xətası: {}", rssUrl, e.getMessage());
         }
      }
      return result;
   }

   private List<CryptoContentDto> fetchFromCoinDeskApi() {
      List<CryptoContentDto> result = new ArrayList<>();
      try {
         HttpHeaders headers = new HttpHeaders();
         headers.set("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36");
         headers.set("Accept", "application/json");

         // Brauzerinizin verdiyi auth_key cookie-sini bura əlavə edirik
         headers.set("Cookie", "auth_key=f2220e9a49c8ed0ba3f5ef36c120837aa6c5819b11c009dadc0bdec505e7233a");

         HttpEntity<String> entity = new HttpEntity<>(headers);
         ResponseEntity<Map> responseEntity = restTemplate.exchange(COINDESK_API_URL, HttpMethod.GET, entity, Map.class);
         Map<String, Object> response = responseEntity.getBody();

         if (response == null || !response.containsKey("Data") || response.get("Data") == null) {
            return result;
         }

         List<Map<String, Object>> articles = (List<Map<String, Object>>) response.get("Data");
         long oneMonthAgo = Instant.now().minus(30, ChronoUnit.DAYS).getEpochSecond();

         for (Map<String, Object> article : articles) {
            Object publishedOnObj = article.get("PUBLISHED_ON");
            if (publishedOnObj == null) continue;

            long publishedOn = ((Number) publishedOnObj).longValue();
            if (publishedOn < oneMonthAgo) continue;

            String id = article.get("ID").toString();
            String title = (String) article.get("TITLE");
            String body = (String) article.get("BODY");

            List<String> symbols = new ArrayList<>();
            if (article.containsKey("CATEGORY_DATA") && article.get("CATEGORY_DATA") != null) {
               List<Map<String, Object>> categoryData = (List<Map<String, Object>>) article.get("CATEGORY_DATA");
               for (Map<String, Object> cat : categoryData) {
                  if (cat.containsKey("NAME") && cat.get("NAME") != null) {
                     symbols.add(cat.get("NAME").toString());
                  }
               }
            }

            if (symbols.isEmpty()) {
               symbols.add("ALL");
            }

            result.add(new CryptoContentDto(
                    "coindesk-news-" + id,
                    title,
                    body,
                    publishedOn,
                    symbols
            ));
         }
         log.info("CoinDesk API-dən {} ədəd xəbər çəkildi.", result.size());
      } catch (Exception e) {
         log.error("CoinDesk API xətası (Avtorizasiya və ya limitsiz əlçatanlıq problemi): {}", e.getMessage());
      }
      return result;
   }

}