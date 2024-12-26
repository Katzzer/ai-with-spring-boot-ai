package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.QuestionFromWeb;
import com.pavelkostal.aiwithjava.model.QuestionTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OpenAIServiceImplTest {

    @Autowired
    OpenAIService openAIService;

    @Test
    void askGeneralQuestion() {
        QuestionFromWeb questionFromWeb = new QuestionFromWeb(QuestionTypeEnum.GENERAL_QUESTION.getQuestionType(), "Why is Java more popular than Kotlin", QuestionTypeEnum.GENERAL_QUESTION);
        String answer = openAIService.askQuestion(questionFromWeb);
        System.out.println(answer);
        assertNotNull(answer);
    }
}