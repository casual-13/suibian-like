package com.suibian.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PageParam {

    // 当前页码
    @Schema(name = "pageNo", description = "页码，默认为1", example = "1")
    private Long pageNo = 1L;

    // 每页记录数
    @Schema(name = "pageSize", description = "每页记录数，默认为10", example = "10")
    private Long pageSize = 10L;

    public PageParam() {

    }

    public PageParam(long pageNo, long pageSize) {
        this.pageNo = pageNo < 1 ? 1 : pageNo;
        this.pageSize = pageSize < 1 ? 10 : pageSize;
    }
}
