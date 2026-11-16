package com.captain.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserRegistrationEventConsumerServiceTests {
    private UserInfoService profiles;
    private UserRegistrationEventConsumerService consumer;

    @BeforeEach
    void setUp() {
        profiles = mock(UserInfoService.class);
        consumer = new UserRegistrationEventConsumerService(new ObjectMapper(), profiles);
    }

    @Test
    void initializesProfileFromJsonBytes() {
        UUID userId = UUID.randomUUID();
        consumer.consumeUserRegistrationEvent(payload(userId));
        verify(profiles).initializeUserProfile(userId, "Test", "User", "user@example.com");
    }

    @Test
    void acceptsOlderEventsWithoutNames() {
        UUID userId = UUID.randomUUID();
        byte[] legacy = ("{\"userId\":\"" + userId + "\",\"email\":\"user@example.com\"}")
                .getBytes(StandardCharsets.UTF_8);
        consumer.consumeUserRegistrationEvent(legacy);
        verify(profiles).initializeUserProfile(userId, null, null, "user@example.com");
    }

    @Test
    void rejectsMissingFieldsAndInvalidJson() {
        for (String invalid : new String[]{"{}", "null", "not-json",
                "{\"userId\":\"invalid\",\"email\":\"user@example.com\"}",
                "{\"userId\":\"" + UUID.randomUUID() + "\",\"email\":\" \"}"}) {
            assertThrows(RuntimeException.class, () ->
                    consumer.consumeUserRegistrationEvent(invalid.getBytes(StandardCharsets.UTF_8)));
        }
        verifyNoInteractions(profiles);
    }

    @Test
    void propagatesDatabaseFailureForKafkaRetry() {
        UUID userId = UUID.randomUUID();
        doThrow(new IllegalStateException("Database unavailable"))
                .when(profiles).initializeUserProfile(userId, "Test", "User", "user@example.com");
        assertThrows(IllegalStateException.class, () -> consumer.consumeUserRegistrationEvent(payload(userId)));
    }

    private byte[] payload(UUID userId) {
        return ("{\"userId\":\"" + userId
                + "\",\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"user@example.com\"}")
                .getBytes(StandardCharsets.UTF_8);
    }
}
