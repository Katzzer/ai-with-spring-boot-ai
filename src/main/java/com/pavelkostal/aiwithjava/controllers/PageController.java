package com.pavelkostal.aiwithjava.controllers;

import com.pavelkostal.aiwithjava.model.QuestionFromWeb;
import com.pavelkostal.aiwithjava.service.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@AllArgsConstructor
public class PageController {

    private final OpenAIService openAIService;

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("questionFromWeb", new QuestionFromWeb());
        return "welcome-page";
    }

    @PostMapping("/submit")
    @ResponseBody
    public String handleQuestionAjax(@ModelAttribute("questionFromWeb") QuestionFromWeb questionFromWeb) {
       return openAIService.askQuestion(questionFromWeb);
    }
}
