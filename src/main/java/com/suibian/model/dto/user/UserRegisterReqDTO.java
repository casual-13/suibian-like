package com.suibian.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户注册提交列表
 */
@Data
@ApiModel("用户注册请求参数")
public class UserRegisterReqDTO {

    @ApiModelProperty("用户账号")
    private String userAccount;

    @ApiModelProperty("用户密码")
    private String userPassword;

    @ApiModelProperty("校验密码")
    private String checkPassword;
}
