package com.suibian.manager.cache;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * TopK热点检测算法接口
 * 定义了热点Key检测的核心操作方法
 */
public interface TopK {

    /**
     * 添加Key访问记录并更新热点统计
     *
     * @param key       被访问的Key
     * @param increment 增加的访问次数（通常为1）
     * @return AddResult 包含操作结果的封装对象（被驱逐的Key、是否为热Key等）
     */
    AddResult add(String key, int increment);

    /**
     * 获取当前TopK热点Key列表
     * 按热度降序排序
     *
     * @return 热点Key列表，第一个元素热度最高
     */
    List<Item> list();

    /**
     * 获取被驱逐出TopK的Key队列
     * 用于监控哪些Key从热点列表中被移除
     *
     * @return 被驱逐Key的阻塞队列
     */
    BlockingQueue<Item> expelled();

    /**
     * 执行时间衰减操作
     * 将所有Key的计数减半，让历史热点逐渐"冷却"
     * 通常定时调用（如每20秒一次）
     */
    void fading();

    /**
     * 获取总访问计数
     *
     * @return 累计的总访问次数
     */
    long total();
}
