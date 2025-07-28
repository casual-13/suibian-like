package com.suibian.service;

import com.suibian.common.PageResult;
import com.suibian.model.dto.blog.BlogPageReqDTO;
import com.suibian.model.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suibian.model.vo.BlogVO;

/**
* @author suibian
* @ description 针对表【blog(博客表)】的数据库操作Service
* @ createDate 2025-07-23 10:37:01
*/
public interface BlogService extends IService<Blog> {

    /**
     * 根据id获取博客详情
     * @param id 博客id
     * @return 博客详情
     */
    BlogVO getBlogVOById(Long id);

    /**
     * 分页获取博客列表
     * @param blogPageReqDTO 博客分页请求参数
     * @return 博客列表
     */
    PageResult<BlogVO> getBlogPage(BlogPageReqDTO blogPageReqDTO);
}
