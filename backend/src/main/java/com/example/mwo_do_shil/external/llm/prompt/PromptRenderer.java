package com.example.mwo_do_shil.external.llm.prompt;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

@Component
public class PromptRenderer {

    private final ResourceLoader resourceLoader;

    private final Map<PromptType, String> cache = new EnumMap<>(PromptType.class);

    public PromptRenderer(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
    // 캐싱을 통해 반복적인 파일 IO 작업을 방지
    @PostConstruct
    public void preload() {
        for (PromptType type : PromptType.values()) {
            cache.put(type, loadTemplate(type));
        }
    }

    public String render(PromptType type, Map<String, String> variables) {
        String template = cache.get(type);
        if (template == null) {
            throw new IllegalStateException("프롬프트 캐시 누락: " + type);
        }
        return applyVariables(template, variables);
    }

    private String loadTemplate(PromptType type) {
        String path = "classpath:prompts/" + type.getFileName();
        try (InputStream is = resourceLoader.getResource(path).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("프롬프트 로드 실패: " + path, e);
        }
    }

    private String applyVariables(String template, Map<String, String> variables) {
        String result = template;
        for (var entry : variables.entrySet()) {
            result = result.replace(
                    "{{" + entry.getKey() + "}}",
                    entry.getValue()
            );
        }
        return result;
    }
}
