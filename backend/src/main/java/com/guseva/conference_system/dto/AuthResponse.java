package com.guseva.conference_system.dto;

public record AuthResponse(
        Long id,
        String lastName,
        String firstName,
        String middleName,
        String email
) {
}