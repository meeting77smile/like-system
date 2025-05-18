package com.meeting_smile.thumb.service;

import com.meeting_smile.thumb.model.dto.thumb.DoThumbRequest;
import com.meeting_smile.thumb.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 功能描述：点赞操作
     * @param doThumbRequest
     * @param request
     * @return
     */
    Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request);

    /**
     * 功能描述：取消点赞
     */
    Boolean undoThumb(DoThumbRequest doThumbRequest,HttpServletRequest request);

    /**
     * 功能描述：判断当前用户是否已经点赞
     * @param blogId
     * @param userId
     * @return
     */
    Boolean hasThumb(Long blogId,Long userId);
}
