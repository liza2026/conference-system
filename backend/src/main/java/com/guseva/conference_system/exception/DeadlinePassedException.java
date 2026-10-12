package com.guseva.conference_system.exception;

/** Время Т1 прошло: подавать, изменять и отзывать заявки уже нельзя. */
public class DeadlinePassedException extends RuntimeException {
    public DeadlinePassedException() {
        super("Приём заявок закончен (время Т1 прошло)");
    }
}