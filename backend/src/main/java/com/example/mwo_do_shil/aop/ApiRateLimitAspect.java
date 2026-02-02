package com.example.mwo_do_shil.aop;

import com.example.mwo_do_shil.auth.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiRateLimitAspect {
    private final RateLimitService rateLimitService;
    // I32yJqndB6YCMIEXw3dbI4bibJ72
    private final String myId = "qsffop5RRVb9ovpeQWDkztqfyxz2 Lra0YJ84vhbNjlN84v3KzK1vdYz1";

    @Around("@annotation(com.example.mwo_do_shil.annotation.RateLimitedApi)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("호출량 제한 AOP 호출 성공 ");
        // 1. 사용자 ID 추출 (Firebase Auth 또는 헤더)
        String uid = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();

        // 2. 호출 전 한도 체크
        // 간이로 만든 나만 통과하기 로직
        if (!myId.contains(uid) && !rateLimitService.checkRateLimit(uid)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "호출 한도 초과");
        }

        try {
            // 3. API 호출
            Object result = joinPoint.proceed();

            // 4. 성공 → 카운트 증가 (비동기)
            rateLimitService.incrementUsage(uid);

            return result;
        } catch (Exception e) {
            // 실패 시 카운트 증가 안함 ✅
            throw e;
        }
    }
}
