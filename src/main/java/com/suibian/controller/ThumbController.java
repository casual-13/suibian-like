package com.suibian.controller;

import com.suibian.common.BaseResponse;
import com.suibian.common.ResultUtils;
import com.suibian.model.dto.thumb.ThumbLikeOrNotDTO;
import com.suibian.service.ThumbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thumb")
@RequiredArgsConstructor
@Tag(name = "点赞管理")
public class ThumbController {

    private final ThumbService thumbService;

    /**
     * 点赞
     *
     * @param thumbLikeOrNotDTO 点赞参数列表
     * @return 是否成功
     */
    @PostMapping("/do")
    @Operation(summary = "点赞")
    public BaseResponse<Boolean> doThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        Boolean res = thumbService.doThumb(thumbLikeOrNotDTO);
        return ResultUtils.success(res);
    }

    /**
     * 取消点赞
     *
     * @param thumbLikeOrNotDTO 取消点赞参数列表
     * @return 是否成功
     */
    @PostMapping("/undo")
    @Operation(summary = "取消点赞")
    public BaseResponse<Boolean> undoThumb(ThumbLikeOrNotDTO thumbLikeOrNotDTO) {
        Boolean res = thumbService.undoThumb(thumbLikeOrNotDTO);
        return ResultUtils.success(res);
    }


}
