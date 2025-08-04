package com.suibian.model.enums;

import lombok.Getter;

@Getter
public enum ThumbTypeEnum {

    // 点赞
    INCR(1),

    // 取消点赞
    DECR(-1),

    // 无操作
    NON(0);

    private final int value;

    ThumbTypeEnum(int value) {
        this.value = value;
    }
}
