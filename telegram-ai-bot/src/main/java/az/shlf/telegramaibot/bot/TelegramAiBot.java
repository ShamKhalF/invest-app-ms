package az.shlf.telegramaibot.bot;

import az.shlf.telegramaibot.entity.ChatSession;
import az.shlf.telegramaibot.enums.SenderType;
import az.shlf.telegramaibot.enums.UserStatus;
import az.shlf.telegramaibot.event.SendTelegramOtpEvent;
import az.shlf.telegramaibot.service.ChatManagerService;
import az.shlf.telegramaibot.service.DynamicRagChatService;
import az.shlf.telegramaibot.service.MessageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramAiBot extends TelegramLongPollingBot {

   @Value("${telegrambots.bot-username}")
   private String botUsername;

   @Value("${telegrambots.bot-token}")
   private String botToken;

   private final ChatManagerService chatManagerService;
   private final MessageProcessingService messageProcessingService;
   private final DynamicRagChatService ragChatService;

   private final Set<Long> processingUsers = ConcurrentHashMap.newKeySet();
   private final Map<Long, StringBuilder> messageBuffer = new ConcurrentHashMap<>();

   @Override
   public String getBotUsername() {
      return botUsername;
   }

   @Override
   public String getBotToken() {
      return botToken;
   }

   @Override
   public void onUpdateReceived(Update update) {
      if (!update.hasMessage() || !update.getMessage().hasText()) {
         return;
      }

      Message message = update.getMessage();
      Long telegramId = message.getChatId();
      String text = message.getText().trim();

      if (processingUsers.contains(telegramId)) {
         return;
      }

      if (text.startsWith("/start")) {
         handleStartCommand(telegramId, text);
         return;
      }

//      if (!chatManagerService.isActiveUserExists(telegramId)) {
//         sendMessage(telegramId, "Sistemə giriş icazəniz yoxdur. Veb platformadan təqdim olunan bot linkinə klikləyin.");
//         return;
//      }

      UserStatus userStatus = chatManagerService.getUserStatus(telegramId);

      // Hesab siliniblərsə gələn sorğu ignor edilir və xəbərdarlıq verilir
      if (userStatus == az.shlf.telegramaibot.enums.UserStatus.DELETED) {
         sendMessage(telegramId, "Belə hesab yoxdur.");
         return;
      }

      // Hesab heç yoxdursa və ya INACTIVE-dirsə
      if (userStatus != az.shlf.telegramaibot.enums.UserStatus.ACTIVE) {
         sendMessage(telegramId, "Sistemə giriş icazəniz yoxdur. Veb platformadan təqdim olunan bot linkinə klikləyin.");
         return;
      }

      if (text.equals("Aktiv Sessiya Yarat") || text.equals("/open_chat")) {
         messageBuffer.remove(telegramId);
         chatManagerService.openSession(telegramId);
         sendMessageWithKeyboard(telegramId, "Yeni sessiya açıldı. Sualınızı hissə-hissə yaza bilərsiniz. Bitirdikdə 'Cavablandır' düyməsinə klikləyin.");
         return;
      }

      if (text.equals("Sessiyanı Bağla") || text.equals("/close_chat")) {
         messageBuffer.remove(telegramId);
         chatManagerService.closeSession(telegramId);
         sendMessageWithKeyboard(telegramId, "Sessiyanız uğurla bağlandı.");
         return;
      }

      Optional<ChatSession> activeSessionOpt = chatManagerService.getActiveSession(telegramId);
      if (activeSessionOpt.isEmpty()) {
         sendMessageWithKeyboard(telegramId, "Aktiv sessiyanız yoxdur. Zəhmət olmasa 'Aktiv Sessiya Yarat' düyməsinə klikləyin.");
         return;
      }

      ChatSession session = activeSessionOpt.get();

      if (text.equals("Cavablandır") || text.equals("/submit")) {
         processBufferedMessages(telegramId, session);
         return;
      }

      messageBuffer.computeIfAbsent(telegramId, k -> new StringBuilder()).append(text).append("\n");
      messageProcessingService.saveMessage(session, SenderType.CUSTOMER, text);
   }

   private void processBufferedMessages(Long telegramId, ChatSession session) {
      StringBuilder bufferedText = messageBuffer.get(telegramId);

      if (bufferedText == null || bufferedText.toString().trim().isEmpty()) {
         sendMessage(telegramId, "LLM-ə göndərmək üçün heç bir məlumat daxil etməmisiniz.");
         return;
      }

      if (!processingUsers.add(telegramId)) {
         return;
      }

      try {
         sendMessage(telegramId, "Sorğunuz emal edilir. Süni intellekt cavab verənə qədər göndərdiyiniz mesajlar nəzərə alınmayacaq.");

         String combinedQuery = bufferedText.toString().trim();
         messageBuffer.remove(telegramId);

         // Məlumat bazasından cari sessiyanın tarixçəsini çəkirik
         List<az.shlf.telegramaibot.entity.Message> history = messageProcessingService.getSessionHistory(session.getId());

         String aiResponse;
         try {
            // Tarixçə LLM servisinə ötürülür
            aiResponse = ragChatService.processUserQuery(combinedQuery, history);
         } catch (Exception e) {
            log.error("LLM proses xətası: {}", e.getMessage());
            aiResponse = "Hazırda sorğunuza cavab verə bilmirəm. Texniki xəta baş verdi.";
         }

         Optional<ChatSession> currentSessionStatus = chatManagerService.getActiveSession(telegramId);
         if (currentSessionStatus.isEmpty()) {
            log.info("İstifadəçi {} sessiyanı LLM cavabından əvvəl bağlayıb. Mesaj ləğv edildi.", telegramId);
            return;
         }

         messageProcessingService.saveMessage(session, SenderType.AI, aiResponse);
         sendMessage(telegramId, aiResponse);

      } finally {
         processingUsers.remove(telegramId);
      }
   }

   private void handleStartCommand(Long telegramId, String text) {
      String[] parts = text.split(" ");
      if (parts.length < 2) {
         sendMessage(telegramId, "Yanlış başlanğıc komandası. Veb platformadan verilən linklə daxil olun.");
         return;
      }

      String username = parts[1];

      try {
         chatManagerService.activateUser(username, telegramId);
         sendMessageWithKeyboard(telegramId, "Hesabınız aktivləşdirildi. Menyu vasitəsilə sessiya yaradın.");
      } catch (Exception e) {
         sendMessage(telegramId, "Aktivasiya xətası: İstifadəçi tapılmadı və ya artıq aktivdir.");
      }
   }

   private void sendMessage(Long chatId, String text) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId.toString());
      message.setText(text);
      try {
         execute(message);
      } catch (TelegramApiException e) {
         log.error("Mesaj göndərilmədi: {}", e.getMessage());
      }
   }

   private void sendMessageWithKeyboard(Long chatId, String text) {
      SendMessage message = new SendMessage();
      message.setChatId(chatId.toString());
      message.setText(text);

      ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
      keyboardMarkup.setResizeKeyboard(true);
      keyboardMarkup.setOneTimeKeyboard(false);

      List<KeyboardRow> keyboard = new ArrayList<>();

      KeyboardRow row1 = new KeyboardRow();
      row1.add("Aktiv Sessiya Yarat");
      row1.add("Sessiyanı Bağla");

      KeyboardRow row2 = new KeyboardRow();
      row2.add("Cavablandır");

      keyboard.add(row1);
      keyboard.add(row2);

      keyboardMarkup.setKeyboard(keyboard);
      message.setReplyMarkup(keyboardMarkup);

      try {
         execute(message);
      } catch (TelegramApiException e) {
         log.error("Klaviaturalı mesaj göndərilmədi: {}", e.getMessage());
      }
   }

   // Mövcud importlara bunları əlavə edin
   // import org.springframework.context.event.EventListener;
   // import az.shlf.telegramaibot.event.SendTelegramOtpEvent;

   @EventListener
   public void handleSendTelegramOtpEvent(SendTelegramOtpEvent event) {
      String text = String.format(
              "Hesabınızın silinməsi üçün OTP kodunuz: *%s*\n\nBu kod 10 dəqiqə ərzində etibarlıdır.",
              event.getOtpCode()
      );

      SendMessage message = new SendMessage();
      message.setChatId(event.getChatId().toString());
      message.setText(text);
      message.setParseMode("Markdown"); // Kodun qalın (bold) görünməsi üçün

      try {
         execute(message);
         log.info("OTP mesajı {} nömrəli chat-a uğurla göndərildi.", event.getChatId());
      } catch (TelegramApiException e) {
         log.error("OTP mesajı göndərilərkən xəta baş verdi: {}", e.getMessage());
      }
   }


}