package com.guseva.conference_system.handler;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.exception.DeadlinePassedException;
import com.guseva.conference_system.exception.InvalidFileException;
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

    // Ошибки проверки введённых данных: 400, а в поле "errors" - "поле -> что не так"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new TreeMap<>();
        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(f.getField(), f.getDefaultMessage()); // одна ошибка на поле
        }
        return badRequest(errors);
    }

    // Проблема с файлом тезисов относится к полю "file"
    @ExceptionHandler(InvalidFileException.class)
    public ProblemDetail invalidFile(InvalidFileException e) {
        return badRequest(Map.of("file", e.getMessage()));
    }

    private ProblemDetail badRequest(Map<String, String> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Проверьте введённые данные");
        problem.setProperty("errors", errors);
        return problem;
    }
}