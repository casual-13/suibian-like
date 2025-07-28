package com.suibian.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PageParam {

    // 当前页码
    @ApiModelProperty("页码")
    private Long pageNo = 1L;

    // 每页记录数
    @ApiModelProperty("每页记录数")
    private Long pageSize = 10L;

    public PageParam(){

    }

    public PageParam(long pageNo,long pageSize){
        this.pageNo = pageNo < 1 ? 1 : pageNo;
        this.pageSize = pageSize < 1 ? 10 : pageSize;
    }
}
