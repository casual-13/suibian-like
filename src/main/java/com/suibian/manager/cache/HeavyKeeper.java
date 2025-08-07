package com.suibian.manager.cache;

import cn.hutool.core.util.HashUtil;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * HeavyKeeper算法实现 - 高效的TopK热点检测算法
 * <p>
 * HeavyKeeper是一种基于概率数据结构的流式TopK算法，具有以下特点：
 * 1. 空间复杂度：O(k + w*d)，其中k是TopK大小，w是哈希表宽度，d是深度
 * 2. 时间复杂度：每次插入O(d*log k)，查询TopK为O(k)
 * 3. 准确性：通过多层哈希和概率性替换保证高准确率
 * 4. 实时性：支持流式数据处理，无需预先知道数据分布
 * <p>
 * 算法核心思想：
 * - 使用多层哈希表(Count-Min Sketch变种)估算频次
 * - 维护一个最小堆保存TopK候选
 * - 通过概率性替换(衰减机制)处理哈希冲突
 * - 支持时间衰减，适应数据流的时间局部性
 * <p>
 * 适用场景：
 * - 热点Key检测（缓存、数据库）
 * - 网络流量分析
 * - 用户行为分析
 * - 实时推荐系统
 */
public class HeavyKeeper implements TopK {
    /**
     * 查找表大小，用于预计算衰减概率
     */
    private static final int LOOKUP_TABLE_SIZE = 256;

    /**
     * TopK的K值，即要维护的热点Key数量
     */
    private final int k;

    /**
     * 哈希表宽度，即每层哈希表的桶数量
     */
    private final int width;

    /**
     * 哈希表深度，即哈希表的层数
     */
    private final int depth;

    /**
     * 衰减概率查找表，预计算不同计数下的衰减概率
     */
    private final double[] lookupTable;

    /**
     * 多层哈希表，每个桶存储指纹和计数
     */
    private final Bucket[][] buckets;

    /**
     * 最小堆，维护TopK热点Key
     */
    private final PriorityQueue<Node> minHeap;

    /**
     * 被驱逐Key的队列，用于监控热点变化
     */
    private final BlockingQueue<Item> expelledQueue;

    /**
     * 随机数生成器，用于概率性衰减
     */
    private final Random random;

    /**
     * 总访问计数
     */
    private long total;

    /**
     * 最小计数阈值，低于此值不考虑为热点候选
     */
    private final int minCount;

    /**
     * 哈希种子数组，为每层哈希表提供不同的种子
     */
    private final int[] hashSeeds;

