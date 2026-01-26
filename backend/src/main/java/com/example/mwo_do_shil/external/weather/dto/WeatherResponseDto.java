package com.example.mwo_do_shil.external.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeatherResponseDto {

    private List<WeatherItem> weather;
    private MainInfo main;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class WeatherItem {
        private String description;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MainInfo {
        @JsonProperty("feels_like")
        private Double feelsLike;
    }

}
