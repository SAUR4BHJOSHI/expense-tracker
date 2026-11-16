package com.captain.userservice.service;

import com.captain.userservice.dto.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationEventConsumerService {
    private final ObjectMapper objectMapper;
    private final UserInfoService userInfoService;

    @KafkaListener(
            topics = "${spring.kafka.topic.name:${spring.kafka.topic:et-user-registration}}",
            groupId = "${spring.kafka.consumer.group-id:user-service-profile}",
            containerFactory = "userRegistrationKafkaListenerContainerFactory")
    public void consumeUserRegistrationEvent(byte[] message) {
        UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);
        if (event == null || event.userId() == null
                || event.email() == null || event.email().isBlank()) {
            throw new IllegalArgumentException("Registration event requires userId and email");
        }
        // Older registration events have no names; onboarding can fill them later.
        log.info("Received registration event for userId: {}", event.userId());
        userInfoService.initializeUserProfile(event.userId(), event.firstName(), event.lastName(), event.email());
    }


}
