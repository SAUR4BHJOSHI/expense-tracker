package com.captain.userservice.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
public class UserRegistrationKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> userRegistrationKafkaListenerContainerFactory(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var factory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // Basic no-loss policy: retry failures until fixed; a bad event blocks its partition.
        // Introduce a dead-letter topic before enabling bounded retries in production.
        var errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, FixedBackOff.UNLIMITED_ATTEMPTS));
        errorHandler.setClassifications(Map.of(Exception.class, true), true);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
