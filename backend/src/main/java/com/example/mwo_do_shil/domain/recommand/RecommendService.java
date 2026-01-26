package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.recommand.dto.*;
import com.example.mwo_do_shil.external.kakao.KakaoApiService;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMRouter;
import com.example.mwo_do_shil.external.llm.LLMType;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.example.mwo_do_shil.external.weather.WeatherService;
import com.example.mwo_do_shil.external.weather.dto.WeatherResponseDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final KakaoApiService kakaoApiService;
    private final WeatherService weatherService;
    private final RecommendRepository recommendRepository;
    private final LLMRouter llmRouter;
    private final Gson gson;

    /**
     * rect
     *
     * @return
     */
    public List<RecommendResponseDto> getRecommend(
            String alcohol,
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {
        // kakao local api 호출
        List<KakaoPlaceDto> stores = getStoreList(minX, minY, maxX, maxY);
        // todo
        //  DB에서 이미 추천을 받아 기록되어있는 place들을 검색 [캐싱 전략 바로 세우기]


        // 이미 추천된 가게가 5개 이상이 아니라면 llm api 호출

        String weather = weatherService.getCurrentWeather(minX, minY, maxX, maxY);
        log.info("날씨 api호출 성공 : "+weather);


        String result = llmRouter.route(LLMType.GEMINI).generate(new LLMRequest(Map.of(
                "alcohol", alcohol
                , "weather", weather
                // 검색은 한 지역내에 있는 가게를 기준으로 하기 때문에 주소가 비슷하다.
                // 데이터 다이어트를 위해 상단에 한번만 address를 고정한다.
                , "address", stores.get(0).getAddress_name() + " 주변"
        )
                , stores
        ));
        // String 형태의 json 텍스트를 dto list로 역직렬화
        Type type = new TypeToken<List<RecommendPlaceDto>>() {
        }.getType();
        List<RecommendPlaceDto> recommendPlaceDtos = gson.fromJson(result, type);

        // 추천된 결과값과 가게의 정보를 합치는 과정이 필요하다.
        // 이때 가게갯수 * 추천된 갯수 만큼 순회하는것보다 map으로 데이터를 옮기고 id가 일치하는지 확인하는게 더 빠르다.
        Map<Long, KakaoPlaceDto> storeMap = stores.stream()
                .collect(Collectors.toMap(KakaoPlaceDto::getId, Function.identity()));

        List<RecommendResponseDto> response = recommendPlaceDtos.stream()
                .map(dto -> RecommendResponseDto.builder()
                        .place(storeMap.get(dto.getId()))  // null 체크는 서비스 레이어에서
                        .reason(dto.getR())
                        .score(dto.getS())
                        .build())
                .toList();

        return response;
    }

    private List<KakaoPlaceDto> getStoreList(
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {
        // 1. kakao local api 호출을 통해 store의 리스트를 받아온다.
        // 1.1 이때 lat과 lon을 16등분해서 kakao local api 호출을 한다.
        List<RectDto> rects = divideRectInto16(minX, minY, maxX, maxY);

        // 1.2 키워드는 "맛집", 1.3 병렬 처리를 위해 @Async로 처리
        List<CompletableFuture<KakaoSearchResponseDto>> futures =
                rects.stream()
                        .map(kakaoApiService::searchPlacesAsync).toList();

        // 모든 병렬 호출이 완료될 때까지 대기
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.join(); // 모든 비동기 작업이 완료될 때까지 대기
            // 2. 가져온 json값을 dto에 맵핑
            return futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(response -> response.getDocuments().stream())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("병렬 API 호출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }


    private List<RectDto> divideRectInto16(BigDecimal minX, BigDecimal minY, BigDecimal maxX, BigDecimal maxY) {
        List<RectDto> rects = new ArrayList<>();

        // 4로 나누기 위해 BigDecimal 사용
        BigDecimal divisor = BigDecimal.valueOf(4);

        BigDecimal xStep = maxX.subtract(minX).divide(divisor, RoundingMode.HALF_UP);
        BigDecimal yStep = maxY.subtract(minY).divide(divisor, RoundingMode.HALF_UP);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                BigDecimal rectMinX = minX.add(xStep.multiply(BigDecimal.valueOf(i)));
                BigDecimal rectMinY = minY.add(yStep.multiply(BigDecimal.valueOf(j)));
                BigDecimal rectMaxX = rectMinX.add(xStep);
                BigDecimal rectMaxY = rectMinY.add(yStep);

                rects.add(RectDto.builder()
                        .minX(rectMinX)
                        .minY(rectMinY)
                        .maxX(rectMaxX)
                        .maxY(rectMaxY)
                        .build());
            }
        }

        return rects;
    }


}
