package com.kansas.ctbot.service;

import com.kansas.ctbot.model.MatchingQuestion;
import com.kansas.ctbot.model.MultipleChoiceQuestion;
import com.kansas.ctbot.model.NumericQuestion;
import com.kansas.ctbot.model.OpenTextQuestion;
import com.kansas.ctbot.model.QuestionResult;
import com.kansas.ctbot.model.SequenceQuestion;
import com.kansas.ctbot.model.StudentAnswer;
import com.kansas.ctbot.model.Variant;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnswerCheckingServiceTest {

    private final AnswerCheckingService service = new AnswerCheckingService();

    @Nested
    class MultipleChoice {

        @Test
        void exactMatch_isCorrect() {
            var q = new MultipleChoiceQuestion(1, Set.of("1", "3", "5"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 1, "135");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
            assertEquals(2, result.scoreEarned());
        }

        @Test
        void oneExtraOption_isPartial() {
            var q = new MultipleChoiceQuestion(2, Set.of("1", "3", "5"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 2, "1345"); // лишняя "4"

            assertEquals(QuestionResult.Verdict.PARTIAL, result.verdict());
            assertEquals(1, result.scoreEarned());
        }

        @Test
        void oneMissingOption_isPartial() {
            var q = new MultipleChoiceQuestion(3, Set.of("1", "3", "5"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 3, "13"); // пропущена "5"

            assertEquals(QuestionResult.Verdict.PARTIAL, result.verdict());
        }

        @Test
        void twoErrors_isIncorrect() {
            var q = new MultipleChoiceQuestion(4, Set.of("1", "3", "5"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 4, "24");

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
            assertEquals(0, result.scoreEarned());
        }
    }

    @Nested
    class Sequence {

        @Test
        void exactOrder_isCorrect() {
            var q = new SequenceQuestion(1, List.of("1", "3", "5", "2"), 2, 1);
            var variant = new Variant("v1", "chemistry", List.of(q));
            var result = check(variant, 1, "1352");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void onePositionSwapped_isPartial() {
            var q = new SequenceQuestion(2, List.of("1", "3", "5", "2"), 2, 1);
            var variant = new Variant("v1", "chemistry", List.of(q));
            var result = check(variant, 2, "1325"); // последние два переставлены

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
        }

        @Test
        void wrongLength_isIncorrect() {
            var q = new SequenceQuestion(3, List.of("1", "3", "5", "2"), 2, 1);
            var variant = new Variant("v1", "chemistry", List.of(q));
            var result = check(variant, 3, "135");

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
        }
    }

    @Nested
    class Matching {

        @Test
        void allLettersCorrect_isCorrect() {
            var q = new MatchingQuestion(1, Map.of("А", "1", "Б", "1", "В", "2", "Г", "3"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 1, "А1Б1В2Г3");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void oneLetterWrong_isPartial() {
            var q = new MatchingQuestion(2, Map.of("А", "1", "Б", "1", "В", "2", "Г", "3"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 2, "А1Б2В2Г3"); // Б должно быть 1

            assertEquals(QuestionResult.Verdict.PARTIAL, result.verdict());
        }

        @Test
        void twoLettersWrong_isIncorrect() {
            var q = new MatchingQuestion(3, Map.of("А", "1", "Б", "1", "В", "2", "Г", "3"), 2, 1);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 3, "А2Б2В2Г3");

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
        }
    }

    @Nested
    class Numeric {

        @Test
        void exactMatch_isCorrect() {
            var q = new NumericQuestion(1, "3.1", 2);
            var variant = new Variant("v1", "physics", List.of(q));
            var result = check(variant, 1, "3.1");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void commaInsteadOfDot_isStillCorrect() {
            var q = new NumericQuestion(2, "3.1", 2);
            var variant = new Variant("v1", "physics", List.of(q));
            var result = check(variant, 2, "3,1");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void wrongRounding_isIncorrect_noPartial() {
            var q = new NumericQuestion(3, "3.1", 2);
            var variant = new Variant("v1", "physics", List.of(q));
            var result = check(variant, 3, "3.10"); // не тот разряд округления

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
            assertEquals(0, result.scoreEarned());
        }
    }

    @Nested
    class OpenText {

        @Test
        void exactMatch_isCorrect() {
            var q = new OpenTextQuestion(1, Set.of("митохондрия"), 1, false, false);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 1, "митохондрия");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void caseInsensitive_isCorrect() {
            var q = new OpenTextQuestion(2, Set.of("митохондрия"), 1, true, false);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 2, "Митохондрия");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void ignoreWhitespace_isCorrect() {
            var q = new OpenTextQuestion(3, Set.of("хлоридионы"), 1, false, true);
            var variant = new Variant("v1", "chemistry", List.of(q));
            var result = check(variant, 3, "хлорид ионы");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void synonymAccepted() {
            var q = new OpenTextQuestion(4, Set.of("NaCl", "хлорид натрия"), 1, true, true);
            var variant = new Variant("v1", "chemistry", List.of(q));
            var result = check(variant, 4, "хлоридНАТРИЯ");

            assertEquals(QuestionResult.Verdict.CORRECT, result.verdict());
        }

        @Test
        void noMatch_isIncorrect() {
            var q = new OpenTextQuestion(5, Set.of("митохондрия"), 1, false, false);
            var variant = new Variant("v1", "biology", List.of(q));
            var result = check(variant, 5, "рибосома");

            assertEquals(QuestionResult.Verdict.INCORRECT, result.verdict());
        }
    }

    @Test
    void unknownQuestionNumber_throwsIllegalArgumentException() {
        var q = new NumericQuestion(1, "1", 1);
        var variant = new Variant("v1", "physics", List.of(q));

        assertThrows(IllegalArgumentException.class,
                () -> service.check(variant, List.of(new StudentAnswer(99, "1"))));
    }

    private QuestionResult check(Variant variant, int questionNumber, String rawValue) {
        return service.check(variant, List.of(new StudentAnswer(questionNumber, rawValue))).get(0);
    }
}