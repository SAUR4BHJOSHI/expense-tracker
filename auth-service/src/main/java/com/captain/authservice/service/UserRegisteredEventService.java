package com.captain.authservice.service;

import com.captain.authservice.dto.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class UserRegisteredEventService {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final String TOPIC_NAME;

    public UserRegisteredEventService(KafkaTemplate<String, byte[]> kafkaTemplate,
                                      @Value("${spring.kafka.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.TOPIC_NAME = topicName;
    }


    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        kafkaTemplate.send(TOPIC_NAME, serialize(event));
    }

    public static byte[] serialize(UserRegisteredEvent userRegisteredEvent) {
        // Implement serialization logic here
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.writeValueAsBytes(userRegisteredEvent);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing UserRegisteredEvent", e);
        }
    }
}
