package com.suibian.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户登录")
public class UserLoginReqDTO {

    @ApiModelProperty(value = "用户账号", required = true)
    private String userAccount;

    @ApiModelProperty(value = "用户密码", required = true)
    private String userPassword;

}
