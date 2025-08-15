package com.suibian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.ErrorCode;
import com.suibian.constant.RedisLuaScriptConstant;
import com.suibian.constant.ThumbConstant;
import com.suibian.exception.BusinessException;
import com.suibian.listener.thumb.msg.ThumbEvent;
import com.suibian.mapper.ThumbMapper;
import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.model.entity.Blog;
import com.suibian.model.entity.Thumb;
import com.suibian.model.entity.User;
import com.suibian.model.enums.LuaTypeEnum;
import com.suibian.service.ThumbService;
import com.suibian.service.UserService;
import com.suibian.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author suibian
 * @ description 针对表【thumb(点赞记录表)】的数据库操作Service实现
 * @ createDate 2025-07-23 10:37:01
 */
@Service("ThumbService")
@Primary
@RequiredArgsConstructor
@Slf4j
public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final PulsarTemplate<ThumbEvent> pulsarTemplate;

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
        // 用户ID
        Long userId = loginUser.getId();
        // 博客ID
        Long blogId = thumbLikeOrNotDTO.getBlogId();
        // 获取Redis的Key
        String userThumbKey = RedisKeyUtil.getUserThumbKey(userId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId);
        // 根据返回值判断 Lua 脚本执行情况
        if (LuaTypeEnum.FAIL.getValue().equals(result)) {
            throw new BusinessException(ErrorCode.USER_LIKE_ERROR);
        }
        // 发送消息
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .userId(userId)
                .blogId(blogId)
                .type(ThumbEvent.EventType.INCR)
                .eventTime(LocalDateTime.now())
                .build();
        try {
            pulsarTemplate.sendAsync("thumb-topic", thumbEvent);
        } catch (PulsarClientException e) {
            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
            log.error("点赞事件发送失败: userId={}, blogId={}", userId, blogId, e);
            return null;
        }
//        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
//            redisTemplate.opsForHash().delete(userThumbKey, blogId.toString(), true);
//            log.error("点赞事件发送失败: userId={}, blogId={}", userId, blogId, ex);
//            return null;
//        });
        return true;
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
        // 用户ID
        Long userId = loginUser.getId();
        // 博客ID
        Long blogId = thumbLikeOrNotDTO.getBlogId();
        // 获取Redis的Key
        String userThumbKey = RedisKeyUtil.getUserThumbKey(userId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(RedisLuaScriptConstant.UN_THUMB_SCRIPT_MQ, List.of(userThumbKey), blogId);
        // 根据返回值判断 Lua 脚本执行情况
        if (LuaTypeEnum.FAIL.getValue().equals(result)) {
            throw new BusinessException(ErrorCode.USER_UNLIKE_ERROR);
        }
        // 发送消息
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .userId(userId)
                .blogId(blogId)
                .type(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        try {
            pulsarTemplate.sendAsync("thumb-topic", thumbEvent);
        } catch (PulsarClientException e) {
            redisTemplate.opsForHash().put(userThumbKey, blogId.toString(), true);
            log.error("点赞事件发送失败: userId={}, blogId={}", userId, blogId, e);
            return null;
        }
        return true;
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
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }
}




