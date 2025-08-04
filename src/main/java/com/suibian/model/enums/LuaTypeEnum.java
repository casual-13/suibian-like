package com.suibian.model.enums;

import lombok.Getter;

@Getter
public enum LuaTypeEnum {

    // 成功
    SUCCESS(1L),

    // 失败
    FAIL(-1L);

    private final Long value;

    LuaTypeEnum(Long value) {
        this.value = value;
    }
}
