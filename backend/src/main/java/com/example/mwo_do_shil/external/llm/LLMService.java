package com.example.mwo_do_shil.external.llm;

import com.example.mwo_do_shil.domain.store.dto.InputForRecommendDto;

import java.util.concurrent.CompletableFuture;

public interface LLMService {
    String recommendWithEvidence(LLMRequest llmRequest);
    String first_generate(LLMRequest llmRequest);
    CompletableFuture<InputForRecommendDto> getEvidenceByLLM(LLMRequest llmRequest);
}
