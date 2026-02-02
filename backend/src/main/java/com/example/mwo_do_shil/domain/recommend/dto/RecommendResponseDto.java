package com.example.mwo_do_shil.domain.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendResponseDto {

    private KakaoPlaceDto place;
    private String reason;
    private Integer score;
}
