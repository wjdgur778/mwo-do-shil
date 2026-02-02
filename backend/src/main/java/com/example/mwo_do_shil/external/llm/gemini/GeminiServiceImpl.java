package com.example.mwo_do_shil.external.llm.gemini;

import com.example.mwo_do_shil.domain.recommend.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMService;
import com.example.mwo_do_shil.external.llm.gemini.dto.InputDto;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.llm.prompt.PromptType;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
//
import java.util.List;
import java.util.Map;
import java.util.Set;

// 빈 이름 "gemini" 설정
@Slf4j
@Service("gemini")
@RequiredArgsConstructor
public class GeminiServiceImpl implements LLMService {

    private final PromptRenderer promptRenderer;
    private final Gson gson;

    @Qualifier("geminiWebClient")
    private final WebClient geminiWebClient;

    // 1차 필터링 수행
    public String first_generate(LLMRequest llmRequest) {

        String prompt = promptRenderer.render(
                PromptType.FILTER_STORE_ALCOHOL_PAIRING,
                llmRequest.getParams()
        );

        String storesJson = gson.toJson(llmRequest.getData());
        String finalPrompt = prompt + "\n가게 리스트:\n" + storesJson;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", finalPrompt)
                                )
                        )
                )
        );

        Map response = geminiWebClient.post()
                .uri("/models/gemini-2.5-flash-lite:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        System.out.println("method:first_generate / llm api 호출 완료");

        return extractText(response);
    }


    // 1차 필터링을 거친 가게를 중심으로 웹그라운딩을 통한 추천 결과 generate
    public String generateWithWebGrounding(LLMRequest llmRequest) {

        String prompt = promptRenderer.render(
                PromptType.STORE_ALCOHOL_PAIRING,
                llmRequest.getParams()
        );
        //랜더링한 프롬포트에 가게 리스트 붙이기
        String storesJson = gson.toJson(llmRequest.getData());
        String finalPrompt = prompt + "\n가게 리스트:\n" + storesJson;

        System.out.println("웹그라운딩 프롬프트 : \n"+finalPrompt);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", finalPrompt)
                                )
                        )
                ),
                "tools", List.of(
                        Map.of(
                                "google_search", Map.of() // ⭐ 웹 그라운딩 활성화
                        )
                )
        );

        Map response = geminiWebClient.post()
                .uri("/models/gemini-2.5-flash-lite:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        System.out.println("method:generateWithWebGrounding / llm api 호출 완료");

        return extractText(response);
    }



    @SuppressWarnings("unchecked")
    private String extractText(Map response) {
        if (response == null) return "";

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) return "";

        Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

        List<Map<String, String>> parts =
                (List<Map<String, String>>) content.get("parts");

        String text = parts.get(0).get("text");

        return text
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }



    /**
     * memo
     *  카카오 가게 데이터를 llm api 호출에 맞게 데이터 다이어트
     *  1. 불필요한 정보 삭제를 삭제한다.
     *  1.1. 전화번호나 위치 등은 삭제하고 필드 명은 (ex. n, ad, c)와 같이 간소화.
     *  1.2. 현 지도를 기반으로 한 검색이기 때문에 주소는 고정이라는 점을 인지한다.
     *       따라서 하나의 데이터의 주소를 파싱해서 프롬프트 상단에 고정시킨다.
     *  1.3. 술을 팔지 않는 가게는 미리 필터링 한다.
     *
     * @param stores
     * @return
     */
    public static List<InputDto> toInputList(List<KakaoPlaceDto> stores) {
        Set<String> excludeKeywords = Set.of("카페", "분식", "간식", "편의점", "샐러드", "패스트푸드", "도시락", "제과");

        return stores.stream()
                .filter(store -> {
                    String category = store.getCategory_name();
                    return excludeKeywords.stream()
                            .noneMatch(category::contains);
                })
                .map(kakaoPlaceDto ->
                        InputDto.builder()
                                .id(kakaoPlaceDto.getId())
                                .n(kakaoPlaceDto.getPlace_name())
                                .build()
                ).toList();


    }

}
