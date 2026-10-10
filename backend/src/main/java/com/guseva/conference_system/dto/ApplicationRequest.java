package com.guseva.conference_system.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Данные заявки, которые вводит участник */
public record ApplicationRequest (
        @NotBlank @Size(max = 500) String title,
        @NotNull Short directionId,
        @NotEmpty @Size(max = 2) List<@Valid AuthorRequest> authors,
        @NotEmpty @Size(max = 2) List<@Valid SupervisorRequest> supervisors) {
}
