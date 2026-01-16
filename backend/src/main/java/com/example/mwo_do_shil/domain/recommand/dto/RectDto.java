package com.example.mwo_do_shil.domain.recommand.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RectDto {
    private Double minX;
    private Double minY;
    private Double maxX;
    private Double maxY;
}