package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.Answer;
import com.pavelkostal.aiwithjava.model.GetCapitalRequest;

public interface OpenAIService {

    String getAnswer(String question);

    Answer getCapital(GetCapitalRequest getCapitalRequest);

    String getCapitalWithInfo(GetCapitalRequest getCapitalRequest);
}
