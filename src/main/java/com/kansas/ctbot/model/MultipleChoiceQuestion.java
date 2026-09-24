package com.kansas.ctbot.model;

import java.util.Set;

/**
 * Задание с множественным выбором (как в А-части, но верных вариантов может быть несколько).
 * Ошибка в одном варианте (лишний выбранный или пропущенный верный) — частично верно,
 * в двух и более — неверно.
 *
 * @param correctOptions правильные варианты, например {"1", "3", "5"}
 */
public record MultipleChoiceQuestion(
        int number,
        Set<String> correctOptions,
        double maxScore,
        double partialScore
) implements Question {}
