package com.pavelkostal.aiwithjava.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionFromWeb {
    private String questionTypeString;
    private String question;
}
