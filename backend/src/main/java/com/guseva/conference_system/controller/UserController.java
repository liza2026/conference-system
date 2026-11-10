package com.guseva.conference_system.controller;

import com.guseva.conference_system.dto.UserRequest;
import com.guseva.conference_system.dto.UserResponse;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody UserRequest request) {

        if (users.existsByEmail(request.email())) {
            throw new ApplicationStateException(
                    "Пользователь с email " + request.email() + " уже существует");
        }

        User user = new User(
                request.lastName(),
                request.firstName(),
                request.middleName(),
                request.email()
        );

        User saved = users.save(user);

        UserResponse response = new UserResponse(
                saved.getId(),
                saved.getLastName(),
                saved.getFirstName(),
                saved.getMiddleName(),
                saved.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}