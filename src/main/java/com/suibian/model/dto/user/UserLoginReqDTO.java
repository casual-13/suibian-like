package com.suibian.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Data
@Tag(name = "用户登录")
public class UserLoginReqDTO {

    @Schema(name = "用户账号")
    private String userAccount;

    @Schema(name = "用户密码")
    private String userPassword;

}
