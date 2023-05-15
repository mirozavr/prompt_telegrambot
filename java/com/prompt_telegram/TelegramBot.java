package com.prompt_telegram;

import com.prompt_telegram.entity.StableDiffusionQueryes;
import com.prompt_telegram.repository.StableDiffusionQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private Map<String, Boolean> userStates = new HashMap<>();
    private static final int stableDiffusion = 1;
    private static final int MidJourney = 2;
    private static int sostoyanie;

    @Autowired
    private StableDiffusionQueryRepository stableRepo;

    private final String url = "jdbc:postgresql://localhost/mypromptgen";
    private final String username = "postgres";
    private final String password = "vilka88";

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";


    public TelegramBot() {

        List<BotCommand> listOfCommands = new ArrayList(); // BotCommand - удобный класс телеграм для создания и описания команд.
        listOfCommands.add(new BotCommand("/start", "This is start (first) command"));
        listOfCommands.add(new BotCommand("/planes", "Click to see prices"));
        listOfCommands.add(new BotCommand("/my_prompts", "Get your prompts store"));
        listOfCommands.add(new BotCommand("/delete_prompts", "Delete your prompts"));
        listOfCommands.add(new BotCommand("/settings", "set your preferences"));
        listOfCommands.add(new BotCommand("/help", "Press for take care!"));

        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getBotUsername() {
        return "midjourney_best_bot";
    }

    @Override
    public String getBotToken() {
        return "5870416748:AAFqvFIZ-hOhpsXDuKK0CsWNm6VasSoFfOE";
    }

    private void firstChoiceNeural(Message message, String hello) {  // кнопки!

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();


        InlineKeyboardButton buttonStableDiffusion = new InlineKeyboardButton();
        buttonStableDiffusion.setText("Stable Diffusion");
        buttonStableDiffusion.setCallbackData("start0");

        InlineKeyboardButton buttonMidJourney = new InlineKeyboardButton();
        buttonMidJourney.setText("MidJourney");
        buttonMidJourney.setCallbackData("start1");

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(buttonStableDiffusion);
        keyboardButtonsRow.add(buttonMidJourney);
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        keyboard.setKeyboard(rowList);


        try {
            execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .parseMode("Markdown")
                            .text(hello + "! Чтобы начать, выберите нейросеть!" +
                                    "Введите /help для справочной информации.")
                            .replyMarkup(keyboard)
                            .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Message command = update.getMessage();
            long chatID = update.getMessage().getChatId();
            String chatIdString = update.getMessage().getChatId().toString();
            String hello = "Привет " + update.getMessage().getChat().getFirstName();
            update.getMessage().getDocument();


            switch (messageText) {
                case "/start":
                    startAnswer(command, hello);
                    break;

                case "/planes":

                    allPlanes(chatID, command);
                    break;

                case "/help":
                    helpAnswer(chatID, command);
                    break;
            }
                      if (update.hasMessage() && update.getMessage().hasText()) {
                    // Обработка текстового сообщения от пользователя
                    String chatId = update.getMessage().getChatId().toString();
                    String userResponse = update.getMessage().getText();

                    // Проверяем, в каком состоянии находится пользователь
                    if (isWaitingForUserInputStableDiffusion(chatId)  && sostoyanie == stableDiffusion) {
                        // Выполняем второй execute
                        String photoUrl = "https://astrafarm.com/images/encyclopedia/ittenVes170221.jpg";
                        InputFile inputFile = new InputFile(photoUrl);

                        try {
                            execute(SendPhoto.builder()
                                    .chatId(chatId)
                                    .parseMode("Markdown")
                                    .photo(inputFile)
                                    .build());
                            stableDiffString(userResponse, command);
                            firstChoiceNeural(command, hello);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        clearNextStep(chatId); // Очищаем состояние пользователя после выполнения второго шага
                    }
                     else if (isWaitingForUserInputMiJourney(chatId) && sostoyanie == MidJourney) {
                        List<String> photoUrls = new ArrayList<>();
                        photoUrls.add("https://blitzpet.ru/wp-content/uploads/2019/04/pochemu-shhenok-ne-nabiraet-ves-na-suhom-korme.jpg");
                        photoUrls.add("https://4lapy.ru/resize/1157x660/upload/iblock/9a1/9a1631bd1a8ece6c278e7b5a685b4549.jpg");
                        photoUrls.add("https://fikiwiki.com/uploads/posts/2022-02/1644866275_1-fikiwiki-com-p-shchenki-krasivie-kartinki-1.jpg");
                        photoUrls.add("https://www.purina.ru/sites/default/files/2022-10/1140_shutterstock_1517123654.jpg");

                        sendMultiplePhotosToUser(chatId, photoUrls);
                        firstChoiceNeural(command, hello);
                    }

            }
        } else if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().equals("start0")) {
                try {
                    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                    sostoyanie = stableDiffusion;

                    execute(SendMessage.builder()
                            .chatId(chatId)
                            .parseMode("Markdown")
                            .text("Вы выбрали Stable Diffusion. Введите запрос того, что хотите сгенерировать.")
                            .allowSendingWithoutReply(true)
                            .build());
  
                    setNextStep(chatId);

                        execute(SendMessage.builder()
                                .chatId(chatId)
                                .parseMode("Markdown")
                                .text("Желательно описанее не менее шести слов")
                                .build());

                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else if (update.getCallbackQuery().getData().equals("start1")) {
                try {
                    String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                    sostoyanie = MidJourney;
                    execute(
                            SendMessage.builder()
                                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                                    .parseMode("Markdown")
                                    .text("Вы выбрали MidJourney. Введите запрос того, что хотите сгенерировать")
                                    .build());
                    setNextStep(chatId);

                    execute(SendMessage.builder()
                            .chatId(chatId)
                            .parseMode("Markdown")
                            .text("Желательно описанее не менее шести слов")
                            .build());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void clearNextStep(String chatId) {
        userStates.remove(chatId);
    }

    private boolean isWaitingForUserInputStableDiffusion(String chatId) {
        return userStates.containsKey(chatId) && userStates.get(chatId);
    }
    private boolean isWaitingForUserInputMiJourney(String chatId) {
        return userStates.containsKey(chatId) && userStates.get(chatId);
    }

    private void setNextStep(String chatId) {
        userStates.put(chatId, true);
    }

    private void sendMultiplePhotosToUser(String chatId, List<String> photoUrls) {
        List<InputMedia> mediaList = new ArrayList<>();
        for (String photoUrl : photoUrls) {
            InputMediaPhoto mediaPhoto = new InputMediaPhoto(photoUrl);
            mediaList.add(mediaPhoto);
        }

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId);
        sendMediaGroup.setMedias(mediaList);

        try {
            execute(sendMediaGroup);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void stableDiffString(String text, Message msg) throws TelegramApiException {
        var chatID = msg.getChatId();
        var query = text;
        var username = msg.getChat().getFirstName();
        StableDiffusionQueryes stableDiffusionEntity = new StableDiffusionQueryes();

        stableDiffusionEntity.setQuery(query);
        stableDiffusionEntity.setUsr(username);

        stableRepo.save(stableDiffusionEntity);
        execute(
                SendMessage.builder()
                        .chatId(msg.getChatId())
                        .chatId(chatID)
                        .parseMode("Markdown")
                        .text("метод отправки в БД stable_diffusion отработал")
                        .build());
    }
    public void sendMessage(Update update, String text){
        try {
            execute(
                    SendMessage.builder()
                            .chatId((update.hasMessage()) ? update.getMessage().getChatId() : update.getCallbackQuery().getFrom().getId())
                            .text(text)
                            .build());
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void startAnswer(Message command, String hello) {
        firstChoiceNeural(command, hello);
    }

    private void allPlanes(long ChatID, Message message) {
        try {
            execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .chatId(ChatID)
                            .parseMode("Markdown")
                            .text("Я ответ на коменду /planes!")
                            .text("Тарифные планы на стадии разработки.")
                            .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void helpAnswer(long ChatID, Message message) {
        try {
            execute(
                    SendMessage.builder()
                            .chatId(message.getChatId())
                            .chatId(ChatID)
                            .parseMode("Markdown")
                            .text("Я ответ на коменду /help!")
                            .text("Выберите одну из двух нейросетей - " +
                                    "stable diffusion (бесплатную) или midjourney.  В сутки доступно 3 бесплатных генерации " +
                                    "изображения. Для ознакомления с платными тарифами - введите команду /planes\n\n" +
                                    " " + HELP_TEXT)
                            .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}