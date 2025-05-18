package com.meeting_smile.thumb.controller;


import com.meeting_smile.thumb.common.BaseResponse;
import com.meeting_smile.thumb.common.ResultUtils;
import com.meeting_smile.thumb.model.entity.Blog;
import com.meeting_smile.thumb.model.vo.BlogVO;
import com.meeting_smile.thumb.service.IBlogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    @Autowired
    private IBlogService blogService;

    @GetMapping("/get")
    public BaseResponse<BlogVO> get(long blogId, HttpServletRequest request) {
        BlogVO blogVO = blogService.getBlogVOById(blogId,request);
        return ResultUtils.success(blogVO);
    }

    /**
     * 功能描述：批量查询获取博客列表
     */
    @GetMapping("/list")
    public BaseResponse<List<BlogVO>> list(HttpServletRequest request) {
        List<Blog> blogList = blogService.list();//查询所有的博客
        List<BlogVO> blogVOList = blogService.getBlogVoList(blogList,request);//将blog列表转换为blogVO列表
        return ResultUtils.success(blogVOList);
    }
}
