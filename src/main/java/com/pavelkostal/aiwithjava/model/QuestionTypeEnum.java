package com.pavelkostal.aiwithjava.model;

public enum QuestionTypeEnum {
    GENERAL_QUESTION("general_question"),
    UHK_DOCUMENTATION("uhk_documentation"),
    FILM_QUESTION("film_question"),
    CAPITAL_CITY_QUESTION("capital_city_question"),
    CAPITAL_CITY_WITH_MORE_INFO_QUESTION("capital_city_with_more_info_question");

    private final String question;

    QuestionTypeEnum(String question) {
        this.question = question;
    }

    public String question() {
        return question;
    }

    public static QuestionTypeEnum fromString(String text) {
        for (QuestionTypeEnum type : QuestionTypeEnum.values()) {
            if (type.question.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with text: " + text);
    }
}