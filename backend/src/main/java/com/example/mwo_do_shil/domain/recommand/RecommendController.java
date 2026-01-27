package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/recommend")
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/{alcohol}")
    public ResponseEntity<Result> getRecommend(
            @PathVariable(name = "alcohol") String alcohol,
            @RequestParam BigDecimal minX,
            @RequestParam BigDecimal minY,
            @RequestParam BigDecimal maxX,
            @RequestParam BigDecimal maxY) {
        return ResponseEntity.ok().body(Result.builder()
                .message("")
                .data(recommendService.getRecommend(alcohol, minX, minY, maxX, maxY))
                .build());
    }

}
