package com.guseva.conference_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record SupervisorRequest (
        @NotBlank @Size(max = 100) @Pattern(regexp = "([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String lastName,

        @NotBlank @Size(max = 100) @Pattern(regexp = "([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String firstName,

        @Size(max = 100) @Pattern(regexp = "^([\\p{L}][\\p{L} '\\-]*)?$",
                message = "только буквы, пробел, дефис") String middleName,

        @NotBlank String post) {

    public SupervisorRequest {
        lastName = StringUtils.trimWhitespace(lastName);
        firstName = StringUtils.trimWhitespace(firstName);
        middleName = StringUtils.hasText(middleName) ? middleName.trim() : null;
        post = StringUtils.trimWhitespace(post);
    }
}
