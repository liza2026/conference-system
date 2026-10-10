package com.guseva.conference_system.dto;

import com.guseva.conference_system.entity.Funding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthorRequest (
        @NotBlank String fullName,
        @NotBlank String groupName,
        @NotBlank String phone,
        @NotBlank @Email String email,
        @NotNull Funding funding) {
}
