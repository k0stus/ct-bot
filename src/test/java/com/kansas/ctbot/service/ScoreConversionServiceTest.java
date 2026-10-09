package com.kansas.ctbot.service;

import com.kansas.ctbot.model.ScoreScale;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreConversionServiceTest {

    private final ScoreConversionService service = new ScoreConversionService();

    @Test
    void convertsExactIntegerScore() {
        ScoreScale scale = new ScoreScale("test", 2026, Map.of(0, 0, 1, 5, 2, 10));

        assertEquals(5, service.convert(scale, 1));
    }

    @Test
    void roundsFractionalRawScoreBeforeConverting() {
        ScoreScale scale = new ScoreScale("test", 2026, Map.of(0, 0, 1, 5, 2, 10, 3, 15, 4, 20));

        assertEquals(15, service.convert(scale, 3.4)); // округляется к 3
        assertEquals(20, service.convert(scale, 3.6)); // округляется к 4
    }

    @Test
    void outOfRangeScore_throws() {
        ScoreScale scale = new ScoreScale("test", 2026, Map.of(0, 0, 1, 5));

        assertThrows(IllegalArgumentException.class, () -> service.convert(scale, 5));
    }

    @Test
    void negativeScore_throws() {
        ScoreScale scale = new ScoreScale("test", 2026, Map.of(0, 0, 1, 5));

        assertThrows(IllegalArgumentException.class, () -> service.convert(scale, -1));
    }

    @Test
    void gapInTable_throwsOnConstruction() {
        // пропущен первичный балл 1
        Map<Integer, Integer> tableWithGap = Map.of(0, 0, 2, 10);

        assertThrows(IllegalArgumentException.class, () -> new ScoreScale("test", 2026, tableWithGap));
    }

    @Test
    void realBiologyScale_matchesKnownPoints() {
        ScoreScale scale = fullBiology2026Scale();

        assertEquals(0, service.convert(scale, 0));
        assertEquals(9, service.convert(scale, 4));
        assertEquals(50, service.convert(scale, 14));
        assertEquals(60, service.convert(scale, 20));
        assertEquals(70, service.convert(scale, 30));
        assertEquals(100, service.convert(scale, 60));
    }

    /** Полная таблица РИКЗ по биологии 2026 (та же, что в src/main/resources/scales/biology_2026.json). */
    private ScoreScale fullBiology2026Scale() {
        int[] primary = {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
                15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
                30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44,
                45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60
        };
        int[] converted = {
                0, 1, 2, 3, 9, 14, 20, 25, 30, 34, 38, 41, 45, 48, 50,
                52, 55, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
                70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
                85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100
        };
        Map<Integer, Integer> table = new HashMap<>();
        for (int i = 0; i < primary.length; i++) {
            table.put(primary[i], converted[i]);
        }
        return new ScoreScale("биология", 2026, table);
    }
}