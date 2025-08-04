package com.suibian.model.dto.blog;

import com.suibian.common.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@Tag(name = "博客分页请求参数")
@EqualsAndHashCode(callSuper = false)
public class BlogPageReqDTO extends PageParam implements Serializable {

    @Schema(name = "blogName", description = "博客名称")
    private String blogName;
}
