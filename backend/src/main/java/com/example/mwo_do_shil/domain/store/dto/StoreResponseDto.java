package com.example.mwo_do_shil.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreResponseDto {
    private Long id;
    private String name;
    private String address;
    private String reason;
    private Integer score;
    private BigDecimal x;
    private BigDecimal y;
}
