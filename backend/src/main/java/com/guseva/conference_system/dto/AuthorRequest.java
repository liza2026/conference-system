package com.guseva.conference_system.dto;

import com.guseva.conference_system.entity.Funding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record AuthorRequest (
        @NotBlank @Size(max = 100) @Pattern(regexp = "^([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String lastName,

        @NotBlank @Size(max = 100) @Pattern(regexp = "^([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String firstName,

        // отчество необязательное: может быть null или пустым, но если есть - только буквы
        @Size(max = 100) @Pattern(regexp = "^([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String middleName,

        @NotBlank String groupName,
        @NotBlank String phone,
        @NotBlank @Email String email,
        @NotNull Funding funding) {

    public AuthorRequest {
        lastName = StringUtils.trimWhitespace(lastName);
        firstName = StringUtils.trimWhitespace(firstName);
        middleName = StringUtils.hasText(middleName) ? middleName.trim() : null;
        groupName = StringUtils.trimWhitespace(groupName);
        phone = StringUtils.trimWhitespace(phone);
        email = StringUtils.trimWhitespace(email);
    }
}