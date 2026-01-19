package com.example.mwo_do_shil.external.kakao;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.recommand.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {
    
    private final WebClient webClient;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Async("kakaoExecutor")
    public CompletableFuture<KakaoSearchResponseDto> searchPlacesAsync(RectDto rect) {
        try {
            // 키워드는 "맛집"으로 통일
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json" +
                    "?query= 맛집" +
                    "&rect=" + String.format("%s,%s,%s,%s", 
                            rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
            
            Mono<KakaoSearchResponseDto> responseMono = webClient.get()
                    .uri(url)
                    .header("Authorization", "KakaoAK " + kakaoApiKey)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(KakaoSearchResponseDto.class)
                    .doOnSuccess(response -> log.info("카카오 API 호출 성공: {}개 장소 검색됨", 
                            response.getDocuments() != null ? response.getDocuments().size() : 0))
                    .doOnError(error -> log.error("카카오 API 호출 실패: rect={}, error={}", rect, error.getMessage()));
            
            // Mono를 CompletableFuture로 변환
            return responseMono.toFuture()
                    .exceptionally(throwable -> {
                        log.error("카카오 API 호출 실패: rect={}, error={}", rect, throwable.getMessage());
                        return KakaoSearchResponseDto.builder()
                                .documents(Collections.emptyList())
                                .meta(KakaoSearchResponseDto.Meta.builder()
                                        .total_count(0)
                                        .pageable_count(0)
                                        .is_end(true)
                                        .build())
                                .build();
                    });
            
        } catch (Exception e) {
            log.error("카카오 API 호출 중 예외 발생: rect={}, error={}", rect, e.getMessage());
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