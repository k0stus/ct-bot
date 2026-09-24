package com.kansas.ctbot.service;

import com.kansas.ctbot.model.MatchingQuestion;
import com.kansas.ctbot.model.MultipleChoiceQuestion;
import com.kansas.ctbot.model.NumericQuestion;
import com.kansas.ctbot.model.OpenTextQuestion;
import com.kansas.ctbot.model.Question;
import com.kansas.ctbot.model.QuestionResult;
import com.kansas.ctbot.model.SequenceQuestion;
import com.kansas.ctbot.model.StudentAnswer;
import com.kansas.ctbot.model.Variant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Сверяет ответы ученика с ключом варианта. Логика разбора и правило "частично верно"
 * свои для каждого типа задания — см. switch в checkOne. Compiler enforced exhaustiveness:
 * добавите новый тип Question — сюда придётся дописать case, иначе не скомпилируется.
 */
public class AnswerCheckingService {

    private static final Pattern MATCHING_PAIR = Pattern.compile("(\\p{Lu})(\\d+)");

    public List<QuestionResult> check(Variant variant, List<StudentAnswer> studentAnswers) {
        List<QuestionResult> results = new ArrayList<>();
        for (StudentAnswer answer : studentAnswers) {
            Question question = variant.questionByNumber(answer.questionNumber());
            results.add(checkOne(question, answer.rawValue()));
        }
        return results;
    }

    private QuestionResult checkOne(Question question, String rawValue) {
        String value = rawValue == null ? "" : rawValue.trim();
        return switch (question) {
            case MultipleChoiceQuestion q -> checkMultipleChoice(q, value);
            case SequenceQuestion q -> checkSequence(q, value);
            case MatchingQuestion q -> checkMatching(q, value);
            case NumericQuestion q -> checkNumeric(q, value);
            case OpenTextQuestion q -> checkOpenText(q, value);
        };
    }

    private QuestionResult checkMultipleChoice(MultipleChoiceQuestion q, String value) {
        Set<String> given = splitIntoChars(value);
        int errors = symmetricDifferenceSize(q.correctOptions(), given);
        return byErrorCount(q.number(), q.maxScore(), q.partialScore(), errors);
    }

    private QuestionResult checkSequence(SequenceQuestion q, String value) {
        List<String> given = splitIntoCharList(value);
        List<String> correct = q.correctOrder();
        int errors;
        if (given.size() != correct.size()) {
            errors = Math.max(given.size(), correct.size()); // заведомо >1 -> неверно
        } else {
            errors = 0;
            for (int i = 0; i < correct.size(); i++) {
                if (!correct.get(i).equals(given.get(i))) {
                    errors++;
                }
            }
        }
        return byErrorCount(q.number(), q.maxScore(), q.partialScore(), errors);
    }

    private QuestionResult checkMatching(MatchingQuestion q, String value) {
        Map<String, String> given = parseMatchingPairs(value);
        int errors = 0;
        for (Map.Entry<String, String> entry : q.correctMapping().entrySet()) {
            String givenValue = given.get(entry.getKey());
            if (!entry.getValue().equals(givenValue)) {
                errors++;
            }
        }
        return byErrorCount(q.number(), q.maxScore(), q.partialScore(), errors);
    }

    private QuestionResult checkNumeric(NumericQuestion q, String value) {
        String normalized = value.replace(',', '.');
        boolean correct = normalized.equals(q.correctValue().trim());
        return new QuestionResult(q.number(), correct ? q.maxScore() : 0, q.maxScore(),
                correct ? QuestionResult.Verdict.CORRECT : QuestionResult.Verdict.INCORRECT);
    }

    private QuestionResult checkOpenText(OpenTextQuestion q, String value) {
        String normalized = normalizeText(value, q.caseInsensitive(), q.ignoreWhitespace());
        boolean correct = q.acceptableAnswers().stream()
                .map(a -> normalizeText(a, q.caseInsensitive(), q.ignoreWhitespace()))
                .anyMatch(normalized::equals);
        return new QuestionResult(q.number(), correct ? q.maxScore() : 0, q.maxScore(),
                correct ? QuestionResult.Verdict.CORRECT : QuestionResult.Verdict.INCORRECT);
    }

    private QuestionResult byErrorCount(int number, double maxScore, double partialScore, int errors) {
        if (errors == 0) {
            return new QuestionResult(number, maxScore, maxScore, QuestionResult.Verdict.CORRECT);
        }
        if (errors == 1) {
            return new QuestionResult(number, partialScore, maxScore, QuestionResult.Verdict.PARTIAL);
        }
        return new QuestionResult(number, 0, maxScore, QuestionResult.Verdict.INCORRECT);
    }

    private Set<String> splitIntoChars(String value) {
        Set<String> result = new LinkedHashSet<>();
        for (char c : value.toCharArray()) {
            result.add(String.valueOf(c));
        }
        return result;
    }

    private List<String> splitIntoCharList(String value) {
        List<String> result = new ArrayList<>();
        for (char c : value.toCharArray()) {
            result.add(String.valueOf(c));
        }
        return result;
    }

    private Map<String, String> parseMatchingPairs(String value) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = MATCHING_PAIR.matcher(value);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }

    private String normalizeText(String text, boolean caseInsensitive, boolean ignoreWhitespace) {
        String result = text.trim();
        if (ignoreWhitespace) {
            result = result.replaceAll("\\s+", "");
        }
        if (caseInsensitive) {
            result = result.toLowerCase(Locale.ROOT);
        }
        return result;
    }

    private int symmetricDifferenceSize(Set<String> a, Set<String> b) {
        Set<String> union = new LinkedHashSet<>(a);
        union.addAll(b);
        Set<String> intersection = new LinkedHashSet<>(a);
        intersection.retainAll(b);
        union.removeAll(intersection);
        return union.size();
    }
}