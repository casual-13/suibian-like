package com.suibian.constant;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class RedisLuaScriptConstant {

    /**
     * 点赞脚本
     * KEYS[1]  -- 临时计数键
     * KEYS[2]  -- 用户点赞状态键
     * ARGV[1]  -- 用户ID
     * ARGV[2]  -- 博客ID
     * ARGV[3]  -- 点赞时间
     * <p>
     * return：
     * 1：成功
     * -1：已点赞
     */
    public static final RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]    -- 临时计数键，例如：[thumb:temp:{timeSlice}]
            local userThumbKey = KEYS[2]    -- 用户点赞状态键，例如：[thumb:{userId}]
            local userId = ARGV[1]          -- 用户ID
            local blogId = ARGV[2]          -- 博客ID
            local currentTime = ARGV[3]     -- 点赞时间
                        
            -- 1. 检测是否已经点赞（避免重复操作）
            if redis.call('HEXISTS', userThumbKey, blogId) == 1 then
                return -1                   -- 如果已经点赞，返回-1表示操作失败
            end
                        
            -- 2. 获取旧值，不存在则为0     .. 拼接字符串
            local hashKey = userId .. ":" .. blogId
            local oldValue = redis.call('HGET', tempThumbKey, hashKey)
            if oldValue == false then
                oldValue = '{"type": 0, "time": ""}'
            end
                        
            -- 3. 解析旧值
            local oldData = cjson.decode(oldValue)
            local oldType = oldData.type
            local oldTime = oldData.time
                        
            -- 4. 计算新值
            -- 旧值可能为0，再+1就是执行点赞操作，为-1，再+1就是0没有任何变化，不操作数据库
            local newType = 1
            local newValue = '{"type": ' .. newType .. ', "time": ' .. currentTime .. '}'
                        
            -- 4. 原子化更新：写入临时计数 并 标记用户点赞状态
            redis.call('HSET', tempThumbKey, hashKey, newValue)
            redis.call('HSET', userThumbKey, blogId, 1)
                        
            -- 5. 返回1，表示成功
            return 1
            """, Long.class);

    /**
     * 取消点赞脚本
     * KEYS[1]  -- 临时计数键
     * KEYS[2]  -- 用户点赞状态键
     * ARGV[1]  -- 用户ID
     * ARGV[2]  -- 博客ID
     * ARGV[3]  -- 点赞时间
     * <p>
     * return：
     * 1：成功
     * -1：未点赞
     */
    public static final RedisScript<Long> UNTHUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]    -- 临时计数键，例如：[thumb:temp:{timeSlice}]
            local userThumbKey = KEYS[2]    -- 用户点赞状态键，例如：[thumb:{userId}]
            local userId = ARGV[1]            -- 用户ID
            local blogId = ARGV[2]            -- 博客ID
            local currentTime = ARGV[3]
                        
            -- 1. 检测是否已经点赞（避免重复操作）
            if redis.call('HEXISTS', userThumbKey, blogId) ~= 1 then
                return -1                     -- 如果没有点赞记录，返回-1表示操作失败
            end
                        
            -- 2. 获取旧值，不存在则为0     .. 拼接字符串
            local hashKey = userId .. ":" .. blogId
            local oldValue = redis.call('HGET', tempThumbKey, hashKey)
            if oldValue == false then
                oldValue = '{"type": 0, "time": ""}'
            end
                        
            -- 3. 解析旧值
            local oldData = cjson.decode(oldValue)
            local oldType = oldData.type
            local oldTime = oldData.time
                        
            -- 4. 计算新值
            -- 旧值可能为0，再+1就是执行点赞操作，为-1，再+1就是0没有任何变化，不操作数据库
            local newNumber = oldType - 1
            local newValue = '{"type":' .. newNumber .. ',"time":'.. currentTime ..'}'
                        
            -- 4. 原子化更新：写入临时计数 并 标记用户点赞状态
            redis.call('HSET', tempThumbKey, hashKey, newValue)
            redis.call('HDEL', userThumbKey, blogId)
                        
            -- 5. 返回1，表示成功
            return 1
            """, Long.class);

    /**
     * 点赞脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""
                local userThumbKey = KEYS[1]
                local blogId = ARGV[1]
                            
                -- 判断是否已经点赞
                if redis.call("HEXISTS", userThumbKey, blogId) == 1 then
                    return -1
                end
                            
                -- 添加点赞记录
                redis.call("HSET", userThumbKey, blogId, 1)
                return 1
            """, Long.class);

    /**
     * 取消点赞 Lua 脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    public static final RedisScript<Long> UN_THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""
                local userThumbKey = KEYS[1]
                local blogId = ARGV[1]
                
                -- 判断是否已经点赞
                if redis.call("HEXISTS", userThumbKey, blogId) == 0 then
                	return -1
                end
                
                -- 删除点赞记录
                redis.call("HDEL", userThumbKey, blogId, 1)
                return 1
            """, Long.class);
}
