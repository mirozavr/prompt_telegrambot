package com.prompt_telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class PrompttelegramApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrompttelegramApplication.class, args);
	}
	TelegramBot bot = new TelegramBot();
	TelegramBotsApi telegramBot;

	{
		try {
			telegramBot = new TelegramBotsApi(DefaultBotSession.class);
			telegramBot.registerBot(bot);
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

}
