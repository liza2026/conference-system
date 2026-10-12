package com.guseva.conference_system.exception;

/** Действие невозможно в текущем статусе заявки (например, изменение отозванной). */
public class ApplicationStateException extends RuntimeException {
    public ApplicationStateException(String message) {
        super(message);
    }
}