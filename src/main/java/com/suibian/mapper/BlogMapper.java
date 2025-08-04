package com.suibian.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suibian.common.PageResult;
import com.suibian.model.dto.blog.BlogPageReqDTO;
import com.suibian.model.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;
import java.util.Objects;

/**
* @author suibian
* @description 针对表【blog(博客表)】的数据库操作Mapper
* @createDate 2025-07-23 10:37:01
* @Entity com.suibian.entity.Blog
*/
public interface BlogMapper extends BaseMapper<Blog> {

    /**
     * 批量更新点赞数
     * @param countMap 键：blogId，值：点赞数
     */
    void batchUpdateThumbCount(@Param("countMap") Map<Long, Long> countMap);

}




