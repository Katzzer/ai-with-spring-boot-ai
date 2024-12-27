package com.pavelkostal.aiwithjava.model;

import java.time.LocalDateTime;

public record PromptDBItem(
        String id,
        String partitionKey,
        String gptType,
        String inputPromptFromUser,
        String inputPrompt,
        String outputPrompt,
        Long promptTokens,
        Long generationTokens,
        Long totalTokens,
        boolean isInvalidPrompt,
        String errorMessage,
        LocalDateTime localDateTime
) {
}
