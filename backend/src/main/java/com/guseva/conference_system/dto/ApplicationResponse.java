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
        String thesisFileName,   // имя по шаблону из письма, вычисляется, в базе не хранится
        List<AuthorResponse> authors,
        List<SupervisorResponse> supervisors,
        Instant createdAt,
        Instant updatedAt) {
}