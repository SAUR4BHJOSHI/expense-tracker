package com.captain.userservice.service;

import com.captain.userservice.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;

    @Transactional
    public void initializeUserProfile(UUID userId, String firstName, String lastName, String email) {
        if (userId == null || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Registration requires userId and email");
        }

        int inserted = userInfoRepository.initializeProfile(userId, firstName, lastName, email);
        if (inserted == 1) {
            log.info("User profile initialized for userId: {}", userId);
        } else {
            log.debug("Profile already exists for userId: {}", userId);
        }
    }
}
