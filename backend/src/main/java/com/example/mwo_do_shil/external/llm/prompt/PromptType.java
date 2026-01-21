package com.example.mwo_do_shil.external.llm.prompt;

public enum PromptType {
    STORE_ALCOHOL_PAIRING("store_alcohol_pairing.txt");
    private final String fileName;

    PromptType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
