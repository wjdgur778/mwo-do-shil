package com.example.mwo_do_shil.domain.recommand.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "좌표 범위 정보")
public class RectDto {
    @Schema(description = "최소 X 좌표 (경도)", example = "127.18927471609899")
    private Double minX;
    
    @Schema(description = "최소 Y 좌표 (위도)", example = "37.56137655855616")
    private Double minY;
    
    @Schema(description = "최대 X 좌표 (경도)", example = "127.19667376910273")
    private Double maxX;
    
    @Schema(description = "최대 Y 좌표 (위도)", example = "37.56451794798859")
    private Double maxY;
}