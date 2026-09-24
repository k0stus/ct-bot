package com.kansas.ctbot.parser;

import com.kansas.ctbot.model.StudentAnswer;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentAnswerTableParserTest {

    private static final String[] HEADERS = {"№", "Ответ"};

    private final StudentAnswerTableParser parser = new StudentAnswerTableParser();

    @Test
    void parsesRowsIntoStudentAnswers() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "135"},
                {2.0, "3.1"},
                {3.0, "митохондрия"}
        });

        List<StudentAnswer> answers = parser.parse(xlsx);

        assertEquals(3, answers.size());
        assertEquals(new StudentAnswer(1, "135"), answers.get(0));
        assertEquals(new StudentAnswer(2, "3.1"), answers.get(1));
        assertEquals(new StudentAnswer(3, "митохондрия"), answers.get(2));
    }

    @Test
    void skipsBlankRows() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {1.0, "135"},
                {"", ""}
        });

        assertEquals(1, parser.parse(xlsx).size());
    }

    @Test
    void invalidNumber_throwsVariantParsingException() throws IOException {
        InputStream xlsx = buildWorkbook(new Object[][]{
                {"не число", "135"}
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