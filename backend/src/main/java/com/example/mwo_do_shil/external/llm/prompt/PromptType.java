package com.example.mwo_do_shil.external.llm.prompt;

public enum PromptType {
    CACHE_STORE_ALCOHOL_PAIRING("instruction_store_alcohol_pairing.txt"),
    INPUT_STORE_ALCOHOL_PAIRING("input_store_alcohol_pairing.txt"),
    STORE_ALCOHOL_PAIRING("store_alcohol_pairing.txt"),
    FILTER_STORE_ALCOHOL_PAIRING("filter_store_alcohol_pairing.txt");

    private final String fileName;

    PromptType(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
