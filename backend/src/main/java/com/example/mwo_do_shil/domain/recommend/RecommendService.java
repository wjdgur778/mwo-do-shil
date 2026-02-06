package com.example.mwo_do_shil.domain.recommend;

import com.example.mwo_do_shil.config.database.FirestoreService;
import com.example.mwo_do_shil.domain.recommend.dto.*;
import com.example.mwo_do_shil.domain.store.dto.InputForRecommendDto;
import com.example.mwo_do_shil.external.kakao.KakaoApiService;
import com.example.mwo_do_shil.external.kakao.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.external.kakao.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMRouter;
import com.example.mwo_do_shil.external.llm.LLMType;
import com.example.mwo_do_shil.external.llm.gemini.dto.FilterInputDto;
import com.example.mwo_do_shil.external.llm.gemini.dto.InputDto;
import com.example.mwo_do_shil.external.weather.WeatherService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.mwo_do_shil.external.llm.gemini.GeminiServiceImpl.toFilterInputList;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final KakaoApiService kakaoApiService;
    private final FirestoreService firestoreService;
    private final WeatherService weatherService;
    private final LLMRouter llmRouter;
    private final Gson gson;
    private final Firestore firestore;

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
     *      3.2 선별된 가게에 대한 자세한 정보를 웹그라운딩을 통해 획득
     *      3.3 추출된 evidence롸 가게들을 바탕으로 llm에게 추천 판단 호출
     */
    public List<RecommendResponseDto> getRecommend(
            String uid,
            String alcohol,
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {

        // -------------kakao local api 호출-------------
        List<KakaoPlaceDto> stores = getStoreList(minX, minY, maxX, maxY);
        String address = stores.get(0).getAddress_name();
        // -------------날씨 api호출-------------
        String weather = weatherService.getCurrentWeather(minX, minY, maxX, maxY);

        //-------------LLM API 호출-------------
        // [step.1]
        // 1차 필터링
        // '가게 id와 가게명'을 json배열로 응답받는다.
        List<FilterInputDto> smaller_stores = toFilterInputList(stores); // 간소화된 가게 리스트 준비(카테고리 포함 dto)
        List<InputDto> filteredStores = filterStoresByLLM(
                alcohol,
                weather,
                address,
                smaller_stores);
        if (filteredStores.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "추천할 가게가 없습니다.");
        }

        /**
         * memo
         *  1️⃣ Firestore에 있는 것 조회
         *  2️⃣ 없는 ID만 LLM 병렬 호출
         *  3️⃣ 모든 LLM 호출 완료 (allOf)
         *  4️⃣ 결과 병합
         *  5️⃣ Firestore에 batch 저장
         *  -[work flow]-
         *  - 1차 필터링에서 얻은 가게들(12곳) 정보가 firestore 있는 정보라면 firestore에서 가져온다.
         *  - firestore에 정보가 일부 있다면 없는 정보들만 2차 getevidenceByLMM(가게 정보 얻기)을 병렬 수행 후 저장
         *  - firestore에 저장되는 형태는 문서 id를 가게 id로, 필드는 whole_info,expire_at으로 수행
         *  - 가져온 정보를 3차(가게 정보를 바탕으로 페어링 맛집 추천 판단)단계를 거치며 마무리
         */

        List<InputDto> noExistIds = new ArrayList<>();
        List<InputForRecommendDto> inputForRecommendDtos = firestoreService.getStores(filteredStores, noExistIds);

        // [step.2] - getEvidenceByLLM()
        // 1차 필터링된 가게를 기준으로한 웹그라운딩을 통해 가게정보 추출 및 firebase에 저장 (llm을 통한 오직 검색만)
        // 이때 llm은 1차 필터링에서 받아온 가게리스트의 size() 만큼 llm api를 병렬 호출
        inputForRecommendDtos.addAll(Objects.requireNonNull(getEvidenceByLLM(alcohol, address, noExistIds)));

        // [step.3]
        // 2차에서 받아온 가게의 정보를 바탕으로 llm이 추천 가게를 판단하게 한다.(웹그라운딩 X)
        List<RecommendPlaceDto> recommendPlaceDtos =
                recommendWithEvidence(
                        alcohol,
                        weather,
                        address,
                        gson.toJson(inputForRecommendDtos));
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
    // divide시에 반올림을 하게되었을때, 오차가 생겨 가게 데이터가 겹칠 수 있다.
    //
    private List<RectDto> divideRectInto16(BigDecimal minX, BigDecimal minY, BigDecimal maxX, BigDecimal maxY) {
        List<RectDto> rects = new ArrayList<>();

        BigDecimal width = maxX.subtract(minX);
        BigDecimal height = maxY.subtract(minY);
        BigDecimal stepX = width.divide(BigDecimal.valueOf(4), 10, RoundingMode.DOWN);  // scale 증가
        BigDecimal stepY = height.divide(BigDecimal.valueOf(4), 10, RoundingMode.DOWN);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                BigDecimal rectMinX = minX.add(stepX.multiply(BigDecimal.valueOf(i)));
                BigDecimal rectMinY = minY.add(stepY.multiply(BigDecimal.valueOf(j)));
                BigDecimal rectMaxX = i < 3 ? rectMinX.add(stepX) : maxX;  // ✅ 마지막은 원본 maxX 사용
                BigDecimal rectMaxY = j < 3 ? rectMinY.add(stepY) : maxY;

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


    //[step.1]
    private List<InputDto> filterStoresByLLM(String alcohol,
                                             String weather,
                                             String address,
                                             List<FilterInputDto> smaller_stores) {
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
                .map(FilterInputDto::toInputDto)
                .toList();
    }

    //[step.2]
    private List<InputForRecommendDto> getEvidenceByLLM(String alcohol,
                                                        String address,
                                                        List<InputDto> noExistIds) {
        // 병렬호출을 위한 CompletableFuture + webclient 사용
        List<CompletableFuture<InputForRecommendDto>> futures =
                noExistIds.stream()
                        .map(inputDto -> llmRouter.route(LLMType.GEMINI).getEvidenceByLLM(new LLMRequest(Map.of(
                                "alcohol", alcohol
                                , "address", address
                        ), inputDto
                        ))).toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        try {
            // 3. 현재 쓰레드에서 모든 비동기 작업이 끝날 때까지 대기
            //    → 병렬 실행은 유지되며, 결과를 사용하기 직전에만 join
            allFutures.join();
            // 4. 각 Future의 결과를 가져와
            List<InputForRecommendDto> results = futures.stream()
                    .map(CompletableFuture::join)// 각 API 호출 결과 획득
                    .collect(Collectors.toList());
//            todo
//             주석해제 필수
//            firestoreService.setStoreWithBatch(results);

            return results;
        } catch (Exception e) {
            log.error("병렬 API 호출 중 오류 발생: {}", e.getMessage());
            return null;
        }

    }


    //[step.3]
    private List<RecommendPlaceDto> recommendWithEvidence(String alcohol,
                                                          String weather,
                                                          String address,
                                                          String storeInfo) {
        // 2차 웹 그라운딩
        String recommended_stores = llmRouter.route(LLMType.GEMINI).recommendWithEvidence(new LLMRequest(Map.of(
                "alcohol", alcohol
                , "weather", weather
                // 검색은 한 지역내에 있는 가게를 기준으로 하기 때문에 주소가 비슷하다.
                // 데이터 다이어트를 위해 상단에 한번만 address를 고정한다.
                , "address", address
        ), storeInfo
        ));

        System.out.println("recommended_stores : " + recommended_stores);
        // String 형태의 json 텍스트를 dto list로 역직렬화
        Type type = new TypeToken<List<RecommendPlaceDto>>() {
        }.getType();

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
