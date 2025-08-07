package com.suibian.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.ErrorCode;
import com.suibian.constant.RedisLuaScriptConstant;
import com.suibian.exception.BusinessException;
import com.suibian.mapper.ThumbMapper;
import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.model.entity.Thumb;
import com.suibian.model.entity.User;
import com.suibian.model.enums.LuaTypeEnum;
import com.suibian.service.ThumbService;
import com.suibian.service.UserService;
import com.suibian.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author suibian
 * @ description 针对表【thumb(点赞记录表)】的数据库操作Service实现
 * @ createDate 2025-07-23 10:37:01
 */
@Service("ThumbServiceRedis")
//@Primary
@RequiredArgsConstructor
public class ThumbRedisServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 点赞
     *
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 是否成功
     */
    @Override
    public Boolean doThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        // 参数校验并获取登录用户
        User loginUser = validateRequestAndGetUser(thumbLikeOrNotDTO);
        Long userId = loginUser.getId();
        Long blogId = thumbLikeOrNotDTO.getBlogId();
        // 获得时间
        String timeSlice = getTimeSlice();
        // 获得Redis的Key
        String userThumbKey = RedisKeyUtil.getUserThumbKey(userId);
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                userId,
                blogId,
                DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss"));
        // 根据返回值判断 Lua 脚本执行情况
        if (null == result || result.equals(LuaTypeEnum.FAIL.getValue())) {
            throw new BusinessException(ErrorCode.USER_LIKE_ERROR);
        }
        return result.equals(LuaTypeEnum.SUCCESS.getValue());
    }

    /**
     * 取消点赞
     *
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 是否成功
     */
    @Override
    public Boolean undoThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        // 参数校验并获取登录用户
        User loginUser = validateRequestAndGetUser(thumbLikeOrNotDTO);
        // 获取用户Id 和 博客Id
        Long userId = loginUser.getId();
        Long blogId = thumbLikeOrNotDTO.getBlogId();
        // 获取当前时间片
        String timeSlice = getTimeSlice();
        // 获取Redis的Key
        String userThumbKey = RedisKeyUtil.getUserThumbKey(userId);
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                userId,
                blogId,
                DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss")
        );
        // 根据返回值判断 Lua 脚本执行情况
        if (null == result || result.equals(LuaTypeEnum.FAIL.getValue())) {
            throw new BusinessException(ErrorCode.USER_UNLIKE_ERROR);
        }
        return result.equals(LuaTypeEnum.SUCCESS.getValue());
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

    /**
     * 获取时间片
     * 以 10 秒为一个时间片
     *
     * @return 时间片， 返回时间格式 12:16:30
     */
    private String getTimeSlice() {
        // 获得当前时间
        DateTime nowDate = DateUtil.date();
        // 获得初始秒
        int second = (DateUtil.second(nowDate) / 10) * 10;
        // 返回时间格式 HH:mm:ss
        return DateUtil.format(nowDate, "HH:mm:") + (second == 0 ? "00" : second);
    }

    /**
     * 参数列表校验并返回登录用户
     *
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 登录用户
     */
    private User validateRequestAndGetUser(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        if (null == thumbLikeOrNotDTO || thumbLikeOrNotDTO.getBlogId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST_PARAMS);
        }
        return Optional.ofNullable(userService.getLoginUser())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }
}




