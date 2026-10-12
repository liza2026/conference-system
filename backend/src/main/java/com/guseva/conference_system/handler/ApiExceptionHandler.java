package com.guseva.conference_system.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.exception.DeadlinePassedException;
import com.guseva.conference_system.exception.NotFoundException;

/** Обработчик ошибок */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 409 Conflict: срок вышел или заявка уже отозвана
    @ExceptionHandler({DeadlinePassedException.class, ApplicationStateException.class})
    public ProblemDetail conflict(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
}