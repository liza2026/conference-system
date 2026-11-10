package com.guseva.conference_system.mapper;

import java.util.stream.Collectors;

import com.guseva.conference_system.dto.ApplicationResponse;
import com.guseva.conference_system.dto.AuthorResponse;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.entity.Author;
import com.guseva.conference_system.util.FileNames;
import com.guseva.conference_system.util.PersonNames;
import org.springframework.stereotype.Component;

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
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    private AuthorResponse toResponse(Author a) {
        return new AuthorResponse(
                a.getPosition(),
                a.getUser().getId(),
                a.getUser().getLastName(),
                a.getUser().getFirstName(),
                a.getUser().getMiddleName(),
                a.getUser().getEmail()
        );
    }

    /**
     * Имя файла по шаблону:
     * «номер направления – фамилии и инициалы авторов».
     */
    public String thesisFileName(Application a) {
        String authors = a.getAuthors().stream()
                .map(author -> PersonNames.shortName(
                        author.getUser().getLastName(),
                        author.getUser().getFirstName(),
                        author.getUser().getMiddleName()
                ))
                .collect(Collectors.joining(", "));

        String extension = FileNames.extension(a.getThesisFileName());

        return a.getDirection().getId() + " – " + authors
                + (extension.isEmpty() ? "" : "." + extension);
    }
}