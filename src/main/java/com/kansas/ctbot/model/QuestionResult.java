package com.kansas.ctbot.model;

/**
 * Результат сверки одного задания.
 *
 * @param questionNumber номер задания
 * @param scoreEarned    полученные первичные баллы
 * @param maxScore       максимум баллов за задание
 * @param verdict        вердикт: верно / частично верно / неверно
 */
public record QuestionResult(
        int questionNumber,
        double scoreEarned,
        double maxScore,
        Verdict verdict
) {
    public enum Verdict { CORRECT, PARTIAL, INCORRECT }
}
