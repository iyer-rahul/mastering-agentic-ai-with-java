package com.telusko.langchainspring1.web;

import com.telusko.langchainspring1.lc4j.Assitant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController
{
    private Assitant assitant;
    public ChatController(Assitant assitant)
    {
        this.assitant=assitant;
    }
    @GetMapping("/ask")
    public String ask(@RequestParam("question") String question)
    {
        return assitant.chat(question);
    }

}
