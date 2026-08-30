package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.dto.CryptoContentDto;
import az.shlf.telegramaibot.provider.RagDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagIngestionService {

   private final VectorStore vectorStore;
   // Bütün RagDataProvider implementasiyalarını (News, Terminology və s.) avtomatik yığır
   private final List<RagDataProvider> dataProviders;


   public void processAndSaveAllData() {
      List<Document> documentsToSave = new ArrayList<>();

      for (RagDataProvider provider : dataProviders) {
         List<CryptoContentDto> contents = provider.fetchData();

         for (CryptoContentDto dto : contents) {
            // Deterministik UUID - Mənbə ID-si eyni olduqca eyni UUID yaranacaq
            String documentId = UUID.nameUUIDFromBytes(dto.uniqueId().getBytes(StandardCharsets.UTF_8)).toString();

            String content = String.format("Mövzu: %s\nDetallar: %s", dto.title(), dto.content());

            // Metadata Zənginləşdirilməsi
            Map<String, Object> metadata = Map.of(
                    "publishedAt", dto.publishedAt(),
                    "relatedSymbols", String.join(",", dto.symbols()),
                    "source", provider.getClass().getSimpleName()
            );

            documentsToSave.add(new Document(documentId, content, metadata));
         }
      }

      if (!documentsToSave.isEmpty()) {
         // Sənədləri token limitinə uyğun parçalamaq üçün TokenTextSplitter tətbiq edilir
         TokenTextSplitter textSplitter = new TokenTextSplitter();
         List<Document> splitDocuments = textSplitter.apply(documentsToSave);

         // Parçalanmış sənədlər bazaya yazılır
         vectorStore.add(splitDocuments);
         log.info("RAG VectorStore-a ümumi {} sənəd hissəsi (chunk) yazıldı/yeniləndi.", splitDocuments.size());
      }
   }

//   public void processAndSaveAllData() {
//      List<Document> documentsToSave = new ArrayList<>();
//
//      for (RagDataProvider provider : dataProviders) {
//         List<CryptoContentDto> contents = provider.fetchData();
//
//         for (CryptoContentDto dto : contents) {
//            // Deterministik UUID - Mənbə ID-si eyni olduqca eyni UUID yaranacaq
//            String documentId = UUID.nameUUIDFromBytes(dto.uniqueId().getBytes(StandardCharsets.UTF_8)).toString();
//
//            String content = String.format("Mövzu: %s\nDetallar: %s", dto.title(), dto.content());
//
//            // Metadata Zənginləşdirilməsi
//            Map<String, Object> metadata = Map.of(
//                    "publishedAt", dto.publishedAt(),
//                    "relatedSymbols", String.join(",", dto.symbols()),
//                    "source", provider.getClass().getSimpleName()
//            );
//
//            documentsToSave.add(new Document(documentId, content, metadata));
//         }
//      }
//
//      if (!documentsToSave.isEmpty()) {
//         // vectorStore.add metodu eyni ID-yə malik sənədləri üstələyir (upsert), bu da dublikatın qarşısını alır
//         vectorStore.add(documentsToSave);
//         log.info("RAG VectorStore-a ümumi {} sənəd yazıldı/yeniləndi.", documentsToSave.size());
//      }
//   }
}