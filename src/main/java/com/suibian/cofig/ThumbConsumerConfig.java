package com.suibian.cofig;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;

@Configuration
public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {
    @Override
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder.batchReceivePolicy(BatchReceivePolicy.builder()
                // 最大批量拉取数量
                .maxNumMessages(1000)
                // 最长等待时间
                .timeout(10000, TimeUnit.MILLISECONDS)
                .build());
    }

    // 配置 NACK 重试策略
    @Bean
    public RedeliveryBackoff NackRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(1000)
                .maxDelayMs(60_000)
                .multiplier(2)
                .build();
    }

    // 配置 ACK 超时重试策略
    @Bean
    public RedeliveryBackoff AckRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(3000)
                .maxDelayMs(300_000)
                .multiplier(3)
                .build();
    }

    // 死信主题
    @Bean
    public DeadLetterPolicy deadLetterPolicy() {
        return DeadLetterPolicy.builder()
                .maxRedeliverCount(3)
                .deadLetterTopic("thumb-dlq-topic")
                .build();
    }
}
