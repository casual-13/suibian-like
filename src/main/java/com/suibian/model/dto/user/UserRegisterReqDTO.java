package com.suibian.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

/**
 * 用户注册提交列表
 */
@Data
@Tag(name = "用户注册请求参数")
public class UserRegisterReqDTO {

    @Schema(name = "用户账号")
    private String userAccount;

    @Schema(name = "用户密码")
    private String userPassword;

    @Schema(name = "校验密码")
    private String checkPassword;
}