    /**
     * 构造HeavyKeeper实例
     *
     * @param k        TopK的K值，要维护的热点Key数量
     * @param width    哈希表宽度，每层桶的数量，影响哈希冲突概率
     * @param depth    哈希表深度，层数，影响检测准确性
     * @param decay    衰减系数(0,1)，控制概率性替换的激进程度
     * @param minCount 最小计数阈值，过滤低频Key
     */
    public HeavyKeeper(int k, int width, int depth, double decay, int minCount) {
        this.k = k;
        this.width = width;
        this.depth = depth;
        this.minCount = minCount;

        // 初始化衰减概率查找表，预计算decay^i的值
        this.lookupTable = new double[LOOKUP_TABLE_SIZE];
        for (int i = 0; i < LOOKUP_TABLE_SIZE; i++) {
            lookupTable[i] = Math.pow(decay, i);
        }

        // 初始化多层哈希表
        this.buckets = new Bucket[depth][width];
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < width; j++) {
                buckets[i][j] = new Bucket();
            }
        }

        // 为每层哈希表生成不同的种子，确保哈希独立性
        this.hashSeeds = new int[depth];
        Random seedRandom = new Random(42); // 使用固定种子保证可重现性
        for (int i = 0; i < depth; i++) {
            hashSeeds[i] = seedRandom.nextInt();
        }

        // 初始化最小堆，按计数升序排列
        this.minHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));

        // 初始化被驱逐Key队列
        this.expelledQueue = new LinkedBlockingQueue<>();
        this.random = new Random();
        this.total = 0;
    }

    /**
     * 添加Key的访问记录
     * HeavyKeeper算法的核心方法，实现概率性计数和TopK维护
     *
     * @param key       被访问的Key
     * @param increment 增加的计数值（通常为1）
     * @return AddResult 包含操作结果：被驱逐的Key、是否为热点Key等
     */
    @Override
    public AddResult add(String key, int increment) {
        // 计算Key的字节表示和指纹
        byte[] keyBytes = key.getBytes();
        long itemFingerprint = hash(keyBytes);
        int maxCount = 0;

        // 在每层哈希表中处理该Key
        for (int i = 0; i < depth; i++) {
            // 计算在第i层的桶位置
            int bucketNumber = Math.abs(hash(keyBytes, i)) % width;
            Bucket bucket = buckets[i][bucketNumber];

            synchronized (bucket) {
                if (bucket.count == 0) {
                    // 桶为空，直接插入
                    bucket.fingerprint = itemFingerprint;
                    bucket.count = increment;
                    maxCount = Math.max(maxCount, increment);
                } else if (bucket.fingerprint == itemFingerprint) {
                    // 指纹匹配，增加计数
                    bucket.count += increment;
                    maxCount = Math.max(maxCount, bucket.count);
                } else {
                    // 指纹不匹配，执行概率性衰减替换
                    int originalCount = bucket.count;
                    for (int j = 0; j < increment; j++) {
                        // 根据当前桶的计数决定衰减概率
                        double decay = bucket.count < LOOKUP_TABLE_SIZE ?
                                lookupTable[bucket.count] :
                                lookupTable[LOOKUP_TABLE_SIZE - 1];
                        // 以decay概率减少桶计数
                        if (random.nextDouble() < decay) {
                            bucket.count--;
                            if (bucket.count == 0) {
                                // 桶计数归零，替换为新Key
                                bucket.fingerprint = itemFingerprint;
                                bucket.count = increment - j;
                                maxCount = Math.max(maxCount, bucket.count);
                                break;
                            }
                        }
                    }
                    // 如果没有成功替换，至少记录一次访问
                    if (bucket.count == originalCount && maxCount == 0) {
                        maxCount = 1;
                    }
                }
            }
        }

        // 更新总访问计数
        total += increment;

        // 如果估算的最大计数低于阈值，不考虑为热点
        if (maxCount < minCount) {
            return new AddResult(null, false, key);
        }

        // 更新TopK最小堆
        synchronized (minHeap) {
            boolean isHot = false;
            String expelled = null;

            // 检查Key是否已在TopK中
            Optional<Node> existing = minHeap.stream()
                    .filter(n -> n.key.equals(key))
                    .findFirst();

            if (existing.isPresent()) {
                // Key已存在，更新其计数
                minHeap.remove(existing.get());
                minHeap.add(new Node(key, maxCount));
                isHot = true;
            } else {
                // 新Key，判断是否应该加入TopK
                if (minHeap.size() < k || maxCount >= Objects.requireNonNull(minHeap.peek()).count) {
                    Node newNode = new Node(key, maxCount);
                    if (minHeap.size() >= k) {
                        // TopK已满，驱逐计数最小的Key
                        Node expelledNode = minHeap.poll();
                        expelled = expelledNode.key;
                        expelledQueue.offer(new Item(expelled, expelledNode.count));
                    }
                    minHeap.add(newNode);
                    isHot = true;
                }
            }

            return new AddResult(expelled, isHot, key);
        }
    }

    /**
     * 获取当前TopK热点Key列表
     * 按访问计数降序排列
     *
     * @return TopK热点Key列表，第一个元素热度最高
     */
    @Override
    public List<Item> list() {
        synchronized (minHeap) {
            // 将堆中的Node转换为Item列表
            List<Item> result = new ArrayList<>(minHeap.size());
            for (Node node : minHeap) {
                result.add(new Item(node.key, node.count));
            }
            // 按计数降序排序
            result.sort((a, b) -> Integer.compare(b.count(), a.count()));
            return result;
        }
    }

    /**
     * 获取被驱逐Key的队列
     * 用于监控哪些Key曾经是热点但后来被其他Key替换
     *
     * @return 被驱逐Key的阻塞队列
     */
    @Override
    public BlockingQueue<Item> expelled() {
        return expelledQueue;
    }

    /**
     * 执行时间衰减操作
     * 对所有桶和TopK中的计数进行衰减，模拟时间遗忘效应
     * <p>
     * 衰减策略：
     * 1. 将所有桶的计数右移1位（相当于除以2）
     * 2. 清理计数归零的桶，释放指纹空间
     * 3. 对TopK中的计数也进行相同的衰减
     * 4. 移除衰减后计数为0的TopK项
     * <p>
     * 作用：
     * - 让历史热点逐渐"冷却"，为新热点让出空间
     * - 保持算法对数据流时间变化的敏感性
     * - 防止旧热点永久占据TopK位置
     */
    @Override
    public void fading() {
        // 对所有桶执行衰减
        for (Bucket[] row : buckets) {
            for (Bucket bucket : row) {
                synchronized (bucket) {
                    // 计数右移1位，相当于除以2
                    bucket.count = bucket.count >> 1;
                    if (bucket.count == 0) {
                        // 计数归零，清理指纹
                        bucket.fingerprint = 0;
                    }
                }
            }
        }

        // 对TopK堆中的计数也执行衰减
        synchronized (minHeap) {
            PriorityQueue<Node> newHeap = new PriorityQueue<>(Comparator.comparingInt(n -> n.count));
            for (Node node : minHeap) {
                int newCount = node.count >> 1;
                if (newCount > 0) {
                    // 只保留衰减后计数仍大于0的Key
                    newHeap.add(new Node(node.key, newCount));
                }
            }
            minHeap.clear();
            minHeap.addAll(newHeap);
        }

        // 总计数也进行衰减
        total = total >> 1;
    }

    /**
     * 获取总访问计数
     * 返回所有Key的累计访问次数
     *
     * @return 总访问次数
     */
    @Override
    public long total() {
        return total;
    }

    /**
     * 哈希桶数据结构
     * 存储Key的指纹和访问计数
     */
    private static class Bucket {
        /**
         * Key的指纹，用于快速比较Key是否相同
         */
        long fingerprint;
        /**
         * 访问计数
         */
        int count;
    }

    /**
     * TopK堆中的节点
     * 存储Key和其对应的计数
     */
    private static class Node {
        /**
         * Key值
         */
        final String key;
        /**
         * 计数值
         */
        final int count;

        Node(String key, int count) {
            this.key = key;
            this.count = count;
        }
    }

    /**
     * 计算带层级种子的哈希值
     * 为不同层的哈希表提供独立的哈希函数
     *
     * @param data  要哈希的数据
     * @param layer 哈希层级
     * @return 哈希值
     */
    private int hash(byte[] data, int layer) {
        int hash = HashUtil.murmur32(data);
        return hash ^ hashSeeds[layer];
    }

    /**
     * 计算标准哈希值
     * 用于生成Key的指纹
     *
     * @param data 要哈希的数据
     * @return 哈希值
     */
    private static int hash(byte[] data) {
        return HashUtil.murmur32(data);
    }

}