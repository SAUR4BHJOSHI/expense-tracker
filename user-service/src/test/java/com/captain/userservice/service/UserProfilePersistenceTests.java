package com.captain.userservice.service;

import com.captain.userservice.entity.UserInfo;
import com.captain.userservice.repository.UserInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserProfilePersistenceTests {
    @Autowired
    private UserInfoService profiles;

    @Autowired
    private UserInfoRepository repository;

    @Test
    void storesNamesAndPreservesCompletedProfilesOnRedelivery() {
        UUID userId = UUID.randomUUID();
        String email = userId + "@example.com";
        profiles.initializeUserProfile(userId, "Test", "User", email);

        UserInfo profile = repository.findById(userId).orElseThrow();
        assertEquals("Test", profile.getFirstName());
        assertEquals("User", profile.getLastName());
        assertEquals(email, profile.getEmail());
        assertEquals("INCOMPLETE", profile.getProfileStatus());
        assertEquals("INR", profile.getPreferredCurrency());
        assertNotNull(profile.getCreatedAt());
        assertNotNull(profile.getUpdatedAt());

        profile.setProfileStatus("COMPLETE");
        repository.saveAndFlush(profile);
        profiles.initializeUserProfile(userId, "Replacement", "Name", email);

        assertEquals("Test", repository.findById(userId).orElseThrow().getFirstName());
        assertEquals("COMPLETE", repository.findById(userId).orElseThrow().getProfileStatus());
        assertEquals(0, repository.initializeProfile(userId, "Replacement", "Name", email));
    }
}
