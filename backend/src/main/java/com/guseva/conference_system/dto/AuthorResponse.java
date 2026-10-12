package com.guseva.conference_system.dto;

import com.guseva.conference_system.entity.Funding;

public record AuthorResponse (
        short position,
        String lastName,
        String firstName,
        String middleName,
        String groupName,
        String phone,
        String email,
        Funding funding) {
}