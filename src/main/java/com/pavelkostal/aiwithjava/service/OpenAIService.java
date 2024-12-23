package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.GetCapitalRequest;

public interface OpenAIService {

    String getAnswer(String question);

    String getCapital(GetCapitalRequest getCapitalRequest);

    String getCapitalWithInfo(GetCapitalRequest getCapitalRequest);
}
