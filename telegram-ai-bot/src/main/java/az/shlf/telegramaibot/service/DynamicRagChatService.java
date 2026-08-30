package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.enums.SenderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DynamicRagChatService {

   private final ChatClient chatClient;
   private final JdbcTemplate jdbcTemplate;
   private final VectorStore vectorStore;

   private record RouteInfo(String targetDb, String symbol, String sql, String language) {}

   private static final String ROUTER_PROMPT = """
            Sən verilənlər bazası marşrutlayıcısı (Router) və PostgreSQL mütəxəssisisən. Sualı analiz et və qərar ver:
            
            1. TARGET "POSTGRES": Sual kriptovalyutaların qiyməti, ticarət həcmi, maksimum/minimum rəqəmlər, tarix aralıqları və ya statistik müqayisələr tələb edirsə.
            2. TARGET "VECTOR": Sual kriptovalyuta xəbərləri, layihələrin məqsədi, texnoloji yeniliklər, terminologiya və ya ümumi mətn xarakterli məlumatlardırsa.
            3. TARGET "REJECT": Sual kriptovalyuta, blockchain, maliyyə bazarları, xəbərlər və ya trading ilə qətiyyən əlaqədar DEYİLSƏ.
            
            QAYDALAR (POSTGRES üçün):
            - Cədvəl: symbol_hourly_klines (id, close_price, close_time, high_price, low_price, open_price, open_time, symbol, volume)
            - Vaxt filtri üçün (Epoch ms): to_timestamp(close_time / 1000). Hazırki tarix: 2026-08-12.
            - Ekstremumlar üçün: MAX() əvəzinə ORDER BY ... LIMIT 1 istifadə et.
            
            ÇIXIŞ FORMATI (qəti şəkildə bu formatı qoru, markdown istifadə etmə):
            TARGET: [POSTGRES, VECTOR və ya REJECT]
            SYMBOL: [Koinin qısa simvolu. POSTGRES üçün tam cütlük, məs: BTCUSDT. Koin yoxdursa ALL yaz]
            SQL: [Əgər POSTGRES seçilibsə SQL kodunu bura yaz, əks halda NONE yaz]
            LANGUAGE: [Sualın dili: AZ, EN və ya RU]
            
            ƏVVƏLKİ SÖHBƏT TARİXÇƏSİ (Sualda "o", "bu", "onun" kimi əvəzliklər varsa, hansı koindən bəhs edildiyini anlamaq üçün istifadə et):
            %s
            
            İstifadəçinin CARİ sualı: %s
            """;

   private static final String FINAL_PROMPT = """
            Sən təqdim olunan xam məlumatlara (raw data) əsasən istifadəçiyə dəqiq cavab verən analitik botusan.
            Sənə verilən məlumatlardan kənara çıxmaq, özündən fakt uydurmaq və ya mövzudankənar fərziyyələr irəli sürmək QƏTİ QADAĞANDIR.
            
            CAVABIN FORMATLANMASI QAYDALARI:
            1. Məlumatda mövcud olan detalları oxunaqlı və cədvəl/siyahı formasında təqdim et.
            2. "Verilənlər Bazasından Gələn Nəticə" boşdursa və ya "tapılmadı" yazılıbsa, istifadəçiyə məlumatın olmadığını bildir və fərziyyə yürütmə.
            3. DİL QAYDASI: Mütləq və qəti şəkildə "%s" dilində cavab yaz. Baza nəticəsi (xəbər və ya data) fərqli dildə olsa belə, onu tələb olunan dilə tərcümə edərək cavablandır.
            
            Məlumat Mənbəyi: %s
            Verilənlər Bazasından Gələn Nəticə:
            {dbContext}
            """;

   public DynamicRagChatService(ChatClient.Builder chatClientBuilder, JdbcTemplate jdbcTemplate, VectorStore vectorStore) {
      this.chatClient = chatClientBuilder.build();
      this.jdbcTemplate = jdbcTemplate;
      this.vectorStore = vectorStore;
   }

   public String processUserQuery(String userQuery, List<az.shlf.telegramaibot.entity.Message> history) {
      RouteInfo routeInfo = determineRoute(userQuery, history);

      if ("REJECT".equalsIgnoreCase(routeInfo.targetDb())) {
         return switch (routeInfo.language().toUpperCase()) {
            case "EN" -> "Your question is outside the domain of cryptocurrency or financial markets. I can only provide information on these topics.";
            case "RU" -> "Ваш вопрос выходит за рамки криптовалют или финансовых рынков. Я могу предоставить информацию только по этим темам.";
            default -> "Sualınız kriptovalyuta və ya maliyyə bazarları domenindən kənardır. Yalnız bu mövzularda məlumat verə bilərəm.";
         };
      }

      String dbContext = fetchDataFromSource(routeInfo, userQuery);

      return generateFinalResponse(userQuery, routeInfo.targetDb(), dbContext, routeInfo.language(), history);
   }

   private RouteInfo determineRoute(String userQuery, List<az.shlf.telegramaibot.entity.Message> history) {
      String historyText = history.stream()
              .map(m -> (m.getSenderType() == SenderType.CUSTOMER ? "İstifadəçi: " : "Bot: ") + m.getText())
              .collect(Collectors.joining("\n"));

      if (historyText.trim().isEmpty()) {
         historyText = "Tarixçə yoxdur.";
      }

      String llmResponse = chatClient.prompt()
              .user(String.format(ROUTER_PROMPT, historyText, userQuery))
              .call()
              .content()
              .trim();

      String targetDb = "REJECT";
      String detectedSymbol = "ALL";
      String language = "AZ";
      StringBuilder sqlBuilder = new StringBuilder();
      boolean isSqlPart = false;

      for (String line : llmResponse.split("\n")) {
         if (line.startsWith("TARGET:")) {
            targetDb = line.replace("TARGET:", "").trim();
         } else if (line.startsWith("SYMBOL:")) {
            detectedSymbol = line.replace("SYMBOL:", "").trim();
         } else if (line.startsWith("LANGUAGE:")) {
            language = line.replace("LANGUAGE:", "").trim();
         } else if (line.startsWith("SQL:")) {
            isSqlPart = true;
            sqlBuilder.append(line.replace("SQL:", "").trim()).append(" ");
         } else if (isSqlPart) {
            sqlBuilder.append(line.trim()).append(" ");
         }
      }

      String sql = sqlBuilder.toString().replace("```sql", "").replace("```", "").trim();
      return new RouteInfo(targetDb, detectedSymbol, sql, language);
   }

   private String fetchDataFromSource(RouteInfo routeInfo, String userQuery) {
      if ("POSTGRES".equalsIgnoreCase(routeInfo.targetDb())) {
         return executePostgresQuery(routeInfo.sql());
      } else {
         return executeVectorSearch(userQuery, routeInfo.symbol());
      }
   }

   private String executePostgresQuery(String sql) {
      try {
         List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
         if (resultList.isEmpty()) {
            return "Məlumat tapılmadı.";
         }
         StringBuilder sb = new StringBuilder();
         for (Map<String, Object> row : resultList) {
            sb.append(row.toString()).append("\n");
         }
         return sb.toString();
      } catch (Exception e) {
         log.error("SQL icra xətası: {}", e.getMessage());
         return "SQL Xətası: " + e.getMessage();
      }
   }

   private String executeVectorSearch(String userQuery, String detectedSymbol) {
      SearchRequest searchRequest = SearchRequest.builder()
              .query(userQuery)
              .topK(10)
              .build();

      if (!"ALL".equalsIgnoreCase(detectedSymbol) && !detectedSymbol.isEmpty()) {
         String filterSymbol = detectedSymbol.replace("USDT", "").replace("BUSD", "");
         String expression = String.format("relatedSymbols == '%s' || relatedSymbols == 'CRYPTO' || relatedSymbols == 'ALL'", filterSymbol);

         searchRequest = SearchRequest.builder()
                 .query(userQuery)
                 .topK(10)
                 .filterExpression(expression)
                 .build();
      }

      List<Document> similarDocs = vectorStore.similaritySearch(searchRequest);
      String context = similarDocs.stream()
              .map(Document::getFormattedContent)
              .collect(Collectors.joining("\n"));

      return context.isEmpty() ? "Bu mövzuda xəbər və ya məqalə tapılmadı." : context;
   }

   private String generateFinalResponse(String userQuery, String targetDb, String dbContext, String language, List<az.shlf.telegramaibot.entity.Message> history) {
      List<org.springframework.ai.chat.messages.Message> springAiMessages = new ArrayList<>();

      // 1. Sistem təlimatlarının formalaşdırılması
      String systemText = String.format(FINAL_PROMPT, language, targetDb).replace("{dbContext}", dbContext);
      springAiMessages.add(new SystemMessage(systemText));

      // 2. Baza tarixçəsinin Spring AI Message obyektlərinə çevrilərək ötürülməsi
      for (az.shlf.telegramaibot.entity.Message msg : history) {
         if (msg.getSenderType() == SenderType.CUSTOMER) {
            springAiMessages.add(new UserMessage(msg.getText()));
         } else if (msg.getSenderType() == SenderType.AI) {
            springAiMessages.add(new AssistantMessage(msg.getText()));
         }
      }

      // 3. Ən son göndərilən sual
      springAiMessages.add(new UserMessage(userQuery));

      return chatClient.prompt()
              .messages(springAiMessages)
              .call()
              .content();
   }

}