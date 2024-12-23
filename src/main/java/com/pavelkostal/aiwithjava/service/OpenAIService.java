package com.pavelkostal.aiwithjava.service;

import com.pavelkostal.aiwithjava.model.Answer;
import com.pavelkostal.aiwithjava.model.GetCapitalRequest;
import com.pavelkostal.aiwithjava.model.GetCapitalResponse;
import com.pavelkostal.aiwithjava.model.Question;

public interface OpenAIService {

    String getAnswer(String question);

    GetCapitalResponse getCapital(GetCapitalRequest getCapitalRequest);

    String getCapitalWithInfo(GetCapitalRequest getCapitalRequest);

    Answer getMovieInfo(Question question);
}
