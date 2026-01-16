package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.recommand.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import com.example.mwo_do_shil.domain.recommand.dto.RecommendResponseDto;
import com.example.mwo_do_shil.external.KakaoApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {
    
    private final KakaoApiService kakaoApiService;
    /**
     * rect
     * @return
     */
    public RecommendResponseDto getRecommend(
            String category,
            Double left_x,
            Double left_y,
            Double right_x,
            Double right_y)
    {
        // kakao local api 호출
        List<KakaoPlaceDto> stores = getStoreList(left_x,left_y,right_x,right_x);

        // DB에서 이미 추천을 받아 기록되어있는 place들을 검색




    }

    private List<KakaoPlaceDto> getStoreList(
            Double left_x,
            Double left_y,
            Double right_x,
            Double right_y){
        // 1. kakao local api 호출을 통해 store의 리스트를 받아온다.
        // 1.1 이때 lat과 lon을 16등분해서 kakao local api 호출을 한다.
        List<RectDto> rects = divideRectInto16(left_x, left_y, right_x, right_y);

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


    private List<RectDto> divideRectInto16(Double minX, Double minY, Double maxX, Double maxY) {
        List<RectDto> rects = new ArrayList<>();
        
        double xStep = (maxX - minX) / 4;
        double yStep = (maxY - minY) / 4;
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Double rectMinX = minX + (i * xStep);
                Double rectMinY = minY + (j * yStep);
                Double rectMaxX = rectMinX + xStep;
                Double rectMaxY = rectMinY + yStep;
                
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
