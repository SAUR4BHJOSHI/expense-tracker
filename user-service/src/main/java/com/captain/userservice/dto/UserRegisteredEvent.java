package com.captain.userservice.dto;

import java.util.UUID;

public record UserRegisteredEvent(UUID userId,
                                  String firstName,
                                  String lastName,
                                  String email) {
}
