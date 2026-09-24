package com.kansas.ctbot.model;

import java.util.Map;

/**
 * "Абвгдейка" — сопоставление букв (А, Б, В, Г...) с номерами вариантов.
 * Ответ приходит строкой вида "А1Б1В2Г3". Значения (цифры) могут повторяться
 * для разных букв — это не ошибка.
 * Ошибка ровно в одной букве — частично верно, в двух и более — неверно.
 *
 * @param correctMapping буква -> правильный номер варианта, например {"А":"1","Б":"1","В":"2","Г":"3"}
 */
public record MatchingQuestion(
        int number,
        Map<String, String> correctMapping,
        double maxScore,
        double partialScore
) implements Question {}