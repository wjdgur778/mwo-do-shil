package com.example.mwo_do_shil.domain.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RectDto {
    private BigDecimal minX;
    private BigDecimal minY;
    private BigDecimal maxX;
    private BigDecimal maxY;
}