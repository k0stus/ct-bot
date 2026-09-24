package com.kansas.ctbot.parser;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Низкоуровневое чтение .xlsx: не знает ничего ни о варианте, ни об ответах ученика —
 * просто превращает первый лист таблицы в список строк вида
 * "заголовок столбца -> значение ячейки". Первая строка листа — заголовок.
 * Полностью пустые строки пропускаются.
 */
public final class ExcelTableReader {

    public List<Map<String, String>> read(InputStream xlsxInput) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(xlsxInput)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return List.of();
            }

            List<String> headers = readHeaders(rowIterator.next());
            List<Map<String, String>> rows = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Map<String, String> rowMap = readRow(rowIterator.next(), headers);
                if (!isBlank(rowMap)) {
                    rows.add(rowMap);
                }
            }
            return rows;
        }
    }

    private List<String> readHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cellToString(cell).trim());
        }
        return headers;
    }

    private Map<String, String> readRow(Row row, List<String> headers) {
        Map<String, String> rowMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, MissingCellPolicy.RETURN_BLANK_AS_NULL);
            rowMap.put(headers.get(i), cell == null ? "" : cellToString(cell).trim());
        }
        return rowMap;
    }

    private boolean isBlank(Map<String, String> rowMap) {
        return rowMap.values().stream().allMatch(String::isBlank);
    }

    private String cellToString(Cell cell) {
        CellType type = cell.getCellType();
        if (type == CellType.STRING) {
            return cell.getStringCellValue();
        }
        if (type == CellType.NUMERIC) {
            return formatNumeric(cell.getNumericCellValue());
        }
        if (type == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }

    private String formatNumeric(double value) {
        if (value == Math.rint(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}