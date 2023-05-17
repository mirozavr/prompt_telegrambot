package com.prompt_telegram;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static java.lang.Thread.sleep;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private Map<String, Boolean> userStates = new HashMap<>();
    private static final int stableDiffusion = 1;
    private static final int MidJourney = 2;
    private static final int click5ReqStable = 3;
    private static final int click5ReqMj = 4;

    private static Message commandHelper;
    private static String helloHelper;
    private static int sostoyanie;

   // @Autowired
   // private StableDiffusionQueryRepository stableRepo;
    private final String url = "jdbc:postgresql://localhost/mypromptgen";
    private final String usernameBD = "postgres";
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

        InlineKeyboardButton buttonLastFiveRequestsStableDiff = new InlineKeyboardButton();
        buttonLastFiveRequestsStableDiff.setText("Last 5 requests SD");
        buttonLastFiveRequestsStableDiff.setCallbackData("fiveStable");

        InlineKeyboardButton buttonLastFiveRequestsMJ = new InlineKeyboardButton();
        buttonLastFiveRequestsMJ.setText("Last 5 requests MJ");
        buttonLastFiveRequestsMJ.setCallbackData("fiveMJ");

        List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
        keyboardButtonsRow.add(buttonStableDiffusion);
        keyboardButtonsRow.add(buttonMidJourney);

        List<InlineKeyboardButton> keyboardButtons5Lists = new ArrayList<>();
        keyboardButtons5Lists.add(buttonLastFiveRequestsStableDiff);
        keyboardButtons5Lists.add(buttonLastFiveRequestsMJ);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow);
        rowList.add(keyboardButtons5Lists);
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
            staticHelper(command, hello);


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
                     else if (sender5LastStableDiffusion(chatId) && sostoyanie == click5ReqStable) {
                         firstChoiceNeural(command, hello);
                        clearNextStep(chatId);

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
                            .text("Желательно описание не менее шести слов")
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
                            .text("Желательно описание не менее шести слов")
                            .build());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if (update.getCallbackQuery().getData().equals("fiveStable")) {
                sostoyanie = click5ReqStable;
                String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                try {
                    getLastFiveEntriesStableDiff(chatId);
                    setNextStep(chatId);
                    startAnswer(commandHelper, helloHelper);
                }
                catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void staticHelper(Message command, String hello) {
        commandHelper = command;
        helloHelper = hello;
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

    private boolean sender5LastStableDiffusion(String chatId) {
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
        String query = text;
        String fistName = msg.getChat().getFirstName();
        String userName = msg.getChat().getUserName();
        long chatId = msg.getChat().getId();

        try (java.sql.Connection connection = DriverManager.getConnection(url, usernameBD, password)) {
            String insertQuery = "INSERT INTO stable_diffusion (first_name, query, user_name, chat_id) VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {

                statement.setString(1, fistName);
                statement.setString(2, query);
                statement.setString(3, userName);
                statement.setLong(4, chatId);
                statement.executeUpdate();

            }

            execute(
                    SendMessage.builder()
                            .chatId(msg.getChatId())
                            .parseMode("Markdown")
                            .text("Метод сохранения в БД `stable_diffusion` отработал")
                            .build());
        } catch (TelegramApiException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void getLastFiveEntriesStableDiff(String chatId) throws TelegramApiException {
        try (java.sql.Connection connection = DriverManager.getConnection(url, usernameBD, password)) {
            String selectQuery = "SELECT first_name, query FROM stable_diffusion ORDER BY id DESC LIMIT 5";

            try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {

                ResultSet resultSet = statement.executeQuery();

                StringBuilder messageText = new StringBuilder("Ваши последние записи:\n");

                while (resultSet.next()) {
                    String firstName = resultSet.getString("first_name");
                    String query = resultSet.getString("query");
                    execute(
                            SendMessage.builder()
                                    .chatId(chatId)
                                    .parseMode("Markdown")
                                    .text(query)
                                    .build());
                    messageText.append("- ").append(firstName).append(": ").append(query).append("\n");
                }

                execute(
                        SendMessage.builder()
                                .chatId(chatId)
                                .parseMode("Markdown")
                                .text(messageText.toString())
                                .build());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        clearNextStep(chatId);
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

    public void jsoupDiffusion(String request) {
        try {
            // Отправка GET-запроса и получение содержимого страницы
            Connection.Response response = Jsoup.connect("https://stablediffusionweb.com/#demo.html")
                    .method(Connection.Method.GET)
                    .execute();
            Document document = response.parse();

            // Нахождение и очистка поля ввода
            Element input = document.selectFirst("html body gradio-app div div:nth-child(2) div div div:nth-child(2) div:nth-child(1) div div div label input");
            input.val("");

            // Ввод нужной строки
            input.val(request);

            // Нахождение кнопки и имитация нажатия
            Element button = document.selectFirst("html body gradio-app div div:nth-child(2) div div div:nth-child(2) div:nth-child(1) div button");

            // Получение необходимых параметров для отправки POST-запроса
            String url = "https://stablediffusionweb.com/#demo.html";
            String action = button.attr("formaction");
            String method = button.attr("formmethod");

            // Подготовка данных для POST-запроса
            Map<String, String> postData = new HashMap<>();
            Elements inputElements = document.select("input");
            for (Element inputElement : inputElements) {
                String name = inputElement.attr("name");
                String value = inputElement.val();
                postData.put(name, value);
            }

            // Отправка POST-запроса с имитацией нажатия кнопки
            Connection.Response postResponse = Jsoup.connect(url)
                    .data(postData)
                    .method(Connection.Method.valueOf(method))
                    .execute();

            Document postDocument = postResponse.parse();

            // Подождать 10 секунд для загрузки данных
            Thread.sleep(10000);

            // Получение данных из указанного элемента и сохранение как картинка
            Element svgElement = postDocument.selectFirst("html body gradio-app div div:nth-child(2) div div div:nth-child(2) div:nth-child(2) div:nth-child(2) div div svg");
            // Здесь вы можете обработать svgElement, чтобы сохранить его как картинку с помощью других библиотек, таких как Batik или Apache PDFBox.
            // Для простоты примера я просто выведу его содержимое на консоль.
            System.out.println(svgElement.outerHtml());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

   /* public void seleniumDiffusion(String request) {
        ChromeOptions options = new ChromeOptions();

        options.addArguments("-no-sandbox");
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        WebDriver driver = new ChromeDriver(options);
        driver.get("https://stablediffusionweb.com/#demo.html");

        WebElement input = driver.findElement(By.xpath("/html/body/gradio-app/div/div[2]/div/div/div[2]/div[1]/div/div/div/label/input"));
        input.clear();
        input.sendKeys(request);
        input.sendKeys(Keys.ENTER);
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/

}




















