package com.suibian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.ErrorCode;
import com.suibian.constant.ThumbConstant;
import com.suibian.exception.BusinessException;
import com.suibian.manager.cache.CacheManager;
import com.suibian.mapper.ThumbMapper;
import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.model.entity.Blog;
import com.suibian.model.entity.Thumb;
import com.suibian.model.entity.User;
import com.suibian.service.BlogService;
import com.suibian.service.ThumbService;
import com.suibian.service.UserService;
import com.suibian.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.Optional;

/**
 * @author suibian
 * @ description 针对表【thumb(点赞记录表)】的数据库操作Service实现
 * @ createDate 2025-07-23 10:37:01
 */
@Service("ThumbService")
@Primary
@RequiredArgsConstructor
public class ThumbLocalServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final BlogService blogService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final TransactionTemplate transactionTemplate;

    private final CacheManager cacheManager;

    /**
     * 点赞
     *
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 是否成功
     */
    @Override
    public Boolean doThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        // 参数校验
        if (thumbLikeOrNotDTO == null || thumbLikeOrNotDTO.getBlogId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                Long blogId = thumbLikeOrNotDTO.getBlogId();
                Long userId = loginUser.getId();
                // 判断是否已经点赞
                boolean exists = this.isThumb(userId, blogId);
                if (exists) {
                    throw new BusinessException(ErrorCode.USER_LIKE_ERROR);
                }
                // 修改博客点赞数
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumb_count = thumb_count + 1")
                        .update();
                // 插入点赞记录
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                boolean success = update && this.save(thumb);
                // 缓存点赞记录
                if (success) {
                    String hashKey = RedisKeyUtil.getUserThumbKey(userId);
                    String fieldKey = blogId.toString();
                    Long thumbId = thumb.getId();
                    redisTemplate.opsForHash().put(hashKey, fieldKey, thumbId);
                    cacheManager.putIfPresent(hashKey, fieldKey, thumbId);
                }
                return success;
            });
        }
    }

    /**
     * 取消点赞
     *
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 是否成功
     */
    @Override
    public Boolean undoThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        // 参数校验
        if (thumbLikeOrNotDTO == null || thumbLikeOrNotDTO.getBlogId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        // 删除点赞记录
        synchronized (loginUser.getId().toString().intern()) {
            return transactionTemplate.execute(status -> {
                Long blogId = thumbLikeOrNotDTO.getBlogId();
                Long userId = loginUser.getId();
                String hashKey = RedisKeyUtil.getUserThumbKey(userId);
                String fieldKey = blogId.toString();
                // 判断是否已经点赞
                Object thumbIdObj = cacheManager.get(hashKey, fieldKey);
                if (thumbIdObj == null || thumbIdObj.equals(ThumbConstant.UN_THUMB_CONSTANT)) {
                    throw new BusinessException(ErrorCode.USER_UNLIKE_ERROR);
                }
                Long thumbId = Long.valueOf(thumbIdObj.toString());
                // 修改博客点赞数
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumb_count = thumb_count - 1")
                        .update();
                boolean success = update && this.removeById(thumbId);
                // 删除缓存记录
                if (success) {
                    redisTemplate.opsForHash().delete(hashKey, fieldKey);
                    cacheManager.putIfPresent(hashKey, fieldKey, ThumbConstant.UN_THUMB_CONSTANT);
                }
                return success;
            });
        }
    }

    /**
     * 判断用户是否点赞
     *
     * @param userId 用户Id
     * @param blogId 博客Id
     * @return 是否点赞
     */
    @Override
    public Boolean isThumb(Long userId, Long blogId) {
        Object thumbIdObj = cacheManager.get(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
        if (thumbIdObj == null) {
            return false;
        }
        Long thumbId = Long.valueOf(thumbIdObj.toString());
        return !thumbId.equals(ThumbConstant.UN_THUMB_CONSTANT);
    }
}




