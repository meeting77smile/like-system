package com.meeting_smile.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.meeting_smile.thumb.constant.ThumbConstant;
import com.meeting_smile.thumb.model.entity.Blog;
import com.meeting_smile.thumb.mapper.BlogMapper;
import com.meeting_smile.thumb.model.entity.User;
import com.meeting_smile.thumb.model.vo.BlogVO;
import com.meeting_smile.thumb.service.BlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meeting_smile.thumb.service.ThumbService;
import com.meeting_smile.thumb.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {
    @Autowired
    private UserService userService;

    @Autowired
    @Lazy //用来解决循环引用问题（因为后续会在thumbService中引入blogService)
    private ThumbService thumbService;

    @Autowired RedisTemplate<String,Object> redisTemplate;

    /**
     * 功能描述：根据Blog的id获得BlogVO
     * @param blogId
     * @param request
     * @return
     */
    @Override
    public BlogVO getBlogVOById(long blogId, HttpServletRequest request) {
        Blog blog = this.getById(blogId);
        User loginUser = userService.getLoginUser(request);
        return this.getBlogVO(blog,loginUser);
    }


    /**
     * 功能描述：根据Blog与User生成BlogVO
     * @param blog
     * @param loginUser
     * @return
     */
    private BlogVO getBlogVO(Blog blog,User loginUser) {
        BlogVO blogVO = new BlogVO();
        BeanUtil.copyProperties(blog,blogVO);//copyProperties用于将第一个对象的属性值复制到第二个对象中

        if(loginUser == null){
            return blogVO;
        }

        Boolean exist = thumbService.hasThumb(blog.getId(), loginUser.getId());
        blogVO.setHasThumb(exist);//若exit != false则说明存在对该blog的点赞记录

        return blogVO;
    }

    /**
     * 功能描述：获取博客列表
     * 将 Blog 实体列表转换为 BlogVO（View Object，视图对象）列表，
     * 并标记当前登录用户是否点赞过这些博客
     * @param blogList
     * @param request
     * @return
     */
    @Override
    public List<BlogVO> getBlogVoList(List<Blog> blogList, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);//从session中获取用户
        Map<Long,Boolean> blogIdHasThumbMap = new HashMap<>();
        if (ObjUtil.isNotEmpty(loginUser)) {
            List<Object> blogIdList = blogList.stream().map(blog -> blog.getId().toString()).collect(Collectors.toList());
            // 获取点赞
            List<Object> thumbList = redisTemplate.opsForHash().multiGet(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogIdList);
            for (int i = 0; i < thumbList.size(); i++) {
                if (thumbList.get(i) == null) {
                    continue;
                }
                blogIdHasThumbMap.put(Long.valueOf(blogIdList.get(i).toString()), true);
            }
        }

        //将 Blog 实体列表转换为BlogVO列表(二者的区别就是Blog对象
        return blogList.stream()
                .map(blog -> {
                    BlogVO blogVO = BeanUtil.copyProperties(blog,BlogVO.class);
                    blogVO.setHasThumb(blogIdHasThumbMap.get(blog.getId()));
                    return  blogVO;
                })
                .toList();
    }
}
