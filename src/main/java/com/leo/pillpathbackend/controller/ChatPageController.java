package com.leo.pillpathbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ChatPageController {

    @GetMapping("/chat/{chatId}")
    public String chatPage(@PathVariable("chatId") Long chatId) {
        // Serves static chat.html under resources/static
        return "forward:/chat.html";
    }
}
