package com.suibian.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suibian.common.PageResult;
import com.suibian.model.dto.blog.BlogPageReqDTO;
import com.suibian.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Objects;

/**
* @author suibian
* @description 针对表【blog(博客表)】的数据库操作Mapper
* @createDate 2025-07-23 10:37:01
* @Entity com.suibian.entity.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {

}




