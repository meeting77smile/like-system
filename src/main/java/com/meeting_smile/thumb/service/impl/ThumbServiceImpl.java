package com.meeting_smile.thumb.service.impl;

import com.meeting_smile.thumb.constant.ThumbConstant;
import com.meeting_smile.thumb.manager.cache.CacheManager;
import com.meeting_smile.thumb.model.dto.thumb.DoThumbRequest;
import com.meeting_smile.thumb.model.entity.Blog;
import com.meeting_smile.thumb.model.entity.Thumb;
import com.meeting_smile.thumb.mapper.ThumbMapper;
import com.meeting_smile.thumb.model.entity.User;
import com.meeting_smile.thumb.service.BlogService;
import com.meeting_smile.thumb.service.ThumbService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.meeting_smile.thumb.service.UserService;
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
@Service("thumbServiceLocalCache")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    private final RedisTemplate<String,Object> redisTemplate;

    // 引入缓存管理
    private final CacheManager cacheManager;

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
                // 点赞记录存入 Redis
                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    Long realThumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, fieldKey, realThumbId);
                    cacheManager.putIfPresent(hashKey, fieldKey, realThumbId);
                }
                // 更新成功才执行
                return success;
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
                Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId(), blogId.toString());
                if (thumbIdObj == null || thumbIdObj.equals(ThumbConstant.UN_THUMB_CONSTANT)) {
                    throw new RuntimeException("用户未点赞");
                }
                Long thumbId = Long.valueOf(thumbIdObj.toString());
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId,blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean success = update && this.removeById(thumbId);
                // 点赞记录从 Redis 删除
                if (success) {
                    String hashKey = ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId();
                    String fieldKey = blogId.toString();
                    redisTemplate.opsForHash().delete(hashKey, fieldKey);
                    cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
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
        Object thumbIdObj = cacheManager.get(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = (Long) thumbIdObj;
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }

}

