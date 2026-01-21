package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.recommand.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import com.example.mwo_do_shil.domain.recommand.dto.RecommendResponseDto;
import com.example.mwo_do_shil.external.KakaoApiService;
import com.example.mwo_do_shil.external.llm.LLMRequest;
import com.example.mwo_do_shil.external.llm.LLMRouter;
import com.example.mwo_do_shil.external.llm.LLMService;
import com.example.mwo_do_shil.external.llm.LLMType;
import com.example.mwo_do_shil.external.llm.prompt.PromptRenderer;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final KakaoApiService kakaoApiService;
    private final PromptRenderer promptRenderer;
    private final RecommendRepository recommendRepository;
    private final LLMRouter llmRouter;
    private final Gson gson;
    /**
     * rect
     *
     * @return
     */
    public RecommendResponseDto getRecommend(
            String alcohol,
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {
        // kakao local api 호출
//        List<KakaoPlaceDto> stores = getStoreList(minX, minY, maxX, maxY);
        List<KakaoPlaceDto> stores = List.of(
                KakaoPlaceDto.builder()
                        .address_name("경기 하남시 망월동 1125")
                        .place_name("음식점")
                        .category_name("음식점 > 일식 > 돈까스,우동")
                        .distance("")
                        .id(2074111412L)
                        .phone("0503-7152-9277")
                        .place_name("다이몬규카츠")
                        .place_url("http://place.map.kakao.com/2074111412")
                        .address_name("경기 하남시 미사강변동로 100-1")
                        .x("127.19320884501727")
                        .y("37.5639740622423")
                        .build(),

                KakaoPlaceDto.builder()
                        .address_name("경기 하남시 망월동 1100")
                        .place_name("음식점")
                        .category_name("음식점 > 일식")
                        .distance("")
                        .id(179237040L)
                        .phone("")
                        .place_name("오토시")
                        .place_url("http://place.map.kakao.com/179237040")
                        .address_name("경기 하남시 미사강변동로 95")
                        .x("127.1920371194806")
                        .y("37.563876864498795")
                        .build(),

                KakaoPlaceDto.builder()
                        .address_name("경기 하남시 망월동 1125")
                        .place_name("음식점")
                        .category_name("음식점 > 일식 > 초밥,롤")
                        .distance("")
                        .id(1824273719L)
                        .phone("031-794-4447")
                        .place_name("스시코호시 미사")
                        .place_url("http://place.map.kakao.com/1824273719")
                        .address_name("경기 하남시 미사강변동로 100-1")
                        .x("127.193600436917")
                        .y("37.5639617078272")
                        .build(),

                KakaoPlaceDto.builder()
                        .address_name("경기 하남시 망월동 1106")
                        .place_name("카페")
                        .category_name("음식점 > 카페 > 커피전문점 > 빽다방")
                        .distance("")
                        .id(1829132512L)
                        .phone("031-796-2329")
                        .place_name("빽다방 하남미사역점")
                        .place_url("http://place.map.kakao.com/1829132512")
                        .address_name("경기 하남시 미사강변동로 85")
                        .x("127.19188086899027")
                        .y("37.562531932790755")
                        .build()
        );
        // todo
        //  DB에서 이미 추천을 받아 기록되어있는 place들을 검색 [캐싱 전략 바로 세우기]


        // 이미 추천된 가게가 5개 이상이 아니라면 llm api 호출
        String weather = "맑음";
        gson.toJson(stores);

        String result = llmRouter.route(LLMType.GEMINI).generate(new LLMRequest(Map.of(
                "alcohol",alcohol
                ,"weather", weather
        )
                ,stores
        ));


        return RecommendResponseDto.builder()
                .places(stores)
                .tmpText(result)
                .build();
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
