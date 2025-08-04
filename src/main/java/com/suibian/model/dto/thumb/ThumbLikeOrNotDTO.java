package com.suibian.model.dto.thumb;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Tag(name = "点赞参数")
@Data
public class ThumbLikeOrNotDTO {

    @Schema(name = "blogId", description = "博客ID")
    private Long blogId;
}
