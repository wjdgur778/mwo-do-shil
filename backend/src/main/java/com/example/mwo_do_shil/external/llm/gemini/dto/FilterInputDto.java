package com.example.mwo_do_shil.external.llm.gemini.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilterInputDto {
    private Long id;
    private String n;
    private String c;


    // 2차 그라운딩 에이전트에게 전달할 최소한의 정보로 변환
    public static InputDto toInputDto(FilterInputDto filterInputDto) {
        return  InputDto.builder()
                .id(filterInputDto.id)
                .n(filterInputDto.n)
                .build();
    }
}
