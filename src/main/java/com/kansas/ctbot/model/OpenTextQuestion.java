package com.kansas.ctbot.model;

import java.util.Set;

/**
 * Открытый вопрос: словом/термином/сокращением. Частичной верности не бывает.
 *
 * @param acceptableAnswers допустимые варианты ответа (синонимы/сокращения) — если ответ
 *                          совпал хотя бы с одним, засчитывается как верный
 * @param caseInsensitive   игнорировать регистр
 * @param ignoreWhitespace  игнорировать пробелы
 */
public record OpenTextQuestion(
        int number,
        Set<String> acceptableAnswers,
        double maxScore,
        boolean caseInsensitive,
        boolean ignoreWhitespace
) implements Question {}