package com.suibian.manager.cache;

/**
 * 热点数据项记录类
 * 用于封装热点Key及其访问频次信息
 *
 * @param key   热点Key的名称
 * @param count 该Key的访问计数/频次
 * @author pine
 */
public record Item(String key, int count) {
}
