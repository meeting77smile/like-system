package com.meeting_smile.thumb.service.impl;

import com.meeting_smile.thumb.constant.UserConstant;
import com.meeting_smile.thumb.model.entity.User;
import com.meeting_smile.thumb.mapper.UserMapper;
import com.meeting_smile.thumb.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute(UserConstant.LOGIN_USER);
    }
}
