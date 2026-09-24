package com.kansas.ctbot.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Правила разбора "сырой" строки ответа — единая конвенция, которую используют
 * и AnswerCheckingService (ответ ученика), и VariantFactory (ключ репетитора),
 * поэтому вынесены сюда, а не продублированы в обоих местах.
 */
public final class AnswerFormats {

    private static final Pattern MATCHING_PAIR = Pattern.compile("(\\p{Lu})(\\d+)");

    private AnswerFormats() {}

    /** "135" -> {"1","3","5"} — для заданий с множественным выбором. */
    public static Set<String> splitIntoChars(String value) {
        Set<String> result = new LinkedHashSet<>();
        for (char c : value.toCharArray()) {
            result.add(String.valueOf(c));
        }
        return result;
    }

    /** "1352" -> ["1","3","5","2"] — для заданий на последовательность. */
    public static List<String> splitIntoCharList(String value) {
        List<String> result = new ArrayList<>();
        for (char c : value.toCharArray()) {
            result.add(String.valueOf(c));
        }
        return result;
    }

    /** "А1Б1В2Г3" -> {"А":"1","Б":"1","В":"2","Г":"3"} — для "абвгдейки". */
    public static Map<String, String> parseMatchingPairs(String value) {
        Map<String, String> result = new LinkedHashMap<>();
        Matcher matcher = MATCHING_PAIR.matcher(value);
        while (matcher.find()) {
            result.put(matcher.group(1), matcher.group(2));
        }
        return result;
    }

    /** "3,1" -> "3.1" — принимаем и запятую, и точку как десятичный разделитель. */
    public static String normalizeNumeric(String value) {
        return value.trim().replace(',', '.');
    }
}