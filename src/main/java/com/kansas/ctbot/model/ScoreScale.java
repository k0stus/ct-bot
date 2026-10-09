package com.kansas.ctbot.model;

import java.util.Arrays;
import java.util.Map;

/**
 * Таблица соответствия первичного и тестового баллов для одного предмета/года.
 * Первичные баллы должны идти без пропусков от 0 до максимума — это проверяется
 * при создании, чтобы таблица с дырой не проявила себя тихой ошибкой при сверке
 * ответов реального ученика.
 */
public final class ScoreScale {

    private final String subject;
    private final int year;
    private final int[] table; // индекс = первичный балл, значение = тестовый

    public ScoreScale(String subject, int year, Map<Integer, Integer> primaryToConverted) {
        this.subject = subject;
        this.year = year;

        int maxPrimary = primaryToConverted.keySet().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElseThrow(() -> new IllegalArgumentException("Таблица перевода баллов пуста"));

        this.table = new int[maxPrimary + 1];
        Arrays.fill(table, -1);

        for (Map.Entry<Integer, Integer> entry : primaryToConverted.entrySet()) {
            int primary = entry.getKey();
            if (primary < 0) {
                throw new IllegalArgumentException("Первичный балл не может быть отрицательным: " + primary);
            }
            table[primary] = entry.getValue();
        }

        for (int primary = 0; primary <= maxPrimary; primary++) {
            if (table[primary] < 0) {
                throw new IllegalArgumentException(
                        "В таблице (" + subject + " " + year + ") пропущен первичный балл " + primary);
            }
        }
    }

    public String subject() {
        return subject;
    }

    public int year() {
        return year;
    }

    public int maxPrimaryScore() {
        return table.length - 1;
    }

    public int convert(int primaryScore) {
        if (primaryScore < 0 || primaryScore >= table.length) {
            throw new IllegalArgumentException(
                    "Первичный балл " + primaryScore + " вне диапазона таблицы 0.." + maxPrimaryScore()
                            + " (" + subject + " " + year + ")");
        }
        return table[primaryScore];
    }
}