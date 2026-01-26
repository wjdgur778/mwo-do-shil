package com.example.mwo_do_shil.external.weather;

import com.example.mwo_do_shil.external.weather.dto.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${weather.api.key}")
    private String weatherApiKey;

    /**
     *
     * @param minX
     * @param minY
     * @param maxX
     * @param maxY
     * @return
     */
    public String getCurrentWeather(
            BigDecimal minX,
            BigDecimal minY,
            BigDecimal maxX,
            BigDecimal maxY) {
        BigDecimal lon = center(minX, maxX);
        BigDecimal lat = center(minY, maxY);
        try {
            String url = "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=" + lat +
                    "&lon=" + lon +
                    "&appid=" + weatherApiKey +
                    "&units=metric";


            WeatherResponseDto result = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(WeatherResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("날씨 API 성공: {}°C, {}",
                             response.getMain().getFeelsLike(),response.getWeather().get(0).getDescription()))
                    .doOnError(error -> log.error("날씨 API 실패: lon={}, lat={}, error={}", lon, lat, error.getMessage()))
                    .onErrorReturn(createDefaultWeather())
                    .block();  // 동기 호출로 변경

            return result.getWeather().get(0).getDescription() +  " / 체감온도: "+result.getMain().getFeelsLike();
        } catch (Exception e) {
            log.error("날씨 API 호출 예외: lon={}, lat={}, error={}", lon, lat, e.getMessage());
            return "무난함";
        }
    }

    private WeatherResponseDto createDefaultWeather() {
        return   WeatherResponseDto.builder()
                .main(WeatherResponseDto.MainInfo.builder()
                        .feelsLike(20.0)
                        .build())
                .weather(List.of(
                        WeatherResponseDto.WeatherItem.builder()
                                .description("맑음")
                                .build()
                ))
                .build();
    }

    public static BigDecimal center(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(BigDecimal.valueOf(2), 6, RoundingMode.HALF_UP);
    }


}
