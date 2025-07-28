package com.suibian.model.dto.thumb;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("点赞参数")
@Data
public class ThumbLikeOrNotDTO {

    @ApiModelProperty("博客id")
    private Long blogId;
}
