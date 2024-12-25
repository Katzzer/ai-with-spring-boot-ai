package com.pavelkostal.aiwithjava.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping()
    public String welcomePage() {
        return "welcome-page";
    }
}
