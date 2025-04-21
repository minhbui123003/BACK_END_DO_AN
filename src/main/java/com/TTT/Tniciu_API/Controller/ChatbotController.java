package com.TTT.Tniciu_API.Controller;

import com.TTT.Tniciu_API.Chatbot.ChatMessage;
import com.TTT.Tniciu_API.Service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> sendMessage(@RequestBody ChatMessage message) {
        ChatMessage response = chatbotService.sendMessageToCoze(message);
        // Optionally, you can save 'message' here to a repository or log before sending the response
        return ResponseEntity.ok().body(response);
    }
}

