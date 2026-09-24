package com.kansas.ctbot.parser;

import com.kansas.ctbot.model.QuestionType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VariantTableParserTest {

    private static final String[] HEADERS =
            {"№", "Тип", "Правильный ответ", "Макс. балл", "Частично", "Доп. параметры"};

    private final VariantTableParser parser = new VariantTableParser();

    @Test
    void parsesRowsIntoVariantRows() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "выбор", "135", 2.0, 1.0, ""},
                {2.0, "число", "3,1", 1.0, "", ""}
        });

        List<VariantRow> rows = parser.parse(xlsx);

        assertEquals(2, rows.size());

        VariantRow first = rows.get(0);
        assertEquals(1, first.number());
        assertEquals(QuestionType.MULTIPLE_CHOICE, first.type());
        assertEquals("135", first.correctAnswer());
        assertEquals(2, first.maxScore());
        assertEquals(1, first.partialScore());

        VariantRow second = rows.get(1);
        assertEquals(QuestionType.NUMERIC, second.type());
        assertNull(second.partialScore()); // пустая ячейка "Частично" -> null, не 0
    }

    @Test
    void skipsBlankRows() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "число", "3.1", 1.0, "", ""},
                {"", "", "", "", "", ""}
        });

        assertEquals(1, parser.parse(xlsx).size());
    }

    @Test
    void invalidNumber_throwsVariantParsingException() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {"не число", "число", "3.1", 1.0, "", ""}
        });

        assertThrows(VariantParsingException.class, () -> parser.parse(xlsx));
    }

    @Test
    void unknownType_throwsVariantParsingException() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "неизвестный тип", "1", 1.0, "", ""}
        });

        assertThrows(VariantParsingException.class, () -> parser.parse(xlsx));
    }

    @Test
    void missingMaxScore_throwsVariantParsingException() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "число", "3.1", "", "", ""}
        });

        assertThrows(VariantParsingException.class, () -> parser.parse(xlsx));
    }

    private InputStream buildWorkbook(Object[][] data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }
            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < data[r].length; c++) {
                    setCellValue(row.createCell(c), data[r][c]);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void setCellValue(Cell cell, Object value) {
        if (value instanceof Double d) {
            cell.setCellValue(d);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }
}