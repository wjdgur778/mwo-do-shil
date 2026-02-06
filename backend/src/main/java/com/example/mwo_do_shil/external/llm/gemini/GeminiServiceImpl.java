package com.example.mwo_do_shil.external.llm.gemini;

import com.example.mwo_do_shil.domain.store.dto.InputForRecommendDto;
import com.example.mwo_do_shil.external.kakao.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMService;
import com.example.mwo_do_shil.external.llm.gemini.dto.FilterInputDto;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.llm.prompt.PromptType;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
//
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

// 빈 이름 "gemini" 설정
@Slf4j
@Service("gemini")
@RequiredArgsConstructor
public class GeminiServiceImpl implements LLMService {

    private final PromptRenderer promptRenderer;
    private final Gson gson;

    private final WebClient geminiWebClient;

    // [step.1]
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

    // [step 2.]
    // 웹그라운딩을 통해 llm에게 던져줄 가게 추천의 evidence를 가져온다.
    public CompletableFuture<InputForRecommendDto> getEvidenceByLLM(LLMRequest llmRequest) {

        String prompt = promptRenderer.render(
                PromptType.EVIDENCE_STORE_ALCOHOL_PAIRING,
                llmRequest.getParams()
        );
        String stores = gson.toJson(llmRequest.getData());
        String finalPrompt = prompt + "\n가게정보 : \n" + stores;
        System.out.println("[step.2] 프롬프트 : \n" + finalPrompt);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", finalPrompt)
                                )
                        )
                ), "tools", List.of(
                        Map.of(
                                "google_search", Map.of() // ⭐ 웹 그라운딩 활성화
                        )
                )
        );

        return geminiWebClient.post()
                .uri("/models/gemini-2.5-flash-lite:generateContent")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> {
//                    log.debug("📦 Gemini raw response map = {}", response);
                })
                .map(this::extractText)
                .doOnNext(text -> {
                    log.debug("🧾 Extracted text = {}", text);
                })
                .map(text -> gson.fromJson(text, InputForRecommendDto.class))
                .doOnSuccess(result -> {
                    log.info("✅ Gemini 응답 성공 - 가게명={}", result.getN());
                })
                .onErrorResume(e -> {
                    log.error("❌ Gemini 호출/파싱 실패", e);
                    return Mono.empty(); // ← 실패한 건 그냥 버림
                })
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }


    // evidence가 포함된 가게리스트를 보고 llm에게 추천 판단을 맡긴다.
    public String recommendWithEvidence(LLMRequest llmRequest) {

        String prompt = promptRenderer.render(
                PromptType.STORE_ALCOHOL_PAIRING,
                llmRequest.getParams()
        );
        //랜더링한 프롬포트에 가게 리스트 붙이기
        String finalPrompt = prompt + "\n가게 리스트:\n" + llmRequest.getData();

        System.out.println("[step.3] 프롬프트 : \n" + finalPrompt);

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
        System.out.println("method:recommendWithEvidence / llm api 호출 완료");
        return extractText(response);
    }


    @SuppressWarnings("unchecked")
    private String extractText(Map response) {
        if (response == null) {
            System.out.println("❌ response is null");
            return "";
        }

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            System.out.println("❌ candidates empty");
            return "";
        }

        Map<String, Object> candidate = candidates.get(0);

        // ===== 1️⃣ TEXT =====
        Map<String, Object> content =
                (Map<String, Object>) candidate.get("content");

        List<Map<String, String>> parts =
                (List<Map<String, String>>) content.get("parts");

        String text = parts.get(0).get("text")
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
//        // ===== 2️⃣ groundingMetadata =====
//        Map<String, Object> groundingMetadata =
//                (Map<String, Object>) candidate.get("groundingMetadata");
//
//        if (groundingMetadata == null) {
//            System.out.println("====== WEB GROUNDING ======");
//            System.out.println("❌ groundingMetadata 없음 (웹그라운딩 안 됨)");
//            return text;
//        }

//        System.out.println("====== WEB GROUNDING ======");
//        System.out.println("✅ groundingMetadata 존재 (웹그라운딩 됨)");
//
//        // ===== 3️⃣ 검색 쿼리 =====
//        List<String> webSearchQueries =
//                (List<String>) groundingMetadata.get("webSearchQueries");
//
//        System.out.println("------ Search Queries ------");
//        if (webSearchQueries != null) {
//            webSearchQueries.forEach(q -> System.out.println("- " + q));
//        } else {
//            System.out.println("없음");
//        }
//
//        // ===== 4️⃣ 실제 출처 =====
//        List<Map<String, Object>> groundingChunks =
//                (List<Map<String, Object>>) groundingMetadata.get("groundingChunks");
//
//        System.out.println("------ Sources ------");
//        if (groundingChunks != null) {
//            for (Map<String, Object> chunk : groundingChunks) {
//                Map<String, String> web =
//                        (Map<String, String>) chunk.get("web");
//
//                if (web != null) {
//                    System.out.println("- " + web.get("title"));
//                    System.out.println("  " + web.get("uri"));
//                }
//            }
//        } else {
//            System.out.println("없음");
//        }
        // ====== 시스템은 URL이 안전 표준을 충족 ========
//        System.out.println("------ url_retrieval_status ------");
//        Map<String, Object> urlContextMetadata =
//                (Map<String, Object>) candidate.get("url_context_metadata");
//        List<Map<String, Object>> urlMetadataList =
//                (List<Map<String, Object>>) urlContextMetadata.get("url_metadata");
//        if (urlMetadataList != null) {
//            for (Map<String, Object> urlMetadata : urlMetadataList) {
//                String retrievedUrl = (String) urlMetadata.get("retrieved_url");
//                String status = (String) urlMetadata.get("url_retrieval_status");
//                System.out.println("URL: " + retrievedUrl + ", Status: " + status);
//            }
//        } else {
//            System.out.println("없음");
//        }

        return text;
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
    public static List<FilterInputDto> toFilterInputList(List<KakaoPlaceDto> stores) {
        Set<String> excludeKeywords = Set.of("카페", "분식", "간식", "편의점", "샐러드", "패스트푸드", "도시락", "제과", "백화점");

        return stores.stream()
                .filter(store -> {
                    String category = store.getCategory_name();
                    return excludeKeywords.stream()
                            .noneMatch(category::contains);
                })
                .map(kakaoPlaceDto ->
                        FilterInputDto.builder()
                                .id(kakaoPlaceDto.getId())
                                .n(kakaoPlaceDto.getPlace_name())
                                .c(kakaoPlaceDto.getCategory_name())
                                .build()
                ).toList();
    }

}
