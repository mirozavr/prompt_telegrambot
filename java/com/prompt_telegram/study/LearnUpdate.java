package com.prompt_telegram.study;

import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.games.Animation;
import org.telegram.telegrambots.meta.api.objects.games.Game;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

public class LearnUpdate {

    public void updateLook(Update update) {
        var message = update.getMessage();
        var a = update.getCallbackQuery();
        update.getCallbackQuery();
        update.getCallbackQuery().getData();
        update.getCallbackQuery().getMessage();
        update.getCallbackQuery().getChatInstance();
        update.getCallbackQuery().getFrom();
        update.getCallbackQuery().getGameShortName();
        update.getCallbackQuery().getId();
        update.getCallbackQuery().getInlineMessageId();
        update.getCallbackQuery().setChatInstance("f");
        update.hasCallbackQuery();
        update.hasMessage();
        // ChannelPost создать нельзя, так как это в классе Update: private Message channelPost; ///< Optional. New incoming channel post of any kind — text, photo, sticker, etc.
        update.getChannelPost();
        update.getChannelPost().getPhoto();
        PhotoSize photoSize = new PhotoSize();
        List<PhotoSize> pS = new ArrayList<>();
        pS.add(photoSize);
        update.getChannelPost().setPhoto(pS); // вот в таком виде setPhoto принимает PhotoSize
        update.getChannelPost().getDocument();
        Document document = new Document();
        update.getChannelPost().setDocument(document);
        update.getChannelPost().getChat();
        Chat chat = new Chat();
        update.getChannelPost().setChat(chat);
        update.getChannelPost().getText();
        update.getChannelPost().setText("ff");
        update.getChannelPost().getForwardFrom();
        User user = new User();
        update.getChannelPost().setForwardFrom(user);
        update.getChannelPost().getChatId();
        // update.getChannelPost().setChatId();  - такого метода нет
        update.getChannelPost().getAnimation();
        Animation animation = new Animation();
        update.getChannelPost().setAnimation(animation);
        update.getChannelPost().getAudio();
        Audio audio = new Audio();
        update.getChannelPost().setAudio(audio);
        update.getChannelPost().getAuthorSignature();
        update.getChannelPost().setAuthorSignature("qq");
        update.getChannelPost().getCaption();
        update.getChannelPost().setCaption("c");
        update.getChannelPost().getCaptionEntities();
        MessageEntity messageEntity = new MessageEntity();
        List<MessageEntity> me = new ArrayList<>();
        update.getChannelPost().setCaptionEntities(me); //принимает на вход список MessageEntity, что логично
        update.getChannelPost().getChannelChatCreated();
        update.getChannelPost().setChannelChatCreated(true); // Принимает boolean ChannelChatCreated
        update.getChannelPost().getConnectedWebsite();
        update.getChannelPost().setConnectedWebsite("www.lya.com");
        update.getChannelPost().getContact();
        Contact contact = new Contact();
        update.getChannelPost().setContact(contact);
        update.getChannelPost().getDate();
        update.getChannelPost().setDate(44444);
        update.getChannelPost().getDeleteChatPhoto();
        update.getChannelPost().setDeleteChatPhoto(false);
        update.getChannelPost().getDice();
        Dice dice = new Dice();  // связано с эмодзи
        update.getChannelPost().setDice(dice);
        update.getChannelPost().getEditDate();
        update.getChannelPost().setEditDate(444);
        update.getChannelPost().getForwardFromMessageId();
        update.getChannelPost().setForwardFromMessageId(987);
        update.getChannelPost().getGame();
        Game game = new Game();
        update.getChannelPost().setGame(game);
        update.getChannelPost().getForwardDate();
        update.getChannelPost().setForwardDate(55);
        update.getChannelPost().getForwardFromChat();
        update.getChannelPost().setForwardFromChat(chat);
        update.getChannelPost().getEntities();
        MessageEntity entityM = new MessageEntity();
        List<MessageEntity> ms = new ArrayList<>();
        ms.add(entityM);
        update.getChannelPost().setEntities(ms);
        update.getChannelPost().getForwardSenderName();
        update.getChannelPost().setForwardSenderName("ffd");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        update.getChannelPost().setReplyMarkup(markup);;
    }
}
