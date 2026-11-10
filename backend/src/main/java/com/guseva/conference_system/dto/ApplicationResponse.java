package com.guseva.conference_system.dto;

import java.time.Instant;
import java.util.List;

import com.guseva.conference_system.entity.ApplicationStatus;

public record ApplicationResponse (
        Long id,
        String title,
        Short directionId,
        String directionName,
        ApplicationStatus status,
        String thesisFileName,
        List<AuthorResponse> authors,
        Instant createdAt,
        Instant updatedAt
) {
}