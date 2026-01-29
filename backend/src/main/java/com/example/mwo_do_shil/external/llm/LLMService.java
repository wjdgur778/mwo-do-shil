package com.example.mwo_do_shil.external.llm;

public interface LLMService {
    String generateWithWebGrounding(LLMRequest llmRequest);
    String first_generate(LLMRequest llmRequest);
}
