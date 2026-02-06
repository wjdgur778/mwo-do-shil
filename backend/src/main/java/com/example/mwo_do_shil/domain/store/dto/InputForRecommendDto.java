package com.example.mwo_do_shil.domain.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InputForRecommendDto {
    Long id;
    String n;
    String w;
}
