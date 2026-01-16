package com.example.mwo_do_shil.domain.recommand.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPlaceDto {
    private Long id;
    private String placeName;
    private String categoryName;
    private String addressName;
    private String phone;
    private String x;
    private String y;
    private String placeUrl;
    private String distance;
}