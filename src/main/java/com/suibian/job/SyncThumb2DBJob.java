package com.suibian.job;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suibian.mapper.BlogMapper;
import com.suibian.model.dto.thumb.ThumbTempCacheDTO;
import com.suibian.model.entity.Thumb;
import com.suibian.model.enums.ThumbTypeEnum;
import com.suibian.service.ThumbService;
import com.suibian.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class SyncThumb2DBJob {

    @Resource
    ThumbService thumbService;

    @Resource
    BlogMapper blogMapper;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    public static final int SECOND_BUG = -10;

    /**
     * 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("定时任务：将 Redis中的临时点赞数据同步到数据库");
        // 获取当前时间片的初始时间
        DateTime nowDate = DateUtil.date();
        // 处理上一个10s的数据
        int second = (DateUtil.second(nowDate) / 10 - 1) * 10;
        if (second == SECOND_BUG) {
            second = 50;
            // 回到上一分钟
            nowDate = DateUtil.offsetMinute(nowDate, -1);
        }
        String timeSlice = DateUtil.format(nowDate, "HH:mm:") + (second == 0 ? "00" : second);
        syncThumb2DbByDate(timeSlice);
        log.info("同步完成，当前时间片：{}", timeSlice);
    }

    // 同步数据到数据库
    // 删除临时点赞记录
    public void syncThumb2DbByDate(String date) {
        // 获得临时点赞Key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        // 获得date时间片所有的临时点赞和取消点赞操作
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        // 临时点赞数据为空直接返回
        if (CollUtil.isEmpty(allTempThumbMap)) {
            return;
        }
        // 用来记录博客点赞量
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        // 点赞记录列表，用来批量插入数据库
        List<Thumb> thumbList = new ArrayList<>();
        // 分装要删除点赞记录的查询条件
        boolean needDelete = false;
        LambdaQueryWrapper<Thumb> delWrapper = new LambdaQueryWrapper<>();
        // 遍历所有临时点赞记录
        for (Object userIdBlogIdObject : allTempThumbMap.keySet()) {
            // 获得用户Id和博客Id
            String userIdBlogIdStr = (String) userIdBlogIdObject;
            String[] uiAndBi = userIdBlogIdStr.split(StrPool.COLON);
            // 用户Id
            Long userId = Long.valueOf(uiAndBi[0]);
            // 博客Id
            Long blogId = Long.valueOf(uiAndBi[1]);
            // 临时点赞操作
            // {"type":1,time:'2025-01-01 00:00:00'}  -1 取消点赞，1 点赞
            Object value = allTempThumbMap.get(userIdBlogIdObject);
            ThumbTempCacheDTO thumbTemp = BeanUtil.toBean(value, ThumbTempCacheDTO.class);
            if (thumbTemp == null) {
                continue;
            }
            Integer thumbType = Optional.ofNullable(thumbTemp.getType()).orElse(0);
            // 点赞操作，保存点赞记录
            if (ThumbTypeEnum.INCR.getValue() == thumbType) {
                Thumb thumb = new Thumb();
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                thumb.setCreateTime(DateUtil.parse(thumbTemp.getTime()));
                thumbList.add(thumb);
                // 取消点赞操作，保存取消点赞记录
            } else if (ThumbTypeEnum.DECR.getValue() == thumbType) {
                needDelete = true;
                delWrapper.eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId);
            } else if (ThumbTypeEnum.NON.getValue() != thumbType) {
                log.warn("数据异常：{}", userId + "," + blogId + "," + thumbType);
            }
            // 计算点赞增量
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);
        }
        // 批量插入点赞记录
        thumbService.saveBatch(thumbList);
        // 批量删除点赞记录
        if (needDelete) {
            thumbService.remove(delWrapper);
        }
        // 批量更新点赞数
        if (CollUtil.isNotEmpty(blogThumbCountMap)) {
            blogMapper.batchUpdateThumbCount(blogThumbCountMap);
        }
        // 异步删除临时点赞记录
        // 利用Java21的虚拟线程
        Thread.startVirtualThread(() -> redisTemplate.delete(tempThumbKey));
    }
}
