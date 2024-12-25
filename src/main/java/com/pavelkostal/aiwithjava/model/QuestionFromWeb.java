package com.pavelkostal.aiwithjava.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuestionFromWeb {
    private String questionTypeString;
    private String question;
}
