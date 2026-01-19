package com.example.mwo_do_shil.domain.recommand.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "추천 장소 응답")
public class RecommendResponseDto {
    @Schema(description = "추천 장소 목록")
    private List<KakaoPlaceDto> places;
    
    @Schema(description = "총 장소 수", example = "15")
    private int totalCount;
    
    @Schema(description = "검색 카테고리", example = "맛집")
    private String category;
}
