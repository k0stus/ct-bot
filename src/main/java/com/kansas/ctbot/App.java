package com.kansas.ctbot;

import com.kansas.ctbot.model.QuestionResult;
import com.kansas.ctbot.model.StudentAnswer;
import com.kansas.ctbot.model.Variant;
import com.kansas.ctbot.parser.VariantParsingException;
import com.kansas.ctbot.parser.VariantRow;
import com.kansas.ctbot.parser.VariantTableParser;
import com.kansas.ctbot.parser.StudentAnswerTableParser;
import com.kansas.ctbot.service.AnswerCheckingService;
import com.kansas.ctbot.service.VariantFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Консольная утилита: собирает уже написанные слои воедино и проверяет ответы одного
 * ученика по одному варианту. Полезна как проверяемый end-to-end срез ещё до Telegram-слоя.
 *
 * Использование:
 *   java -jar ct-bot.jar <ключ_варианта.xlsx> <ответы_ученика.xlsx> [имя ученика]
 *
 * ВАЖНО: сейчас сверяются только те номера заданий, которые есть в файле ученика.
 * Если ученик пропустил задание (не заполнил строку) — оно просто не попадёт в отчёт,
 * а не будет засчитано как 0 баллов. Это стоит поправить перед реальным использованием,
 * когда дело дойдёт до сборки отчёта для реального ученика.
 */
public final class App {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Использование: java -jar ct-bot.jar <ключ_варианта.xlsx> <ответы_ученика.xlsx> [имя ученика]");
            System.exit(1);
            return;
        }

        String keyPath = args[0];
        String answersPath = args[1];
        String studentName = args.length >= 3 ? args[2] : "Ученик";

        try {
            Variant variant = loadVariant(keyPath);
            List<StudentAnswer> answers = loadAnswers(answersPath);

            AnswerCheckingService checkingService = new AnswerCheckingService();
            List<QuestionResult> results = checkingService.check(variant, answers);

            printReport(studentName, variant, results);
        } catch (VariantParsingException e) {
            System.err.println("Ошибка в файле: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            // Например, ученик ответил на номер задания, которого нет в варианте.
            System.err.println("Ошибка сверки: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Не удалось прочитать файл: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Variant loadVariant(String path) throws IOException {
        VariantTableParser tableParser = new VariantTableParser();
        VariantFactory factory = new VariantFactory();
        String variantId = Path.of(path).getFileName().toString();

        try (InputStream in = new FileInputStream(path)) {
            List<VariantRow> rows = tableParser.parse(in);
            return factory.create(variantId, "", rows);
        }
    }

    private static List<StudentAnswer> loadAnswers(String path) throws IOException {
        StudentAnswerTableParser parser = new StudentAnswerTableParser();
        try (InputStream in = new FileInputStream(path)) {
            return parser.parse(in);
        }
    }

    private static void printReport(String studentName, Variant variant, List<QuestionResult> results) {
        System.out.println("Вариант: " + variant.id());
        System.out.println("Ученик: " + studentName);
        System.out.println();

        double totalEarned = 0;
        double totalMax = 0;

        for (QuestionResult result : results) {
            totalEarned += result.scoreEarned();
            totalMax += result.maxScore();
            System.out.printf(Locale.ROOT, "№%-3d %-10s %s / %s%n",
                    result.questionNumber(),
                    verdictLabel(result.verdict()),
                    formatScore(result.scoreEarned()),
                    formatScore(result.maxScore()));
        }

        System.out.println();
        System.out.printf(Locale.ROOT, "Первичный балл: %s из %s%n",
                formatScore(totalEarned), formatScore(totalMax));
    }

    private static String verdictLabel(QuestionResult.Verdict verdict) {
        return switch (verdict) {
            case CORRECT -> "верно";
            case PARTIAL -> "частично";
            case INCORRECT -> "неверно";
        };
    }

    private static String formatScore(double value) {
        if (value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}