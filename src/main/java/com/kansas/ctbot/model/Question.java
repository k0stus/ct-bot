package com.kansas.ctbot.model;

/**
 * Задание варианта теста. У заданий ЦТ/РТ разная природа ответа, поэтому вместо одной
 * универсальной структуры используется sealed-иерархия: каждый тип задания сам определяет,
 * что такое "правильный ответ" и что считается частично верным.
 *
 * Благодаря sealed + exhaustive switch (см. AnswerCheckingService) компилятор не даст забыть
 * обработать новый тип задания, если он появится в будущем.
 */
public sealed interface Question
        permits MultipleChoiceQuestion,
        SequenceQuestion,
        MatchingQuestion,
        NumericQuestion,
        OpenTextQuestion
{
    int number();
    double maxScore();
}