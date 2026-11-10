
package com.guseva.conference_system.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guseva.conference_system.dto.ApplicationRequest;
import com.guseva.conference_system.dto.AuthorRequest;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.entity.ApplicationStatus;
import com.guseva.conference_system.entity.Direction;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.exception.DeadlinePassedException;
import com.guseva.conference_system.exception.NotFoundException;
import com.guseva.conference_system.repository.ApplicationRepository;
import com.guseva.conference_system.repository.DirectionRepository;
import com.guseva.conference_system.repository.UserRepository;

/**
 * Сервис подачи, изменения и отзыва заявок
 */
@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applications;
    private final DirectionRepository directions;
    private final UserRepository users;
    private final EntityManager entityManager;
    private final OffsetDateTime deadlineT1;

    public ApplicationService(
            ApplicationRepository applications,
            DirectionRepository directions,
            UserRepository users,
            EntityManager entityManager,
            @Value("${conference.deadline-t1}") OffsetDateTime deadlineT1) {

        this.applications = applications;
        this.directions = directions;
        this.users = users;
        this.entityManager = entityManager;
        this.deadlineT1 = deadlineT1;
    }

    /**
     * Подать новую заявку
     */
    public Application submit(
            ApplicationRequest request,
            String fileName,
            byte[] file) {

        checkDeadline();

        Direction direction = findDirection(request.directionId());

        List<User> authors = resolveAuthors(request.authors());
        checkSubmitterIsAuthor(request);

        Application application = new Application(
                request.title(),
                direction,
                fileName,
                file,
                now()
        );

        for (User author : authors) {
            application.addAuthor(author);
        }

        return applications.save(application);
    }

    /**
     * Изменить поданную заявку.
     * Если fileName == null, прежний файл сохраняется.
     */
    public Application update(
            Long id,
            ApplicationRequest request,
            String fileName,
            byte[] file) {

        checkDeadline();

        Application application = findSubmitted(id);

        List<User> authors = resolveAuthors(request.authors());
        checkSubmitterIsAuthor(request);

        application.change(
                request.title(),
                findDirection(request.directionId()),
                now()
        );

        if (fileName != null) {
            application.replaceThesisFile(fileName, file);
        }

        syncAuthors(application, authors);

        return application;
    }

    /**
     * Отозвать заявку.
     * Запись не удаляется — меняется только её статус.
     */
    public Application withdraw(Long id) {
        checkDeadline();

        Application application = findSubmitted(id);
        application.withdraw(now());

        return application;
    }

    // Чтение

    @Transactional(readOnly = true)
    public Application get(Long id) {
        return applications.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Заявка " + id + " не найдена"));
    }

    @Transactional(readOnly = true)
    public List<Application> list() {
        return applications.findAll();
    }

    // Вспомогательные методы

    private Instant now() {
        return Instant.now();
    }

    private void checkDeadline() {
        if (OffsetDateTime.now().isAfter(deadlineT1)) {
            throw new DeadlinePassedException();
        }
    }

    private Application findSubmitted(Long id) {
        Application application = get(id);

        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ApplicationStateException(
                    "Заявка " + id + " уже отозвана"
            );
        }

        return application;
    }

    private Direction findDirection(Short id) {
        return directions.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Направление " + id + " не найдено"));
    }

    /**
     * Получить пользователей, указанных в запросе как авторы.
     * Порядок пользователей соответствует порядку в запросе.
     */
    private List<User> resolveAuthors(List<AuthorRequest> items) {
        List<User> result = new ArrayList<>();
        Set<Long> authorIds = new HashSet<>();

        for (AuthorRequest item : items) {
            Long userId = item.userId();

            if (userId == null) {
                throw new ApplicationStateException(
                        "Необходимо указать пользователя для каждого автора");
            }

            if (!authorIds.add(userId)) {
                throw new ApplicationStateException(
                        "Один пользователь не может быть указан автором несколько раз");
            }

            User user = users.findById(userId)
                    .orElseThrow(() -> new NotFoundException(
                            "Пользователь " + userId + " не найден"));

            result.add(user);
        }

        return result;
    }

    /**
     * Проверить, что пользователь, отправляющий заявку,
     * указан среди её авторов.
     */
    private void checkSubmitterIsAuthor(ApplicationRequest request) {
        boolean isAuthor = request.authors().stream()
                .anyMatch(author ->
                        author.userId().equals(request.submitterUserId()));

        if (!isAuthor) {
            throw new ApplicationStateException(
                    "Подающий заявку пользователь должен быть указан среди авторов"
            );
        }
    }

    /**
     * Заменить список авторов заявки.
     *
     * Сначала удаляем старые связи и сбрасываем изменения в БД,
     * затем добавляем новый список.
     */
    private void syncAuthors(Application application, List<User> authors) {
        while (!application.getAuthors().isEmpty()) {
            application.removeLastAuthor();
        }

        entityManager.flush();

        for (User author : authors) {
            application.addAuthor(author);
        }
    }
}
