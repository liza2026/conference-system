
package com.guseva.conference_system.controller;

import com.guseva.conference_system.dto.UserRequest;
import com.guseva.conference_system.dto.UserResponse;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserRequest request
    ) {

        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пароль слишком длинный"
            );
        }

        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ApplicationStateException(
                    "Пользователь с email " + request.email()
                            + " уже существует"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.lastName(),
                request.firstName(),
                request.middleName(),
                request.email(),
                passwordHash
        );

        User saved = users.save(user);

        UserResponse response = new UserResponse(
                saved.getId(),
                saved.getLastName(),
                saved.getFirstName(),
                saved.getMiddleName(),
                saved.getEmail()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}