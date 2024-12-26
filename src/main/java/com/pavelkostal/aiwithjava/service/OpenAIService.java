package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.*;

public interface OpenAIService {

    String askQuestion(QuestionFromWeb questionFromWeb);
}
