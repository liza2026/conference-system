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

        @NotBlank @Size(max = 50) String groupName,
        @NotBlank @Size(max = 30)
        @Pattern(regexp = "^(?:(?=(?:\\D*\\d){10,11}\\D*$)\\+?[0-9\\s()\\-]+)?$",
                message = "укажите номер из 10-11 цифр, например 8-900-000-00-00") String phone,
        @NotBlank @Size(max = 255) @Email String email,
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