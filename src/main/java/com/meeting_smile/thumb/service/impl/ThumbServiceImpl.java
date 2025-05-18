package com.meeting_smile.thumb.service.impl;

import com.meeting_smile.thumb.constant.ThumbConstant;
import com.meeting_smile.thumb.model.dto.thumb.DoThumbRequest;
import com.meeting_smile.thumb.model.entity.Blog;
import com.meeting_smile.thumb.model.entity.Thumb;
import com.meeting_smile.thumb.mapper.ThumbMapper;
import com.meeting_smile.thumb.model.entity.User;
import com.meeting_smile.thumb.service.IBlogService;
import com.meeting_smile.thumb.service.IThumbService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meeting_smile.thumb.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author meeting_smile
 * @since 2025-05-13
 */
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements IThumbService {

    private final IUserService userService;

    private final IBlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String,Object> redisTemplate;

    /**
     * 功能描述：点赞
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if (doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {

            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                boolean exists = this.hasThumb(blogId,loginUser.getId());
                if (exists) {
                    throw new RuntimeException("用户已点赞");
                }

                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();

                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);

                boolean success = update && this.save(thumb);
                //在点赞成功后将数据写到Redis
                if(success) {
                    redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),blogId.toString(),thumb.getId());
                }
                // 更新成功才执行
                return success;
                // 更新成功才执行
                //return update && this.save(thumb);
            });
        }
    }


    /**
     * 功能描述：取消点赞
     * @param doThumbRequest
     * @param request
     * @return
     */
    @Override
    public Boolean undoThumb(DoThumbRequest doThumbRequest, HttpServletRequest request) {
        if(doThumbRequest == null || doThumbRequest.getBlogId() == null) {
            throw new RuntimeException("参数错误");
        }
        User loginUser = userService.getLoginUser(request);
        //加锁：防止同一用户并发操作导致数据不一致
        //如用户快速连续点击"取消点赞"按钮，两个线程同时执行取消操作能造成博客点赞数被多次减1
        synchronized (loginUser.getId().toString().intern()) {//通过intern()获取字符串常量池中的引用

            //编程式事务
            //使用transactionTemplate创建编程式事务
            //确保数据库操作的原子性（要么全部成功，要么全部回滚）
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                Object thumbIdObj = redisTemplate.opsForHash().get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(),blogId.toString());
                if(thumbIdObj == null) {
                    throw new RuntimeException("用户未点赞");
                }
                Long thumbId = Long.valueOf(thumbIdObj.toString());
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId,blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean success = update && this.removeById(thumbId);

                //点赞记录从Redis删除
                if(success) {
                    redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(),blogId.toString());
                }
                return success;
            });
        }
    }

    /**
     * 功能描述：判断当前登录用户是否已点赞
     * 将点赞记录存储在redis中：key为用户id，field(hashKey)为博客id，(hashValue)value为点赞记录id
     * 即userId:(blogId:thumbId)
     * @param blogId
     * @param userId
     * @return
     */
    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        //检查用户（userId）是否对某篇博客（blogId）点过赞
        //opsForHash().hasKey()：检查Hash中是否存在指定的字段（即用户是否点过赞）(ops：是"operations"的缩写，表示“操作”)
        //ThumbConstant.USER_THUMB_KEY_PREFIX + userId：生成Redis中的Hash键（Key），例如thumb:123（假设userId=123）。
        //blogId.toString()：Hash中的字段（Field），表示博客ID。
        //opsForHash().hasKey()：检查Hash中是否存在指定的字段（即用户是否点过赞）。
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX+userId,blogId.toString());
    }
}

