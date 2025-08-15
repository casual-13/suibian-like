package com.suibian.listener.thumb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suibian.listener.thumb.msg.ThumbEvent;
import com.suibian.mapper.BlogMapper;
import com.suibian.model.entity.Thumb;
import com.suibian.service.ThumbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbConsumer {

    private final BlogMapper blogMapper;
    private final ThumbService thumbService;

    @PulsarListener(
            subscriptionName = "thumb-subscription",
            subscriptionType = SubscriptionType.Shared,
            schemaType = SchemaType.JSON,
            topics = "thumb-topic",
            batch = true,
            // 引用 NACK 重试策略
            negativeAckRedeliveryBackoff ="NackRedeliveryBackoff",
            // 引用 ACK 超时重试策略
            ackTimeoutRedeliveryBackoff = "AckRedeliveryBackoff",
            // 引用死信队列策略
            deadLetterPolicy = "deadLetterPolicy",
            consumerCustomizer = "thumbConsumerConfig"
    )
    public void processBatch(List<Message<ThumbEvent>> messages) {
        log.info("ThumbConsumer processBatch: {}", messages.size());
        /*for (Message<ThumbEvent> message : messages) {
            log.info("message.getMessageId() = {}", message.getMessageId());
        }
        if (true) {
            throw new RuntimeException("ThumbConsumer processBatch failed");
        }*/
        // 封装所有删除条件
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        // Key: blogId， Value: 赞数
        Map<Long, Long> countMap = new ConcurrentHashMap<>();

        // 封装到集合批量插入点赞记录
        List<Thumb> thumbs = new ArrayList<>();
        // 记录是否需要删除
        AtomicReference<Boolean> needRemove = new AtomicReference<>(false);

        // 提取事件并过滤无效消息
        List<ThumbEvent> events = messages.stream()
                .map(Message::getValue)
                .filter(Objects::nonNull)
                .toList();

        Map<Pair<Long, Long>, ThumbEvent> latestEvents = events.stream()
                // 先分组，根据UserId和BlogId进行分组
                .collect(Collectors.groupingBy(
                        e -> Pair.of(e.getUserId(), e.getBlogId()),
                        // 分组后封装成集合，并排序
                        Collectors.collectingAndThen(Collectors.toList(), list -> {
                            list.sort(Comparator.comparing(ThumbEvent::getEventTime));
                            // 如果是偶数，说明点赞操作数等于取消点赞操作数，等同于没操作
                            if (list.size() % 2 == 0) {
                                return null;
                            }
                            // 否则获得最晚操作
                            return list.getLast();
                        })
                ));

        // 遍历所有操作
        latestEvents.forEach((userBlobPair, event) -> {
            if (event == null) {
                return;
            }
            ThumbEvent.EventType finalAction = event.getType();
            // 点赞操作
            if (ThumbEvent.EventType.INCR.equals(finalAction)) {
                countMap.merge(event.getBlogId(), 1L, Long::sum);
                Thumb thumb = new Thumb();
                thumb.setUserId(event.getUserId());
                thumb.setBlogId(event.getBlogId());
                thumbs.add(thumb);
            } else {
                // 取消点赞操作
                needRemove.set(true);
                wrapper.or().eq(Thumb::getUserId, event.getUserId()).eq(Thumb::getBlogId, event.getBlogId());
                countMap.merge(event.getBlogId(), -1L, Long::sum);
            }
        });

        // 批量删除点赞记录
        if (needRemove.get()) {
            thumbService.remove(wrapper);
        }
        // 批量更新点赞数
        batchUpdateBlogs(countMap);
        // 批量插入点赞记录
        batchInsertThumbs(thumbs);
    }

    public void batchUpdateBlogs(Map<Long, Long> countMap) {
        if (!countMap.isEmpty()) {
            blogMapper.batchUpdateThumbCount(countMap);
        }
    }

    public void batchInsertThumbs(List<Thumb> thumbs) {
        if (!thumbs.isEmpty()) {
            thumbService.saveBatch(thumbs, 500);
        }
    }

    @PulsarListener(topics = "thumb-dlq-topic")
    public void consumeDlq(Message<ThumbEvent> message) {
        MessageId messageId = message.getMessageId();
        log.info("dlq message = {}", messageId);
        log.info("消息 {} 已入库", messageId);
        log.info("已通知相关人员 {} 处理消息 {}", "坤哥", messageId);
    }
}
