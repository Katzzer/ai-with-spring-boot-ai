package com.pavelkostal.aiwithjava.model;

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
        String errorMessage
) {
}
