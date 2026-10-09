package com.kansas.ctbot.service;

import com.kansas.ctbot.model.ScoreScale;

/**
 * Переводит первичный балл (сумму, посчитанную AnswerCheckingService) в тестовый —
 * по переданной ScoreScale. Не знает и не заботится о том, откуда взялась шкала
 * (встроенная таблица РИКЗ или своя, загруженная позже с диска).
 */
public final class ScoreConversionService {

    public int convert(ScoreScale scale, double rawScore) {
        return scale.convert(roundToPrimaryScore(rawScore));
    }

    private int roundToPrimaryScore(double rawScore) {
        return (int) Math.round(rawScore);
    }
}