package com.TTT.Tniciu_API.Service;

import com.TTT.Tniciu_API.Chatbot.ChatMessage;
import com.TTT.Tniciu_API.Config.ChatbotConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {

    private final RestTemplate restTemplate;
    private final ChatbotConfig chatbotConfig;

    @Autowired
    public ChatbotService(RestTemplate restTemplate, ChatbotConfig chatbotConfig) {
        this.restTemplate = restTemplate;
        this.chatbotConfig = chatbotConfig;
    }

    public ChatMessage sendMessageToCoze(ChatMessage message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(chatbotConfig.getCozeApiAccessToken());

        HttpEntity<ChatMessage> requestEntity = new HttpEntity<>(message, headers);

        String url = chatbotConfig.getCozeApiBaseUrl() + "/open_api/v2/chat";
        ResponseEntity<ChatMessage> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ChatMessage.class);

        ChatMessage response = responseEntity.getBody();
        if (response != null && response.getMessages() != null && !response.getMessages().isEmpty()) {
            // Assuming the first message in the response is the answer
            String answer = response.getMessages().get(0).getContent();
            message.setAnswer(answer);
            message.setMessages(response.getMessages());
        }

        return message;
    }
}
