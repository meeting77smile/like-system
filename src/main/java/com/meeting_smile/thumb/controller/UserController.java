package com.meeting_smile.thumb.controller;
import com.meeting_smile.thumb.common.ResultUtils;
import com.meeting_smile.thumb.constant.UserConstant;
import jakarta.servlet.http.HttpServletRequest;
import com.meeting_smile.thumb.common.BaseResponse;
import com.meeting_smile.thumb.model.entity.User;
import com.meeting_smile.thumb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 功能描述：用户登录
     */
    @GetMapping("/login")
    public BaseResponse<User> login(long userId, HttpServletRequest request) {
        User user = userService.getById(userId);
        request.getSession().setAttribute(UserConstant.LOGIN_USER,user);
        return ResultUtils.success(user);
    }

    /**
     * 功能描述：从Session中获取登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<User> getLoginUser(HttpServletRequest request){
        User loginUser = (User)request.getSession().getAttribute(UserConstant.LOGIN_USER);
        return ResultUtils.success(loginUser);
    }
}
