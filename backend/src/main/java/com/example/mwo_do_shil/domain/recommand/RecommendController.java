package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.common.Result;
import com.example.mwo_do_shil.domain.recommand.dto.RecommendResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {
    @GetMapping("/{category}")
    public ResponseEntity<Result> getRecommend(@PathVariable String category,Double lat,Double lon){
        return ResponseEntity.ok().body(Result.builder()
                .message("")
                .data("")
                .build());
    }
}
