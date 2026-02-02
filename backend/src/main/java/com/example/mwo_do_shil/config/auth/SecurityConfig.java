package com.example.mwo_do_shil.config.auth;

import com.example.mwo_do_shil.filter.FirebaseTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final FirebaseTokenFilter firebaseTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable) // API 서버이므로 CSRF 비활성화
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 미사용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/recommend/**").authenticated() // 인증 필수
                        .anyRequest().permitAll() // 나머지는 허용
                )
                // Firebase 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * memo
     *  WebMvcConfigure 대신 SecurityConfig 내부에서 cors설정을 하는 이유는
     *  security의 보안 필터 체인(security filter chain)이 서블릿 컨테이너(webmvc)보다 먼저 요청을 가로채기 때문
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 1. 허용할 도메인 설정
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "https://mwo-do-shil.vercel.app/"));
        // 2. 허용할 HTTP 메서드 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 3. 허용할 헤더 설정 -> 모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));
        // 4. 자격 증명 허용 -> 쿠키나 인증 헤더를 사용하는 요청을 허용
        config.setAllowCredentials(true);
        // 5. 서버가 보낸 "Authorization" 헤더를 프론트엔드(JS)에서 읽을 수 있게 허용
        config.addExposedHeader("Authorization");
        // 6. 설정 적용 범위 지정 -> 모든 경로("/**")에 대해 위에서 설정한 config를 적용.
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
