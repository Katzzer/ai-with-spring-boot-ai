package com.pavelkostal.aiwithjava.controllers;

import com.pavelkostal.aiwithjava.model.Answer;
import com.pavelkostal.aiwithjava.model.GetCapitalRequest;
import com.pavelkostal.aiwithjava.model.GetCapitalResponse;
import com.pavelkostal.aiwithjava.model.Question;
import com.pavelkostal.aiwithjava.service.OpenAIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuestionController {

    private final OpenAIService openAIService;

    public QuestionController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping
    public Answer askGeneralQuestion(@RequestBody Question question) {
        return new Answer(openAIService.askGeneralQuestion(question.question()));
    }

    @PostMapping(value = "/uhk-info")
    public Answer askUhkInfo(@RequestBody Question question) {
        return openAIService.askUhkInfo(question);
    }

    @PostMapping(value = "/movie-info")
    public Answer askMovieInfo(@RequestBody Question question) {
        return openAIService.getMovieInfo(question);
    }

    @PostMapping("/capital")
    public GetCapitalResponse getCapital(@RequestBody GetCapitalRequest getCapitalRequest) {
        return openAIService.getCapital(getCapitalRequest);
//        return new Answer(openAIService.getCapital(getCapitalRequest));
    }

    @PostMapping("/capital-with-info")
    public Answer getCapitalWithInfo(@RequestBody GetCapitalRequest getCapitalRequest) {
        return new Answer(openAIService.getCapitalWithInfo(getCapitalRequest));
    }
}
