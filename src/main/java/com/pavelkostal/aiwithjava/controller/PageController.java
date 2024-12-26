package com.pavelkostal.aiwithjava.controller;

import com.pavelkostal.aiwithjava.model.QuestionFromWeb;
import com.pavelkostal.aiwithjava.service.OpenAIService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        return "pages/welcome-page";
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<String> handleQuestionAjax(@ModelAttribute("questionFromWeb") QuestionFromWeb questionFromWeb) {
        String response = openAIService.askQuestion(questionFromWeb);
        return ResponseEntity.ok(response);
    }
}
