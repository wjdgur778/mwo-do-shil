package com.example.mwo_do_shil.external.llm.gemini;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.store.dto.StoreResponseDto;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMService;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.llm.prompt.PromptType;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
        List<KakaoPlaceDto>stores = (List<KakaoPlaceDto>)llmRequest.getData();


        String finalPrompt = prompt + "\n가게 리스트: \n" + gson.toJson(stores);
        System.out.println(finalPrompt);
        //llm api에 요청하기
//        System.out.println("llm api 호출 전");
//        GenerateContentResponse response =
//                client.models.generateContent(
//                        "gemini-3-flash-preview",
//                        prompt,
//                        null);
//        System.out.println("llm api 호출 완료");
//        return response.text();
        return finalPrompt;
    }

}
