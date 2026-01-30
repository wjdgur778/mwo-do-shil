package com.example.mwo_do_shil.external.llm.gemini;

import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.llm.prompt.PromptType;
import com.google.genai.Client;
import com.google.genai.types.CachedContent;
import com.google.genai.types.Content;
import com.google.genai.types.CreateCachedContentConfig;
import com.google.genai.types.Part;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;


@Configuration
@RequiredArgsConstructor
public class GeminiConfig {

    private final PromptRenderer promptRenderer;

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @Bean
    public Client createClient(){
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }
//    @Bean
//    public PromptCache placePairingPromptCache(Client client) {
//
//        String systemPrompt = promptRenderer.render(PromptType.CACHE_STORE_ALCOHOL_PAIRING);
//        if (systemPrompt == null) {
//            throw new IllegalStateException("Prompt not initialized");
//        }
//
//        // 1. 시스템 지침 객체 생성
//        Content systemInstruction = Content.builder()
//                .parts(List.of(Part.builder().text(systemPrompt).build()))
//                .build();
//
//        // 2. Config 빌드 (시스템 지침을 여기에 설정)
//        CreateCachedContentConfig config = CreateCachedContentConfig.builder()
//                .systemInstruction(systemInstruction) // ⚠️ 여기에 넣는 것이 정석입니다!
//                .ttl(Duration.ofDays(1)) // 필요 시 TTL 설정
//                .build();
//
//        // 3. 캐시 생성 호출
//        CachedContent cachedContent = client.caches.create(
//                "gemini-2.5-flash-lite",
//                config
//        );
//
//        String cacheName = cachedContent.name().get();
//        return new PromptCache(cacheName);
//    }
}
