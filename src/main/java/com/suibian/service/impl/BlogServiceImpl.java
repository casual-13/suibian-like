package com.suibian.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suibian.common.PageResult;
import com.suibian.mapper.BlogMapper;
import com.suibian.model.dto.blog.BlogPageReqDTO;
import com.suibian.model.entity.Blog;
import com.suibian.model.entity.Thumb;
import com.suibian.model.entity.User;
import com.suibian.model.vo.BlogVO;
import com.suibian.service.BlogService;
import com.suibian.service.ThumbService;
import com.suibian.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author suibian
 * @ description 针对表【blog(博客表)】的数据库操作Service实现
 * @ createDate 2025-07-23 10:37:01
 */
@Service
//@RequiredArgsConstructor
@Setter
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements BlogService {

    UserService userService;

    ThumbService thumbService;

    @Override
    public BlogVO getBlogVOById(Long id) {
        Blog blog = this.getById(id);
        User user = userService.getById(StpUtil.getLoginIdAsLong());
        return this.getBlogVO(blog, user);
    }

    public BlogVO getBlogVO(Blog blog, User user) {
        BlogVO blogVO = BeanUtil.copyProperties(blog, BlogVO.class);
        if (user != null) {
            // 查询用户点赞记录
            boolean exists = thumbService.lambdaQuery()
                    .eq(Thumb::getBlogId, blog.getId())
                    .eq(Thumb::getUserId, user.getId())
                    .exists();
            blogVO.setHasThumb(exists);
        }
        return blogVO;
    }

    /**
     * 分页获取博客列表
     * @param blogPageReqDTO 博客分页请求参数
     * @return 博客列表
     */
    @Override
    public PageResult<BlogVO> getBlogPage(BlogPageReqDTO blogPageReqDTO) {
        // mp 的分页查询
        Page<Blog> page = new Page<>();
        page.setCurrent(blogPageReqDTO.getPageNo());
        page.setSize(blogPageReqDTO.getPageSize());
        Page<Blog> blogPage = this.page(page, new LambdaQueryWrapper<Blog>()
                .like(Objects.nonNull(blogPageReqDTO.getBlogName()), Blog::getTitle, blogPageReqDTO.getBlogName()));
        // 获取查询后的分页结果
        List<Blog> blogList = blogPage.getRecords();
        if (blogList == null || blogList.isEmpty()) {
            return PageResult.empty();
        }
        // 如果以后要返回用户数据给前端 就改动BlogVO
        // 获取用户数据
        User user = userService.getLoginUser();
        List<BlogVO> blogVOList = blogList.stream()
                .map(blog -> this.getBlogVO(blog, user))
                .collect(Collectors.toList());
        return new PageResult<>(blogVOList, blogPage.getTotal());
    }
}




