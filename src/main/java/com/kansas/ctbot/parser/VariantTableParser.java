package com.kansas.ctbot.parser;

import com.kansas.ctbot.model.QuestionType;
import com.kansas.ctbot.service.AnswerFormats;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Превращает сырые строки таблицы-ключа (от ExcelTableReader) в VariantRow: разбирает
 * и проверяет номер, тип и баллы. Синтаксис "Правильного ответа" и "Доп. параметров"
 * здесь не трогаем — он зависит от типа задания и разбирается уже в VariantFactory.
 *
 * Ожидаемые заголовки столбцов: "№", "Тип", "Правильный ответ", "Макс. балл",
 * "Частично", "Доп. параметры".
 */
public final class VariantTableParser {

    private static final String COL_NUMBER = "№";
    private static final String COL_TYPE = "Тип";
    private static final String COL_ANSWER = "Правильный ответ";
    private static final String COL_MAX_SCORE = "Макс. балл";
    private static final String COL_PARTIAL_SCORE = "Частично";
    private static final String COL_PARAMETERS = "Доп. параметры";

    private final ExcelTableReader tableReader = new ExcelTableReader();

    public List<VariantRow> parse(InputStream xlsxInput) throws IOException {
        List<Map<String, String>> rawRows = tableReader.read(xlsxInput);
        List<VariantRow> rows = new ArrayList<>();
        for (Map<String, String> raw : rawRows) {
            rows.add(toVariantRow(raw));
        }
        return rows;
    }

    private VariantRow toVariantRow(Map<String, String> raw) {
        int number = parseNumber(raw.getOrDefault(COL_NUMBER, ""));
        QuestionType type = parseType(number, raw.getOrDefault(COL_TYPE, ""));
        double maxScore = parseRequiredScore(number, raw.getOrDefault(COL_MAX_SCORE, ""), COL_MAX_SCORE);
        Double partialScore = parseOptionalScore(number, raw.getOrDefault(COL_PARTIAL_SCORE, ""), COL_PARTIAL_SCORE);

        return new VariantRow(
                number,
                type,
                raw.getOrDefault(COL_ANSWER, "").trim(),
                maxScore,
                partialScore,
                raw.getOrDefault(COL_PARAMETERS, "").trim()
        );
    }

    private int parseNumber(String rawValue) {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException e) {
            throw new VariantParsingException("Некорректный номер задания: \"" + rawValue + "\"");
        }
    }

    private QuestionType parseType(int number, String rawValue) {
        try {
            return QuestionType.fromLabel(rawValue);
        } catch (IllegalArgumentException e) {
            throw new VariantParsingException(number, e.getMessage());
        }
    }

    private double parseRequiredScore(int number, String rawValue, String columnName) {
        if (rawValue.isBlank()) {
            throw new VariantParsingException(number, "не указан столбец \"" + columnName + "\"");
        }
        try {
            return Double.parseDouble(AnswerFormats.normalizeNumeric(rawValue));
        } catch (NumberFormatException e) {
            throw new VariantParsingException(number,
                    "не удалось прочитать \"" + columnName + "\": \"" + rawValue + "\"");
        }
    }

    private Double parseOptionalScore(int number, String rawValue, String columnName) {
        if (rawValue.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(AnswerFormats.normalizeNumeric(rawValue));
        } catch (NumberFormatException e) {
            throw new VariantParsingException(number,
                    "не удалось прочитать \"" + columnName + "\": \"" + rawValue + "\"");
        }
    }
}