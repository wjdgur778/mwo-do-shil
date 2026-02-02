package com.example.mwo_do_shil.external.kakao;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.recommand.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {
    @Qualifier("KakaoWebClient")
    private final WebClient KakaoWebClient;

    /**
     * memo
     *  webclient를 사용하면 비동기로 호출하게 됨으로 async를 수행하면 오히려 남는 idle 쓰레드가 생기게 됨으로 삭제할 필요가 있다.
     *  이에 대해서는 더욱 자세히게 다뤄야 한다.
     */
//    @Async("kakaoExecutor")
    public CompletableFuture<KakaoSearchResponseDto> searchPlacesAsync(RectDto rect) {
        try {
            // 키워드는 :"맛집" 으로 통일
            Mono<KakaoSearchResponseDto> responseMono = KakaoWebClient.get()
                    .uri(urlBuilder->urlBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", "맛집")
                            .queryParam(
                                    "rect",
                                    String.format(
                                            "%s,%s,%s,%s",
                                            rect.getMinX(),
                                            rect.getMinY(),
                                            rect.getMaxX(),
                                            rect.getMaxY()
                                    )
                            )
                            .build())
                    .retrieve()
                    .bodyToMono(KakaoSearchResponseDto.class)
                    .timeout(Duration.ofSeconds(10));
            
            return responseMono
                    .doOnSuccess(response -> log.info("카카오 API 호출 성공: {}개 장소 검색됨", response.getDocuments().size()))
                    .doOnError(error -> log.error("카카오 API 호출 실패: rect={}, error={}", rect, error.getMessage()))
                    .onErrorReturn(
                            KakaoSearchResponseDto.builder()
                                    .documents(Collections.emptyList())
                                    .meta(KakaoSearchResponseDto.Meta.builder()
                                            .total_count(0)
                                            .pageable_count(0)
                                            .is_end(true)
                                            .build())
                                    .build()
                    )
                    .toFuture();
                    
        } catch (Exception e) {
            log.error("카카오 API 호출 실패: rect={}, error={}", rect, e.getMessage());
            return CompletableFuture.completedFuture(
                    KakaoSearchResponseDto.builder()
                            .documents(Collections.emptyList())
                            .meta(KakaoSearchResponseDto.Meta.builder()
                                    .total_count(0)
                                    .pageable_count(0)
                                    .is_end(true)
                                    .build())
                            .build()
            );
        }
    }
}