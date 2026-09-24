package com.kansas.ctbot.service;

import com.kansas.ctbot.model.MatchingQuestion;
import com.kansas.ctbot.model.MultipleChoiceQuestion;
import com.kansas.ctbot.model.NumericQuestion;
import com.kansas.ctbot.model.OpenTextQuestion;
import com.kansas.ctbot.model.QuestionType;
import com.kansas.ctbot.model.SequenceQuestion;
import com.kansas.ctbot.model.Variant;
import com.kansas.ctbot.parser.VariantParsingException;
import com.kansas.ctbot.parser.VariantRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VariantFactoryTest {

    private final VariantFactory factory = new VariantFactory();

    @Test
    void buildsMultipleChoiceQuestion() {
        var row = new VariantRow(1, QuestionType.MULTIPLE_CHOICE, "135", 2, 1.0, "");
        Variant variant = factory.create("v1", "biology", List.of(row));

        var mcq = assertInstanceOf(MultipleChoiceQuestion.class, variant.questionByNumber(1));
        assertEquals(Set.of("1", "3", "5"), mcq.correctOptions());
        assertEquals(2, mcq.maxScore());
        assertEquals(1, mcq.partialScore());
    }

    @Test
    void buildsSequenceQuestion() {
        var row = new VariantRow(2, QuestionType.SEQUENCE, "1352", 2, null, "");
        Variant variant = factory.create("v1", "chemistry", List.of(row));

        var sq = assertInstanceOf(SequenceQuestion.class, variant.questionByNumber(2));
        assertEquals(List.of("1", "3", "5", "2"), sq.correctOrder());
        assertEquals(0, sq.partialScore()); // пустая ячейка -> 0, а не NPE
    }

    @Test
    void buildsMatchingQuestion() {
        var row = new VariantRow(3, QuestionType.MATCHING, "А1Б1В2Г3", 2, 1.0, "");
        Variant variant = factory.create("v1", "biology", List.of(row));

        var mq = assertInstanceOf(MatchingQuestion.class, variant.questionByNumber(3));
        assertEquals(Map.of("А", "1", "Б", "1", "В", "2", "Г", "3"), mq.correctMapping());
    }

    @Test
    void buildsNumericQuestion_normalizesComma() {
        var row = new VariantRow(4, QuestionType.NUMERIC, "3,1", 1, null, "");
        Variant variant = factory.create("v1", "physics", List.of(row));

        var nq = assertInstanceOf(NumericQuestion.class, variant.questionByNumber(4));
        assertEquals("3.1", nq.correctValue());
    }

    @Test
    void buildsOpenTextQuestion_withSynonymsAndFlags() {
        var row = new VariantRow(5, QuestionType.OPEN_TEXT, "митохондрия | mitochondrion", 1, null,
                "регистр=нет;пробелы=игнор");
        Variant variant = factory.create("v1", "biology", List.of(row));

        var otq = assertInstanceOf(OpenTextQuestion.class, variant.questionByNumber(5));
        assertEquals(Set.of("митохондрия", "mitochondrion"), otq.acceptableAnswers());
        assertTrue(otq.caseInsensitive());
        assertTrue(otq.ignoreWhitespace());
    }

    @Test
    void openTextWithoutAnswer_throwsVariantParsingException() {
        var row = new VariantRow(6, QuestionType.OPEN_TEXT, "   ", 1, null, "");

        assertThrows(VariantParsingException.class, () -> factory.create("v1", "biology", List.of(row)));
    }

    @Test
    void buildsWholeVariantFromMultipleRows() {
        var rows = List.of(
                new VariantRow(1, QuestionType.MULTIPLE_CHOICE, "13", 2, 1.0, ""),
                new VariantRow(2, QuestionType.NUMERIC, "5.0", 1, null, "")
        );

        Variant variant = factory.create("bio_2026-10-05", "biology", rows);

        assertEquals("bio_2026-10-05", variant.id());
        assertEquals(2, variant.questions().size());
        assertInstanceOf(MultipleChoiceQuestion.class, variant.questionByNumber(1));
        assertInstanceOf(NumericQuestion.class, variant.questionByNumber(2));
    }
}