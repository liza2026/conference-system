package com.guseva.conference_system.mapper;

import java.util.stream.Collectors;

import com.guseva.conference_system.util.FileNames;
import org.springframework.stereotype.Component;

import com.guseva.conference_system.dto.ApplicationResponse;
import com.guseva.conference_system.dto.AuthorResponse;
import com.guseva.conference_system.dto.SupervisorResponse;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.entity.Author;
import com.guseva.conference_system.entity.Supervisor;
import com.guseva.conference_system.util.PersonNames;

/** Превращает сущности в ответы API */
@Component
public class ApplicationMapper {

    public ApplicationResponse toResponse(Application a) {
        return new ApplicationResponse(
                a.getId(),
                a.getTitle(),
                a.getDirection().getId(),
                a.getDirection().getName(),
                a.getStatus(),
                thesisFileName(a),
                a.getAuthors().stream().map(this::toResponse).toList(),
                a.getSupervisors().stream().map(this::toResponse).toList(),
                a.getCreatedAt(),
                a.getUpdatedAt());
    }

    private AuthorResponse toResponse(Author a) {
        return new AuthorResponse(a.getPosition(), a.getLastName(), a.getFirstName(),
                a.getMiddleName(), a.getGroupName(), a.getPhone(), a.getEmail(), a.getFunding());
    }

    private SupervisorResponse toResponse(Supervisor s) {
        return new SupervisorResponse(s.getPosition(), s.getLastName(), s.getFirstName(),
                s.getMiddleName(), s.getPost());
    }

    /**
     * Имя файла по шаблону: "номер направления – фамилии и инициалы авторов",
     * например "2 – Иванов И.И., Карпов К.К.docx"
     */
    public String thesisFileName(Application a) {
        String authors = a.getAuthors().stream()
                .map(x -> PersonNames.shortName(x.getLastName(), x.getFirstName(), x.getMiddleName()))
                .collect(Collectors.joining(", "));
        String extension = FileNames.extension(a.getThesisFileName());
        return a.getDirection().getId() + " – " + authors
                + (extension.isEmpty() ? "" : "." + extension);
    }
}