package com.prompt_telegram.study;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsoupCheck {

    public static void main(String[] args) {
        JsoupCheck jS = new JsoupCheck();

        System.out.println(jS.jsoupDiffusion("Волк"));
    }

    public String jsoupDiffusion(String request) {
        try {
            // Отправка GET-запроса и получение содержимого страницы
            Connection.Response response = Jsoup.connect("https://stablediffusionweb.com/#demo")
                    .timeout(10000)
                    .method(Connection.Method.GET)
                    .execute();

            Document document = response.parse();

            Element inputField = document.selectFirst("input[data-testid='textbox']");
            if (inputField != null) {
                inputField.val(request);
            }

            // Нахождение кнопки и имитация нажатия
            // Element button = document.selectFirst("#component-6");
            document.selectFirst("#component-6");

            // Подождать 10 секунд
            Thread.sleep(12000);

            Element imageElement = document.selectFirst("#gallery");

            if (imageElement != null) {
                String imageUrl = imageElement.attr("src");
                downloadImage(imageUrl); // Скачивание изображения
                return imageUrl;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void downloadImage(String imageUrl) throws IOException {
        String fileName = "image.jpg"; // Имя файла для сохранения изображения
        try (InputStream in = Jsoup.connect(imageUrl).ignoreContentType(true).execute().bodyStream();
             OutputStream out = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Image downloaded successfully!");
    }
}
