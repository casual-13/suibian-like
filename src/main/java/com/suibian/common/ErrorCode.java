package com.suibian.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

//    SUCCESS(0, "ok"),
//    PARAMS_ERROR(40000, "请求参数错误"),
//    NOT_LOGIN_ERROR(40100, "未登录"),
//    NO_AUTH_ERROR(40101, "无权限"),
//    NOT_FOUND_ERROR(40400, "请求数据不存在"),
//    FORBIDDEN_ERROR(40300, "禁止访问"),
//    OPERATION_ERROR(50001, "操作失败"),

    // ========== 客户端错误段 ==========
    BAD_REQUEST(400, "请求参数不正确"),
    BAD_REQUEST_PARAMS_ERROR(400, "请求参数不正确,{}"),
    UNAUTHORIZED(401, "账号未登录"),
    FORBIDDEN(403, "没有该操作权限"),
    NOT_FOUND(404, "请求未找到"),
    METHOD_NOT_ALLOWED(405, "请求方法不正确"),
    LOCKED(423, "请求失败，请稍后重试"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后重试"),
    BAD_REQUEST_PARAMS(400, "请求参数不能为空"),
    BAD_REQUEST_PARAM_ERROR(400, "请求参数错误"),

    // ========== 服务端错误段 ==========
    INTERNAL_SERVER_ERROR(500, "系统异常"),


    // ========== 自定义错误段 ==========
    UNKNOWN(999, "未知错误"),
    PASSWORD_NOT_MATCH(4001, "两次输入的密码不一致"),
    PASSWORD_ERROR(4000, "密码错误"),
    PASSWORD_LENGTH_NOT_ENOUGH(4002, "密码长度不能小于8"),

    USER_NAME_REPEAT(4091, "账号重复"),

    USER_REGISTER_FAIL(4221, "注册失败"),

    USER_NOT_EXIST(4041, "用户不存在"),

    USER_NOT_NORMAL(4031, "此账号已被冻结"),

    ADD_FAIL(4222, "添加失败"),
    UPDATE_FAIL(4223, "更新失败"),
    DELETE_FAIL(4224, "删除失败"),
    USER_PASSWORD_ERROR(4225, "原密码错误"),

    USER_LIKE_ERROR(4226, "您已点赞"),
    USER_UNLIKE_ERROR(4227, "您还未点赞"),
    ;

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
