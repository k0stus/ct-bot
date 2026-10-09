package com.kansas.ctbot.storage;

import com.kansas.ctbot.model.ScoreScale;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoreScaleRepositoryTest {

    private final ScoreScaleRepository repository = new ScoreScaleRepository();

    @Test
    void loadsBuiltinBiology2026Scale() throws IOException {
        ScoreScale scale = repository.loadBuiltin("biology_2026.json");

        assertEquals("биология", scale.subject());
        assertEquals(2026, scale.year());
        assertEquals(60, scale.maxPrimaryScore());
        assertEquals(0, scale.convert(0));
        assertEquals(9, scale.convert(4));
        assertEquals(50, scale.convert(14));
        assertEquals(60, scale.convert(20));
        assertEquals(70, scale.convert(30));
        assertEquals(100, scale.convert(60));
    }

    @Test
    void missingResource_throwsIOException() {
        assertThrows(IOException.class, () -> repository.loadBuiltin("does_not_exist.json"));
    }
}