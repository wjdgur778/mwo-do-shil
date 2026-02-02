package com.example.mwo_do_shil.domain.recommend;

import com.example.mwo_do_shil.domain.recommend.dto.*;
import com.example.mwo_do_shil.external.kakao.KakaoApiService;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMRouter;
import com.example.mwo_do_shil.external.llm.LLMType;
import com.example.mwo_do_shil.external.llm.gemini.dto.InputDto;
import com.example.mwo_do_shil.external.weather.WeatherService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.mwo_do_shil.external.llm.gemini.GeminiServiceImpl.toInputList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final KakaoApiService kakaoApiService;
    private final WeatherService weatherService;
    private final LLMRouter llmRouter;
    private final Gson gson;

    /**
     * todo
     *  DB에서 이미 추천을 받아 기록되어있는 place들을 검색 [캐싱 전략 바로 세우기]
     * <p>
     *  memo
     *   아래의 api를 호출한다.
     *   1. 카카오 키워드 검색 api
     *   2. 날씨 api
     *   3. gemini llm api
     *      3.1 가게 선별을 위한 1차 호출
     *      3.2 선별된 가게에 대한 웹그라운딩
     */
    public List<RecommendResponseDto> getRecommend(
            String uid,
            String alcohol,
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {

        // kakao local api 호출
        List<KakaoPlaceDto> stores = getStoreList(minX, minY, maxX, maxY);
        // 간소화된 가게 리스트 준비
        List<InputDto> smaller_stores = toInputList(stores);
        String address = stores.get(0).getAddress_name() + " 주변";
        // 날씨 api호출
        String weather = weatherService.getCurrentWeather(minX, minY, maxX, maxY);

        // 1차 필터링
        List<InputDto> nextStepStores = filterStoresByLLM(
                alcohol,
                weather,
                address,
                smaller_stores);
        if (nextStepStores.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 가게가 없습니다.");
        }

        // 1차 필터링된 가게를 기준으로 웹그라운딩
        List<RecommendPlaceDto> recommendPlaceDtos =
                recommendWithWebGrounding(
                        alcohol,
                        weather,
                        address,
                        nextStepStores);
        if (recommendPlaceDtos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 가게가 없습니다.");
        }

        // 추천된 가게들을 kakao 가게 정보와 합치기
        Map<Long, KakaoPlaceDto> storeMap = buildStoreMap(stores);

        return recommendPlaceDtos.stream()
                .map(dto -> RecommendResponseDto.builder()
                        .place(storeMap.get(dto.getId()))  // null 체크는 서비스 레이어에서
                        .reason(dto.getR())
                        .score(dto.getS())
                        .build())
                .toList();
    }

    //카카오 Local api호출 매서드
    private List<KakaoPlaceDto> getStoreList(
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {
        // 1. kakao local api 호출을 통해 store의 리스트를 받아온다.
        // 1.1 이때 lat과 lon을 16등분해서 kakao local api 호출을 한다.
        List<RectDto> rects = divideRectInto16(minX, minY, maxX, maxY);

        // 1.2 키워드는 "맛집"
        //   Kakao Local API를 비동기로 호출한다.
        //    → 이 시점에서 HTTP 요청은 거의 동시에 시작되며
        //      각 호출은 CompletableFuture로 즉시 반환된다.
        List<CompletableFuture<KakaoSearchResponseDto>> futures =
                rects.stream()
                        .map(kakaoApiService::searchPlaces).toList();

        // 2. 모든 비동기 호출이 완료되었을 때를 표현하는
        //    하나의 CompletableFuture를 생성한다.
        //    → 실제 API 호출을 실행하는 것이 아니라
        //      "모든 Future가 끝나는 시점"을 조합한 Future
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            // 3. 현재 쓰레드에서 모든 비동기 작업이 끝날 때까지 대기
            //    → 병렬 실행은 유지되며, 결과를 사용하기 직전에만 join
            allFutures.join();

            // 4. 각 Future의 결과를 가져와
            //    Kakao API 응답에 포함된 장소 목록(documents)을 하나의 리스트로 병합
            List<KakaoPlaceDto> results = futures.stream()
                    .map(CompletableFuture::join)// 각 API 호출 결과 획득
                    .flatMap(response -> response.getDocuments().stream())
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 가게가 없습니다.");
            }

            return results;

        } catch (Exception e) {
            log.error("병렬 API 호출 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }


    // 카카오 맵을 16 조각으로 나누어 데이터의 량과 질을 모두 향상시킨다.
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


    private List<InputDto> filterStoresByLLM(String alcohol,
                                             String weather,
                                             String address,
                                             List<InputDto> smaller_stores) {
        // 1차 필터링
        // 결과 : JSON 정수 배열 [1,5,22,45]
        String filtered_stores = llmRouter.route(LLMType.GEMINI).first_generate(new LLMRequest(Map.of(
                "alcohol", alcohol
                , "weather", weather
                // 검색은 한 지역내에 있는 가게를 기준으로 하기 때문에 주소가 비슷하다.
                // 데이터 다이어트를 위해 상단에 한번만 address를 고정한다.
                , "address", address
        ), smaller_stores
        ));
        System.out.println("filtered_stores : " + filtered_stores);

        // 1차 필터링에서 얻은 ID 리스트
        List<Long> selectedIds = gson.fromJson(filtered_stores, new TypeToken<List<Long>>() {
        }.getType());

        // 전체 간소화 가게 리스트(smaller_stores)에서 해당 ID만 추출
        return smaller_stores.stream()
                .filter(store -> selectedIds.contains(store.getId()))
                .toList();
    }

    private List<RecommendPlaceDto> recommendWithWebGrounding(String alcohol,
                                                              String weather,
                                                              String address,
                                                              List<InputDto> nextStepStores) {
        // 2차 웹 그라운딩
        String recommended_stores = llmRouter.route(LLMType.GEMINI).generateWithWebGrounding(new LLMRequest(Map.of(
                "alcohol", alcohol
                , "weather", weather
                // 검색은 한 지역내에 있는 가게를 기준으로 하기 때문에 주소가 비슷하다.
                // 데이터 다이어트를 위해 상단에 한번만 address를 고정한다.
                , "address", address + " 주변"
        ), nextStepStores
        ));

        System.out.println("recommended_stores : " + recommended_stores);
        // String 형태의 json 텍스트를 dto list로 역직렬화
        Type type = new TypeToken<List<RecommendPlaceDto>>() {}.getType();

        return gson.fromJson(recommended_stores, type);
    }

    // 추천된 결과값과 가게의 정보를 합치는 과정이 필요하다.
    // 이때 가게갯수 * 추천된 갯수 만큼 순회하는것보다 map으로 데이터를 옮기고 id가 일치하는지 확인하는게 더 빠르다.
    private Map<Long, KakaoPlaceDto> buildStoreMap(List<KakaoPlaceDto> stores) {
        return stores.stream()
                .collect(Collectors.toMap(
                        KakaoPlaceDto::getId,
                        Function.identity(),
                        (old, replace) -> old
                ));
    }


}
