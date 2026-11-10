package com.guseva.conference_system.dto;

import jakarta.validation.constraints.NotNull;

public record AuthorRequest (
        @NotNull
        Long userId
) {
}