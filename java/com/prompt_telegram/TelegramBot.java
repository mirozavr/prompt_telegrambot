package com.prompt_telegram;

import com.prompt_telegram.entity.StableDiffusionQueryes;
import com.prompt_telegram.repository.StableDiffusionQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    HashMap<Long, ArrayList<String>> mapAnswer = new HashMap<>();
    ArrayList<Long> listOfUsers = new ArrayList<>();

    @Autowired
    private StableDiffusionQueryRepository stableRepo;

    private enum BotState {
        START,
        AWAITING_BUTTON
    }

    private BotState botState = BotState.START;

    private final String url = "jdbc:postgresql://localhost/mypromptgen";
    private final String username = "postgres";
    private final String password = "v";

    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";


    public TelegramBot() {

        List<BotCommand> listOfCommands = new ArrayList(); // BotCommand -  удобный класс телеграм для создания и описания команд.
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
        return "";
    }

    @Override
    public String getBotToken() {
        return "";
    }

    private void sendWithOutURL(Message message, String hello) {  // кнопки!

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

        boolean hasText = update.hasMessage() && update.getMessage().hasText();

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Message command = update.getMessage();
            long chatID = update.getMessage().getChatId();
            String hello = "Привет " + update.getMessage().getChat().getFirstName();
            update.getMessage().getDocument();

            if (messageText.equals("/start")) {
                startAnswer(command, hello);
                botState = BotState.AWAITING_BUTTON;
            }
               else if (botState == BotState.AWAITING_BUTTON) {
             if (update.hasCallbackQuery()) {
                 String callbackData = update.getCallbackQuery().getData();
                    if (callbackData.equals("start0")) {
                        try {
                            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                            execute(SendMessage.builder()
                                    .chatId(chatId)
                                    .parseMode("Markdown")
                                    .text("Вы выбрали Stable Diffusion. Введите запрос того, что хотите сгенерировать.")
                                    .build());

                            sendMessage(update, "я работаю все хорошо");

                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                 else if (callbackData.equals("start1")) {
                     try {
                         String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                         execute(SendMessage.builder()
                                 .chatId(chatId)
                                 .parseMode("Markdown")
                                 .text("Вы выбрали Stable Diffusion. Введите запрос того, что хотите сгенерировать.")
                                 .build());

                         sendMessage(update, "я работаю все хорошо");
                     } catch (TelegramApiException e) {
                         e.printStackTrace();
                     }
                 }
                }
        botState = BotState.START;
            } else if (messageText.equals("/planes")) {
                allPlanes(chatID, command);
            } else if (messageText.equals("/help")) {
                helpAnswer(chatID, command);
            } else if (update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().equals("start0")) {
                    try {
                        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                        execute(SendMessage.builder()
                                .chatId(chatId)
                                .parseMode("Markdown")
                                .text("Вы выбрали Stable Diffusion. Введите запрос того, что хотите сгенерировать.")
                                .build());

                        sendMessage(update, "я работаю все хорошо");

                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                else if (update.getCallbackQuery().getData().equals("start1")) {
                    try {
                        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();

                        execute(SendMessage.builder()
                                .chatId(chatId)
                                .parseMode("Markdown")
                                .text("Вы выбрали Stable Diffusion. Введите запрос того, что хотите сгенерировать.")
                                .build());

                        sendMessage(update, "я работаю все хорошо");
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        execute(
                                SendMessage.builder()
                                        .chatId(command.getChatId())
                                        .parseMode("Markdown")
                                        .text("Пробую работать. Или sorry i don't understand. Я скорее всего выполнюсь в любом случае")
                                        .build());

                        execute(
                                SendMessage.builder()
                                        .chatId(command.getChatId())
                                        .parseMode("Markdown")
                                        .text("Я каким то чудом отработал, кстати привет")
                                        .build());

                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
            }


            }
        }
    }

    private void stableDiffString(Message msg) throws TelegramApiException {
        var chatID = msg.getChatId();
        var query = msg.getText();
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
        sendWithOutURL(command, hello);
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
