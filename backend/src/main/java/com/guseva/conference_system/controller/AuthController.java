package com.guseva.conference_system.controller;

import com.guseva.conference_system.dto.AuthResponse;
import com.guseva.conference_system.dto.LoginRequest;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            UserRepository userRepository) {

        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Сначала проверяем, зарегистрирован ли пользователь
        User user = userRepository
                .findByEmailIgnoreCase(request.email())
                .orElse(null);

        if (user == null) {
            SecurityContextHolder.clearContext();

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Вы ещё не зарегистрированы. Создайте аккаунт."
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
        }

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.email(),
                                    request.password()
                            )
                    );

            if (httpRequest.getSession(false) != null) {
                httpRequest.changeSessionId();
            }

            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(
                    context,
                    httpRequest,
                    httpResponse
            );

            AuthResponse response = new AuthResponse(
                    user.getId(),
                    user.getLastName(),
                    user.getFirstName(),
                    user.getMiddleName(),
                    user.getEmail()
            );

            return ResponseEntity.ok(response);

        } catch (AuthenticationException exception) {
            SecurityContextHolder.clearContext();

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Неверный пароль. Попробуйте ещё раз."
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(user -> ResponseEntity.ok(new AuthResponse(
                        user.getId(),
                        user.getLastName(),
                        user.getFirstName(),
                        user.getMiddleName(),
                        user.getEmail()
                )))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
                );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        return ResponseEntity.noContent().build();
    }
}