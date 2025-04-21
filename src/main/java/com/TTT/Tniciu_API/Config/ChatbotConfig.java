package com.TTT.Tniciu_API.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ChatbotConfig {

    @Value("${coze.api.base-url}")
    private String cozeApiBaseUrl;

    @Value("${coze.api.access-token}")
    private String cozeApiAccessToken;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    public String getCozeApiBaseUrl() {
        return cozeApiBaseUrl;
    }

    public String getCozeApiAccessToken() {
        return cozeApiAccessToken;
    }
}
