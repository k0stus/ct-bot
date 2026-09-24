package com.kansas.ctbot.model;

import java.util.Locale;

/**
 * Тип задания — определяет, как устроен правильный ответ и синтаксис его записи.
 * label соответствует значению в выпадающем списке "Тип" таблицы-ключа.
 */
public enum QuestionType {
    MULTIPLE_CHOICE("выбор"),
    SEQUENCE("последовательность"),
    MATCHING("соответствие"),
    NUMERIC("число"),
    OPEN_TEXT("текст");

    private final String label;

    QuestionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static QuestionType fromLabel(String label) {
        String normalized = label == null ? "" : label.trim().toLowerCase(Locale.ROOT);
        for (QuestionType type : values()) {
            if (type.label.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("неизвестный тип задания \"" + label + "\"");
    }
}