package com.example.mwo_do_shil.domain.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoSearchResponseDto {
    private List<KakaoPlaceDto> documents;
    private Meta meta;
    
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Meta {
        private int total_count;
        private int pageable_count;
        private boolean is_end;
    }
}