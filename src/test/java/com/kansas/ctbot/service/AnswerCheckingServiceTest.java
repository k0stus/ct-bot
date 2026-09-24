package com.kansas.ctbot.service;

import com.kansas.ctbot.model.Question;
import com.kansas.ctbot.model.QuestionResult;
import com.kansas.ctbot.model.StudentAnswer;
import com.kansas.ctbot.model.Variant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnswerCheckingServiceTest {

    private final AnswerCheckingService service = new AnswerCheckingService();

    @Test
    void singleElementExactMatch_isCorrect() {
        Question q = new Question(1, List.of("42"), 1, 0);
        Variant variant = new Variant("v1", "math", List.of(q));
        StudentAnswer answer = new StudentAnswer(1, List.of("42"));

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.CORRECT, results.get(0).verdict());
        assertEquals(1, results.get(0).scoreEarned());
    }

    @Test
    void singleElementWrongAnswer_isIncorrect() {
        Question q = new Question(2, List.of("42"), 1, 0);
        Variant variant = new Variant("v1", "math", List.of(q));
        StudentAnswer answer = new StudentAnswer(2, List.of("17"));

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.INCORRECT, results.get(0).verdict());
        assertEquals(0, results.get(0).scoreEarned());
    }

    @Test
    void multiElementExactMatch_isFullyCorrect() {
        Question q = new Question(3, List.of("1", "2", "5"), 2, 1);
        Variant variant = new Variant("v1", "biology", List.of(q));
        StudentAnswer answer = new StudentAnswer(3, List.of("1", "2", "5"));

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.CORRECT, results.get(0).verdict());
        assertEquals(2, results.get(0).scoreEarned());
    }

    @Test
    void multiElementOneWrongElement_isPartial() {
        Question q = new Question(4, List.of("1", "2", "5"), 2, 1);
        Variant variant = new Variant("v1", "biology", List.of(q));
        StudentAnswer answer = new StudentAnswer(4, List.of("1", "2", "6")); // "6" вместо "5"

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.PARTIAL, results.get(0).verdict());
        assertEquals(1, results.get(0).scoreEarned());
    }

    @Test
    void multiElementMissingOneElement_isPartial() {
        Question q = new Question(5, List.of("1", "2", "5"), 2, 1);
        Variant variant = new Variant("v1", "biology", List.of(q));
        StudentAnswer answer = new StudentAnswer(5, List.of("1", "2")); // пропущен "5"

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.PARTIAL, results.get(0).verdict());
        assertEquals(1, results.get(0).scoreEarned());
    }

    @Test
    void multiElementTwoWrong_isIncorrect() {
        Question q = new Question(6, List.of("1", "2", "5"), 2, 1);
        Variant variant = new Variant("v1", "biology", List.of(q));
        StudentAnswer answer = new StudentAnswer(6, List.of("3", "4", "5")); // два элемента неверны

        List<QuestionResult> results = service.check(variant, List.of(answer));

        assertEquals(QuestionResult.Verdict.INCORRECT, results.get(0).verdict());
        assertEquals(0, results.get(0).scoreEarned());
    }

    @Test
    void unknownQuestionNumber_throwsIllegalArgumentException() {
        Variant variant = new Variant("v1", "math", List.of(new Question(1, List.of("1"), 1, 0)));
        StudentAnswer answer = new StudentAnswer(99, List.of("1"));

        assertThrows(IllegalArgumentException.class, () -> service.check(variant, List.of(answer)));
    }
}