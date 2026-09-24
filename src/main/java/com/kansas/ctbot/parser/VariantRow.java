package com.kansas.ctbot.parser;

import com.kansas.ctbot.model.QuestionType;

/**
 * Одна строка таблицы-ключа, уже структурированная и провалидированная парсером:
 * номер, тип и баллы разобраны и проверены. VariantFactory строит из этого Question,
 * не зная и не заботясь о том, как эта строка была получена (Excel, Google Sheets —
 * не важно).
 *
 * @param correctAnswer правильный ответ как строка — синтаксис зависит от типа задания
 *                      и разбирается уже в VariantFactory через AnswerFormats
 * @param partialScore  баллы за частично верный ответ; null, если ячейка пуста —
 *                      для типов без частичной верности это ожидаемо и не ошибка
 * @param parameters    доп. параметры (сейчас используются только для текстовых
 *                      заданий) — сырые, разбираются в VariantFactory
 */
public record VariantRow(
        int number,
        QuestionType type,
        String correctAnswer,
        double maxScore,
        Double partialScore,
        String parameters
) {}