package com.haryokuncoro.ops.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderCreatedTopic() {

        return TopicBuilder.name("order.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    manualAckFactory(ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setConcurrency(3);

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.MANUAL
        );

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
    autoAckFactory(ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        factory.setConcurrency(3);

        return factory;
    }
}