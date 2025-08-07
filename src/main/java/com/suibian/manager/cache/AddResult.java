package com.suibian.manager.cache;

/**
 * @param expelledKey 被驱逐出TopK的Key，如果没有则为null
 * @param hotKey      当前添加的Key是否成为热点Key
 * @param currentKey  当前操作的 key
 */
public record AddResult(String expelledKey, boolean hotKey, String currentKey) {
}