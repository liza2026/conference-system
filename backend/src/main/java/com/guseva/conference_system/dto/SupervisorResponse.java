package com.guseva.conference_system.dto;

public record SupervisorResponse (
        short position,
        String lastName,
        String firstName,
        String middleName,
        String post) {
}