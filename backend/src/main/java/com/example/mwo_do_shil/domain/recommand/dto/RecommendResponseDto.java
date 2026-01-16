package com.example.mwo_do_shil.domain.recommand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendResponseDto {
    private List<KakaoPlaceDto> places;
    private int totalCount;
    private String category;
}
