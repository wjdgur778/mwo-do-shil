package com.example.mwo_do_shil.domain.recommand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "카카오 장소 정보")
public class KakaoPlaceDto {
    @Schema(description = "장소 ID")
    private Long id;
    
    @JsonProperty("place_name")
    @Schema(description = "장소명", example = "강남역 맛집")
    private String placeName;
    
    @JsonProperty("category_name")
    @Schema(description = "카테고리명", example = "음식점 > 한식")
    private String categoryName;
    
    @JsonProperty("address_name")
    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String addressName;
    
    @JsonProperty("phone")
    @Schema(description = "전화번호", example = "02-123-4567")
    private String phone;
    
    @JsonProperty("x")
    @Schema(description = "X 좌표 (경도)", example = "127.123456")
    private String x;
    
    @JsonProperty("y")
    @Schema(description = "Y 좌표 (위도)", example = "37.123456")
    private String y;
    
    @JsonProperty("place_url")
    @Schema(description = "장소 URL", example = "http://place.map.kakao.com/123456")
    private String placeUrl;
    
    @JsonProperty("distance")
    @Schema(description = "거리 (미터)", example = "150")
    private String distance;
}