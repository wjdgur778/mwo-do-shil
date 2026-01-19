package com.example.mwo_do_shil.domain.recommand;

import com.example.mwo_do_shil.domain.common.Result;
import com.example.mwo_do_shil.domain.recommand.dto.RectDto;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recommend")
public class RecommendController {

    private final RecommendService recommendService;


    @GetMapping("/{alcohol}")
    public ResponseEntity<Result> getRecommend(
            @Parameter(name = "alcohol",description = "주종 (소주, 와인, 맥주)",required = true)
            @PathVariable(name = "alcohol") String alcohol,
            @RequestParam double minX,
            @RequestParam double minY,
            @RequestParam double maxX,
            @RequestParam double maxY) {
        return ResponseEntity.ok().body(Result.builder()
                .message("")
                .data(recommendService.getRecommend(alcohol, minX,minY,maxX,maxY))
                .build());
    }
}
