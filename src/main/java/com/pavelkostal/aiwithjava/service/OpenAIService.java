package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.GetCapitalRequest;
import com.pavelkostal.aiwithjava.model.GetCapitalResponse;

public interface OpenAIService {

    String getAnswer(String question);

    GetCapitalResponse getCapital(GetCapitalRequest getCapitalRequest);

    String getCapitalWithInfo(GetCapitalRequest getCapitalRequest);
}
