package com.captain.userservice.service;

import com.captain.userservice.repository.UserInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserInfoServiceTests {
    private UserInfoRepository repository;
    private UserInfoService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserInfoRepository.class);
        service = new UserInfoService(repository);
    }

    @Test
    void insertsProfileUsingAuthIdentity() {
        UUID userId = UUID.randomUUID();
        when(repository.initializeProfile(userId, "Test", "User", "user@example.com")).thenReturn(1);
        service.initializeUserProfile(userId, "Test", "User", "user@example.com");
        verify(repository).initializeProfile(userId, "Test", "User", "user@example.com");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void duplicateDeliveryDoesNotInvokeAnUpdateOrSave() {
        UUID userId = UUID.randomUUID();
        when(repository.initializeProfile(userId, "Test", "User", "user@example.com")).thenReturn(1, 0);
        service.initializeUserProfile(userId, "Test", "User", "user@example.com");
        service.initializeUserProfile(userId, "Test", "User", "user@example.com");
        verify(repository, times(2)).initializeProfile(userId, "Test", "User", "user@example.com");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void rejectsMissingIdentityFields() {
        assertThrows(IllegalArgumentException.class, () -> service.initializeUserProfile(null, "Test", "User", "user@example.com"));
        assertThrows(IllegalArgumentException.class, () -> service.initializeUserProfile(UUID.randomUUID(), "Test", "User", null));
        assertThrows(IllegalArgumentException.class, () -> service.initializeUserProfile(UUID.randomUUID(), "Test", "User", " "));
        verifyNoInteractions(repository);
    }

    @Test
    void doesNotHidePersistenceFailures() {
        UUID userId = UUID.randomUUID();
        when(repository.initializeProfile(userId, "Test", "User", "user@example.com"))
                .thenThrow(new IllegalStateException("Database unavailable"));
        assertThrows(IllegalStateException.class, () -> service.initializeUserProfile(userId, "Test", "User", "user@example.com"));
    }
}
