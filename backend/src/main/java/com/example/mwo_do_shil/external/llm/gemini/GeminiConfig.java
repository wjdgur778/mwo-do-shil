package com.example.mwo_do_shil.external.llm.gemini;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Bean
    public Client createClient(){
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
}
