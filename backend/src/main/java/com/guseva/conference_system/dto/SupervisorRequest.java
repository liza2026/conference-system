package com.guseva.conference_system.dto;

import jakarta.validation.constraints.NotBlank;

public record SupervisorRequest (
        @NotBlank String fullName,
        @NotBlank String post) {
}
