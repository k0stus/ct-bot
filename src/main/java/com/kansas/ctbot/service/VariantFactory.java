package com.kansas.ctbot.service;

import com.kansas.ctbot.model.MatchingQuestion;
import com.kansas.ctbot.model.MultipleChoiceQuestion;
import com.kansas.ctbot.model.NumericQuestion;
import com.kansas.ctbot.model.OpenTextQuestion;
import com.kansas.ctbot.model.Question;
import com.kansas.ctbot.model.SequenceQuestion;
import com.kansas.ctbot.model.Variant;
import com.kansas.ctbot.parser.VariantParsingException;
import com.kansas.ctbot.parser.VariantRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Строит Variant из уже провалидированных VariantRow. Не знает и не заботится о том,
 * как эти строки были получены (Excel, Google Sheets, руками в тесте) — только
 * доменная логика: по QuestionType выбрать нужный тип Question и разобрать
 * "Правильный ответ"/"Доп. параметры" через AnswerFormats.
 */
public final class VariantFactory {

    public Variant create(String variantId, String subject, List<VariantRow> rows) {
        List<Question> questions = new ArrayList<>();
        for (VariantRow row : rows) {
            questions.add(toQuestion(row));
        }
        return new Variant(variantId, subject, questions);
    }

    private Question toQuestion(VariantRow row) {
        return switch (row.type()) {
            case MULTIPLE_CHOICE -> new MultipleChoiceQuestion(
                    row.number(),
                    AnswerFormats.splitIntoChars(row.correctAnswer()),
                    row.maxScore(),
                    partialOrZero(row)
            );
            case SEQUENCE -> new SequenceQuestion(
                    row.number(),
                    AnswerFormats.splitIntoCharList(row.correctAnswer()),
                    row.maxScore(),
                    partialOrZero(row)
            );
            case MATCHING -> new MatchingQuestion(
                    row.number(),
                    AnswerFormats.parseMatchingPairs(row.correctAnswer()),
                    row.maxScore(),
                    partialOrZero(row)
            );
            case NUMERIC -> new NumericQuestion(
                    row.number(),
                    AnswerFormats.normalizeNumeric(row.correctAnswer()),
                    row.maxScore()
            );
            case OPEN_TEXT -> toOpenTextQuestion(row);
        };
    }

    private double partialOrZero(VariantRow row) {
        // Пустая ячейка "Частично" для типа с частичной верностью = 0 баллов за неё,
        // а не "неизвестно" — так безопаснее, чем NPE при автоунбоксинге.
        return row.partialScore() == null ? 0 : row.partialScore();
    }

    private OpenTextQuestion toOpenTextQuestion(VariantRow row) {
        Set<String> acceptable = new LinkedHashSet<>();
        for (String option : row.correctAnswer().split("\\|")) {
            String trimmed = option.trim();
            if (!trimmed.isEmpty()) {
                acceptable.add(trimmed);
            }
        }
        if (acceptable.isEmpty()) {
            throw new VariantParsingException(row.number(), "не указан правильный ответ для текстового задания");
        }

        Map<String, String> params = parseParameters(row.parameters());
        boolean caseInsensitive = "нет".equalsIgnoreCase(params.get("регистр"));
        boolean ignoreWhitespace = "игнор".equalsIgnoreCase(params.get("пробелы"));
        return new OpenTextQuestion(row.number(), acceptable, row.maxScore(), caseInsensitive, ignoreWhitespace);
    }

    private Map<String, String> parseParameters(String parameters) {
        Map<String, String> params = new HashMap<>();
        if (parameters == null || parameters.isBlank()) {
            return params;
        }
        for (String part : parameters.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0].trim().toLowerCase(Locale.ROOT), kv[1].trim());
            }
        }
        return params;
    }
}