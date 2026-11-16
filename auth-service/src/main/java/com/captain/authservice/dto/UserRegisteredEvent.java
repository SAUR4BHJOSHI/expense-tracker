package com.captain.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserRegisteredEvent {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;


}
