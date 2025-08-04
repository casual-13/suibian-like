package com.suibian.util;

import com.suibian.constant.ThumbConstant;

public class RedisKeyUtil {

    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    /**
     * 获取临时点赞key
     * @param time 点赞时间
     * @return 临时点赞key
     */
    public static String getTempThumbKey(String time) {
        return String.format(ThumbConstant.TEMP_THUMB_KEY_PREFIX, time);
    }
}
