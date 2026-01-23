package com.example.mwo_do_shil.external.llm.gemini;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.store.dto.StoreResponseDto;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMService;
import com.example.mwo_do_shil.external.llm.gemini.dto.InputDto;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.llm.prompt.PromptType;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 빈 이름 "gemini" 설정
@Service("gemini")
@RequiredArgsConstructor
public class geminiServiceImpl implements LLMService {
    private final PromptRenderer promptRenderer;
    private final Gson gson;
    private final Client client;
    // The client gets the API key from the environment variable `GEMINI_API_KEY`.
    public String generate(LLMRequest llmRequest){
        String prompt = promptRenderer.render(
                PromptType.STORE_ALCOHOL_PAIRING,
                llmRequest.getParams()
                );

        //랜더링한 프롬포트에 가게 리스트 붙이기
        String stores = toInput((List<KakaoPlaceDto>)llmRequest.getData());

        String finalPrompt = prompt + "\n가게 리스트: \n" + stores;
        System.out.println(finalPrompt);

        // 1. 구글 검색(Google Search) 기능을 담은 도구 생성 (웹 그라운딩 활성화)

        GoogleSearch googleSearch = GoogleSearch.builder().build();

        // 2. Tool 객체에 googleSearch 주입
        Tool tool = Tool.builder()
                .googleSearch(googleSearch) // 기존 googleSearchRetrieval에서 변경
                .build();

        // 3. Config 설정
        GenerateContentConfig config = GenerateContentConfig.builder()
                .tools(Collections.singletonList(tool))
                .build();

        //4. 실제 llm api에 요청하기
        System.out.println("llm api 호출 전");
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash-lite",
                        finalPrompt,
                        config);
        System.out.println("llm api 호출 완료");
        System.out.println(response.usageMetadata());


        // todo
        //  1.결과를 DTO에 반환하기
        //  2.
        /**
         *
         * String cleanJson = rawResponse
         *         .replaceAll("```json", "")
         *         .replaceAll("```", "")
         *         .trim();
         */
        /**
         * 테스트 결과 text
         * ```json\n[\n  {\n    \"id\": 725163522,\n    \"reason\": \"곱창이랑 소주 조합은 말해 뭐해~ 겉바속쫀한 곱창에 시원한 소주 한 잔이면 스트레스 싹 풀릴걸? 💖 김치말이국수도 꼭 시켜봐!\",\n    \"score\": 950\n  },\n
         * {\n    \"id\": 1026252920,\n    \"reason\": \"고기 구워 먹으면서 소주 한 잔! 캬~ 🤤 여기 우대갈비가 그렇게 맛있대! 맑은 날씨에 맛있는 고기랑 소주면 여기가 천국이지! ✨\",\n    \"score\": 900\n  },\n
         * {\n    \"id\": 2020741818,\n    \"reason\": \"얼얼한 마라탕에 소주 한 잔? 🔥 의외로 궁합 최고라구! 매콤한 마라탕 국물이 소주를 술술 넘어가게 할 거야. 😋\",\n    \"score\": 880\n  },\n
         * {\n    \"id\": 1526863270,\n    \"reason\": \"김치찌개 맛집 인정! 얼큰한 김치찌개에 소주 한 잔이면 밥도둑, 술도둑 따로 없지~ 💯 맑은 날씨에 뜨끈한 찌개 강추! 👍\",\n    \"score\": 850\n  },\n
         * {\n    \"id\": 898391064,\n    \"reason\": \"든든하게 밥 먹고 소주 한 잔? 🍚 얼큰한 찌개랑 소주 조합이면 든든함과 시원함 둘 다 잡을 수 있어! 😋\",\n    \"score\": 800\n  }\n]\n```
         */

        return response.text();
    }

    /**
     * memo
     *  카카오 가게 데이터를 llm api 호출에 맞게 데이터 다이어트
     *  1. 불필요한 정보 삭제를 삭제한다.
     *  1.1. 전화번호나 위치 등은 삭제하고 필드 명은 (ex. n, ad, c)와 같이 간소화.
     *  1.2. 현 지도를 기반으로 한 검색이기 때문에 주소는 고정이라는 점을 인지한다.
     *       따라서 하나의 데이터의 주소를 파싱해서 프롬프트 상단에 고정시킨다.
     *  1.3. 술을 팔지 않는 가게는 미리 필터링 한다.
     * @param stores
     * @return
     */
    private String toInput(List<KakaoPlaceDto>stores){
        Set<String> excludeKeywords = Set.of("카페", "분식", "간식", "편의점", "샐러드", "패스트푸드", "도시락");

        List<InputDto> inputList = stores.stream()
                .filter(store -> {
                    String category = store.getCategory_name();
                    return excludeKeywords.stream()
                            .noneMatch(category::contains);
                })
                .map(kakaoPlaceDto ->
                        InputDto.builder()
                                .id(kakaoPlaceDto.getId())
                                .n(kakaoPlaceDto.getPlace_name())
                                .c(kakaoPlaceDto.getCategory_name())
                                .build()
                ).toList();

        return gson.toJson(inputList);
    }

}
