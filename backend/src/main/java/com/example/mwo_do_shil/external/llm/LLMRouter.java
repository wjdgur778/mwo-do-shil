package com.example.mwo_do_shil.external.llm;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LLMRouter {

    private final Map<String, LLMService> llmServices;

    public LLMService route(LLMType type) {
        return llmServices.get(type.getKey());
    }
}
