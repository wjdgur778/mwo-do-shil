package com.example.mwo_do_shil.domain.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Result<T> {
    private String message;
    private T data;

}