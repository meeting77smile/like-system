package com.meeting_smile.thumb.util;

import com.meeting_smile.thumb.constant.ThumbConstant;

/**
 * 功能描述：将常用的key封装为方法获取
 */
public class RedisKeyUtil {

    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    /**
     * 获取 临时点赞记录 key
     */
    public static String getTempThumbKey(String time) {
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }

}
