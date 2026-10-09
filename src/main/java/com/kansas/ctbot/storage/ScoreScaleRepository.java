package com.kansas.ctbot.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kansas.ctbot.model.ScoreScale;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Загружает ScoreScale из JSON вида {"subject": "...", "year": 2026, "table": {"0": 0, ...}}.
 * Встроенные таблицы (упакованные в jar) — из classpath-ресурсов scales/*.json;
 * собственные таблицы репетитора в будущем — с диска, тем же форматом файла.
 */
public final class ScoreScaleRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Встроенная таблица, например "biology_2026.json" из src/main/resources/scales/. */
    public ScoreScale loadBuiltin(String resourceName) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/scales/" + resourceName)) {
            if (in == null) {
                throw new IOException("Встроенная таблица не найдена: scales/" + resourceName);
            }
            return read(in);
        }
    }

    /** Своя таблица, положенная репетитором на диск (тот же JSON-формат). */
    public ScoreScale loadFromFile(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return read(in);
        }
    }

    private ScoreScale read(InputStream in) throws IOException {
        ScaleDto dto = objectMapper.readValue(in, ScaleDto.class);
        Map<Integer, Integer> table = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : dto.table.entrySet()) {
            table.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return new ScoreScale(dto.subject, dto.year, table);
    }

    private static final class ScaleDto {
        public String subject;
        public int year;
        public Map<String, Integer> table;
    }
}