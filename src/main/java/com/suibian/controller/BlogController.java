package com.suibian.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.suibian.common.BaseResponse;
import com.suibian.common.PageResult;
import com.suibian.common.ResultUtils;
import com.suibian.model.dto.blog.BlogPageReqDTO;
import com.suibian.model.vo.BlogVO;
import com.suibian.service.BlogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags = "博客管理")
@RestController
@AllArgsConstructor
@RequestMapping("/blog")
public class BlogController {

    private final BlogService blogService;

    /**
     * 根据id获取博客详情
     *
     * @param id 博客id
     * @return 博客详情
     */
    @GetMapping("/getBlogVOById")
    @ApiOperation("根据id获取博客详情")
    public BaseResponse<BlogVO> getBlogById(Long id) {
        BlogVO blogVO = blogService.getBlogVOById(id);
        return ResultUtils.success(blogVO);
    }

    /**
     * 分页获取博客列表
     * @param blogPageReqDTO 博客分页请求参数
     * @return 博客列表
     */
    @GetMapping("/page")
    @ApiOperation("分页获取博客列表")
//  TODO  @SaCheckRole("admin")
    public BaseResponse<PageResult<BlogVO>> getBlogPage(BlogPageReqDTO blogPageReqDTO) {
        PageResult<BlogVO> blogPage = blogService.getBlogPage(blogPageReqDTO);
        return ResultUtils.success(blogPage);
    }
}


