package com.kansas.ctbot.parser;

import com.kansas.ctbot.model.StudentAnswer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Превращает таблицу ответов ученика (та же форма, что и ключ репетитора, но без
 * столбцов "Тип"/баллы) в список StudentAnswer. Никакой типизации ответа здесь не
 * происходит — сырую строку интерпретирует уже AnswerCheckingService, зная тип задания
 * из Variant.
 *
 * Ожидаемые заголовки столбцов: "№", "Ответ".
 */
public final class StudentAnswerTableParser {

    private static final String COL_NUMBER = "№";
    private static final String COL_ANSWER = "Ответ";

    private final ExcelTableReader tableReader = new ExcelTableReader();

    public List<StudentAnswer> parse(InputStream xlsxInput) throws IOException {
        List<Map<String, String>> rawRows = tableReader.read(xlsxInput);
        List<StudentAnswer> answers = new ArrayList<>();
        for (Map<String, String> raw : rawRows) {
            answers.add(toStudentAnswer(raw));
        }
        return answers;
    }

    private StudentAnswer toStudentAnswer(Map<String, String> raw) {
        String rawNumber = raw.getOrDefault(COL_NUMBER, "");
        int number;
        try {
            number = Integer.parseInt(rawNumber.trim());
        } catch (NumberFormatException e) {
            throw new VariantParsingException("Некорректный номер задания в ответах ученика: \"" + rawNumber + "\"");
        }
        return new StudentAnswer(number, raw.getOrDefault(COL_ANSWER, "").trim());
    }
}