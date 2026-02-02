package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.annotation.RateLimitedApi;
import com.example.mwo_do_shil.auth.RateLimitService;
import com.example.mwo_do_shil.domain.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/recommend")
public class RecommendController {

    private final RecommendService recommendService;
    private final RateLimitService rateLimitService;

    @GetMapping("/{alcohol}")
    @RateLimitedApi
    public ResponseEntity<Result> getRecommend(
            @AuthenticationPrincipal String uid,
            @PathVariable(name = "alcohol") String alcohol,
            @RequestParam BigDecimal minX,
            @RequestParam BigDecimal minY,
            @RequestParam BigDecimal maxX,
            @RequestParam BigDecimal maxY) {

        return ResponseEntity.ok().body(Result.builder()
                .message("맛집 추천 조회 api 성공!!")
                .data(recommendService.getRecommend(uid,alcohol, minX, minY, maxX, maxY))
                .build());
    }

    @GetMapping("/remaining")
    public ResponseEntity<Result> getRemainingCount(@AuthenticationPrincipal String uid) {
        // RateLimitService에서 Firestore를 조회해 남은 횟수 계산
        int remaining = rateLimitService.getRemainingUsage(uid);
        log.info("남은 호출 횟수 조회 성공");
        return ResponseEntity.ok().body(Result.builder()
                .message("")
                .data(remaining)
                .build());
    }


}
