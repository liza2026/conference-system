package com.guseva.conference_system.exception;

/** Файл тезисов не подходит: пустой или не .doc/.docx */
public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }
}