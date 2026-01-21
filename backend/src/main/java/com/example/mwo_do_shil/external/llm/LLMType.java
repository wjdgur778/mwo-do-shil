package com.example.mwo_do_shil.external.llm;

public enum LLMType {
    GEMINI("gemini"),
    OPENAI("openai");

    private final String key;

    LLMType(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
