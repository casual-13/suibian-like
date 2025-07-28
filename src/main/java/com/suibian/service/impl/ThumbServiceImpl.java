package com.suibian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.ErrorCode;
import com.suibian.exception.BusinessException;
import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.model.entity.Blog;
import com.suibian.model.entity.Thumb;
import com.suibian.model.entity.User;
import com.suibian.service.BlogService;
import com.suibian.service.ThumbService;
import com.suibian.mapper.ThumbMapper;
import com.suibian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;

/**
* @author suibian
* @ description 针对表【thumb(点赞记录表)】的数据库操作Service实现
* @ createDate 2025-07-23 10:37:01
*/
@Service
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService{

    private final UserService userService;

    private final BlogService blogService;

    private final TransactionTemplate transactionTemplate;

    /**
     * 点赞或取消点赞
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
                boolean exists = this.lambdaQuery()
                        .eq(Thumb::getBlogId, blogId)
                        .eq(Thumb::getUserId, userId)
                        .exists();
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
                return update && this.save(thumb);
            });
        }
    }

    /**
     * 取消点赞
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
                Thumb thumb = this.lambdaQuery()
                        .eq(Thumb::getBlogId, blogId)
                        .eq(Thumb::getUserId, userId)
                        .one();
                if (thumb == null) {
                    throw new BusinessException(ErrorCode.USER_UNLIKE_ERROR);
                }
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumb_count = thumb_count - 1")
                        .update();
                return update && this.removeById(thumb.getId());
            });
        }
    }
}




