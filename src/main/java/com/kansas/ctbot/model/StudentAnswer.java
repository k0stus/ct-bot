package com.kansas.ctbot.model;

/**
 * Ответ ученика на одно задание — как он пришёл (ячейка Google Sheets / Excel), без разбора.
 * Разбор формата зависит от типа задания, поэтому происходит внутри AnswerCheckingService,
 * а не здесь.
 */
public record StudentAnswer(int questionNumber, String rawValue) {}


