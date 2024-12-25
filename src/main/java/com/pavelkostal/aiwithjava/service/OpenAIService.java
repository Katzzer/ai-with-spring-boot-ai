package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.*;

public interface OpenAIService {

    String askGeneralQuestion(String question);

    GetCapitalResponse getCapital(GetCapitalRequest getCapitalRequest);

    String getCapitalWithInfo(GetCapitalRequest getCapitalRequest);

    Answer getMovieInfo(Question question);

    Answer askUhkInfo(Question question);

    String askQuestion(QuestionFromWeb questionFromWeb);
}
