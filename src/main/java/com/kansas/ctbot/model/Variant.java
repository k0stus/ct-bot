package com.kansas.ctbot.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Вариант теста: ключ ответов на конкретное пробное тестирование.
 *
 * Про List vs массив: задания хранятся в List (корректные equals/hashCode, честная
 * иммутабельность через List.copyOf — у "голого" массива в record этого нет: он сравнивается
 * по ссылке, а не по содержимому, и остаётся мутабельным). При этом доступ по номеру задания
 * всё равно O(1) — не через фильтрацию списка при каждом обращении, а через индекс,
 * построенный один раз в конструкторе.
 */
public final class Variant {

    private final String id;
    private final String subject;
    private final List<Question> questions;
    private final Map<Integer, Question> byNumber;

    public Variant(String id, String subject, List<Question> questions) {
        this.id = id;
        this.subject = subject;
        this.questions = List.copyOf(questions);
        this.byNumber = new HashMap<>();
        for (Question q : this.questions) {
            byNumber.put(q.number(), q);
        }
    }

    public String id() {
        return id;
    }

    public String subject() {
        return subject;
    }

    public List<Question> questions() {
        return questions;
    }

    public Question questionByNumber(int number) {
        Question question = byNumber.get(number);
        if (question == null) {
            throw new IllegalArgumentException("В варианте " + id + " нет задания №" + number);
        }
        return question;
    }
}