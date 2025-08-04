package com.suibian.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Tag(name = "用户登录VO")
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 id
     */
    @Schema(name = "用户 id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(name = "用户昵称")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(name = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(name = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(name = "用户角色：user/admin")
    private String userRole;

    /**
     * 创建时间
     */
    @Schema(name = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(name = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

}
