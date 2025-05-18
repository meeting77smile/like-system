package com.meeting_smile.thumb.service;

import com.meeting_smile.thumb.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.meeting_smile.thumb.model.vo.BlogVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
public interface IBlogService extends IService<Blog> {
    BlogVO getBlogVOById(long blogId, HttpServletRequest request);

    /**
     * 功能描述：获取博客列表
     */
    List<BlogVO> getBlogVoList(List<Blog> blogList,HttpServletRequest request);
}
