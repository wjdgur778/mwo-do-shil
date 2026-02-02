package com.example.mwo_do_shil.domain.recommend.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPlaceDto {
    private Long id;
    private String place_name;
    private String category_name;
    private String address_name;
    private String phone;
    private String x;
    private String y;
    private String place_url;
    private String distance;
}