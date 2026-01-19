package com.example.mwo_do_shil.external;

import com.example.mwo_do_shil.domain.recommand.dto.KakaoPlaceDto;
import com.example.mwo_do_shil.domain.recommand.dto.KakaoSearchResponseDto;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Async("kakaoExecutor")
    public CompletableFuture<KakaoSearchResponseDto> searchPlacesAsync(RectDto rect) {
        try {
            // 키워드는 :"맛집" 으로 통일
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json" +
                    "?query= 맛집" +
                    "&rect=" + String.format("%s,%s,%s,%s", 
                            rect.getMinX(), rect.getMinY(), rect.getMaxX(), rect.getMaxY());
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                KakaoSearchResponseDto searchResponse = objectMapper.readValue(
                        response.getBody(),
                        KakaoSearchResponseDto.class
                );
                log.info("카카오 API 호출 성공: {}개 장소 검색됨", searchResponse.getDocuments().size());
                return CompletableFuture.completedFuture(searchResponse);
            }
            
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