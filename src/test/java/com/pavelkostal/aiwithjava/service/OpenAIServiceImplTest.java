package com.pavelkostal.aiwithjava.service;

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
        String answer = openAIService.askGeneralQuestion("Why is Java more popular than Kotlin");
        System.out.println(answer);
        assertNotNull(answer);
    }
}