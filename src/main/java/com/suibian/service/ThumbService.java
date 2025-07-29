package com.suibian.service;

import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.model.entity.Thumb;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author suibian
* @ description 针对表【thumb(点赞记录表)】的数据库操作Service
* @ createDate 2025-07-23 10:37:01
*/
public interface ThumbService extends IService<Thumb> {

    /**
     * 点赞
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 返回执行结果
     */
    Boolean doThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO);

    /**
     * 取消点赞
     * @param thumbLikeOrNotDTO 点赞或取消点赞参数列表
     * @return 返回执行结果
     */
    Boolean undoThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO);

    /**
     * 判断用户是否点赞
     * @param userId 用户Id
     * @param blogId 博客Id
     * @return 返回是否点赞
     */
    Boolean isThumb(Long userId, Long blogId);
}
