package com.example.mwo_do_shil.external.kakao;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class KaKaoWebClientConfig {
    /**
     *
     * @param kakaoApiKey
     * @return
     */
    @Bean("KakaoWebClient")
    public WebClient kakaoWebClient(
            @Value("${kakao.api.key}") String kakaoApiKey
    ) {
        ConnectionProvider provider =
                ConnectionProvider.builder("kakao-pool")
                        .maxConnections(256)                 // ⭐ 동시 호출 수
                        .pendingAcquireMaxCount(2048)        // 대기 큐
                        .pendingAcquireTimeout(Duration.ofSeconds(3))
                        .build();

        HttpClient httpClient = HttpClient.create(provider)
                .responseTimeout(Duration.ofSeconds(10));


        return WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
