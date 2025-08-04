package com.suibian.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserSimpleVo {

    @Schema(name = "id")
    private Long id;

    @Schema(name = "用户昵称")
    private String userName;

    @Schema(name = "用户头像")
    private String userAvatar;

    @Schema(name = "用户简介")
    private String userProfile;

}