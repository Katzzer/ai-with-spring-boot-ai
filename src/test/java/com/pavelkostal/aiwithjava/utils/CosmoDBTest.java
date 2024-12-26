package com.pavelkostal.aiwithjava.utils;

import com.pavelkostal.aiwithjava.model.PromptDBItem;
import com.pavelkostal.aiwithjava.model.QuestionTypeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CosmoDBTest {

    @Autowired
    CosmoDB cosmoDB;

    @Test
    void readItemsFromDB() {
        ArrayList<PromptDBItem> promptDBItems = cosmoDB.readItemsFromDB(QuestionTypeEnum.GENERAL_QUESTION);
        long totalTokensForPrompts = promptDBItems.stream()
                .filter(promptDBItem -> !promptDBItem.isInvalidPrompt())
                .mapToLong(PromptDBItem::totalTokens)
                .sum();

        System.out.println(totalTokensForPrompts);
    }
}