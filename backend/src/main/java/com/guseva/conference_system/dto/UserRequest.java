package com.guseva.conference_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

import java.util.Locale;

public record UserRequest(

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String middleName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 8, max = 72)
        String password

) {
        public UserRequest {
                lastName = StringUtils.trimWhitespace(lastName);
                firstName = StringUtils.trimWhitespace(firstName);

                middleName = StringUtils.hasText(middleName)
                        ? StringUtils.trimWhitespace(middleName)
                        : null;

                email = email == null
                        ? null
                        : email.trim().toLowerCase(Locale.ROOT);
        }
}
