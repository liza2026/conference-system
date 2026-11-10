package com.guseva.conference_system.dto;

public record AuthorResponse (
        short position,
        Long userId,
        String lastName,
        String firstName,
        String middleName,
        String email
) {
}