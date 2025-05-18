package com.meeting_smile.thumb.service;

import com.meeting_smile.thumb.model.entity.User;
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
public interface IUserService extends IService<User> {
    public User getLoginUser(HttpServletRequest request);
}
