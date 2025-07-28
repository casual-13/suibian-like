package com.suibian.model.dto.blog;

import com.suibian.common.PageParam;
import com.suibian.common.PageResult;
import com.suibian.model.vo.BlogVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@ApiModel("博客分页请求参数")
@EqualsAndHashCode(callSuper = false)
public class BlogPageReqDTO extends PageParam implements Serializable {

    @ApiModelProperty("博客名称")
    private String blogName;
}
