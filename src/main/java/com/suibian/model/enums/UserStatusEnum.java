package com.suibian.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@AllArgsConstructor
@Getter
public enum UserStatusEnum {

    NORMAL("正常",0),
    DISABLE("异常",1);

    private final String text;
    private final Integer value;


    /**
     * 根据文本获取对应的用户状态枚举。
     *
     * @param text 用户状态的文本表示。
     * @return 对应的用户状态枚举，如果找不到则返回null。
     */
    public static UserStatusEnum getEnumByText(String text) {
        if (text == null) {
            return null;
        }
        for (UserStatusEnum statusEnum : values()) {
            if (text.equals(statusEnum.getText())) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 根据数值获取对应的用户状态枚举。
     *
     * @param value 用户状态的数值表示。
     * @return 对应的用户状态枚举，如果找不到则返回null。
     */
    public static UserStatusEnum getEnumByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserStatusEnum statusEnum : values()) {
            if (value.equals(statusEnum.getValue())) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 判断给定的数值是否表示正常状态。
     *
     * @param value 用户状态的数值表示。
     * @return 如果给定数值表示正常状态，则返回true，否则返回false。
     */
    public static boolean isNormal(Integer value) {
        return NORMAL.getValue().equals(value);
    }

    public static boolean isDisable(Integer value) {
        return DISABLE.getValue().equals(value);
    }
}
