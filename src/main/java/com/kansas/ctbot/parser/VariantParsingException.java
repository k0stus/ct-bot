package com.kansas.ctbot.parser;

/**
 * Ошибка в содержимом таблицы-ключа или ответов ученика — то есть человек ошибся
 * при заполнении, а не баг в коде. Сообщение рассчитано на то, чтобы его можно было
 * почти без изменений показать в ответе бота.
 */
public class VariantParsingException extends RuntimeException {

    public VariantParsingException(String message) {
        super(message);
    }

    public VariantParsingException(int questionNumber, String message) {
        super("Задание №" + questionNumber + ": " + message);
    }
}