package com.kansas.ctbot.model;

/**
 * Задача с числовым ответом, округлённым до указанного в задании разряда.
 * Частичной верности не бывает: либо точное совпадение, либо ошибка.
 *
 * @param correctValue правильное значение как строка в ожидаемом формате, например "3.1"
 */
public record NumericQuestion(
        int number,
        String correctValue,
        double maxScore
) implements Question {}