package com.suibian.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.suibian.constant.ThumbConstant;
import com.suibian.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

//@Component
@Slf4j
public class SyncThumb2DBCompensatoryJob {

    @Resource
    SyncThumb2DBJob syncThumb2DBJob;

    @Resource
    RedisTemplate<String, Object> redisTemplate;

    /**
     * 定时任务，每天2点执行一次，用于补偿点赞数据
     */

    //    @Scheduled(cron = "10 * * * * *")
    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        log.info("开始补偿点赞数据");
        // 获取所有临时点赞数据
        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempThumbKey("") + "*");
        if (thumbKeys == null || CollUtil.isEmpty(thumbKeys)) {
            return;
        }
        // 用于存储要处理的日期
        Set<String> needHandleDateSet = new HashSet<>();
        // 获取所有日期
        thumbKeys.stream()
                .filter(ObjUtil::isNotNull)
                .forEach(thumbKey -> needHandleDateSet.add(thumbKey.replace(ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(""), "")));
        // 如果没有要处理的直接返回
        if (CollUtil.isEmpty(needHandleDateSet)) {
            log.info("没有需要补偿的临时数据");
            return;
        }
        // 遍历所有日期
        for (String date : needHandleDateSet) {
            syncThumb2DBJob.syncThumb2DbByDate(date);
        }
        log.info("补偿点赞数据完成");
    }
}
