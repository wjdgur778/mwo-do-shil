package com.example.mwo_do_shil.external.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
public class LLMRequest {
    private Map<String,String> params;
    private Object data;
}
