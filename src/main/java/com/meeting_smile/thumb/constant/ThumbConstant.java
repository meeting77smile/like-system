package com.meeting_smile.thumb.constant;

/**
 * @author pine
 */
public interface ThumbConstant {

    /**
     * 用户点赞 hash key
     */
    String USER_THUMB_KEY_PREFIX = "thumb:";

    //约定为0时代表当前未点赞
    Long UN_THUMB_CONSTANT = 0L;

    /**
     * 临时 点赞记录 key
     */
    String TEMP_THUMB_KEY_PREFIX = "thumb:temp:%s";

}
