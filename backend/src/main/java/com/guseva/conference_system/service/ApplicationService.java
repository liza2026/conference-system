package com.guseva.conference_system.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.guseva.conference_system.dto.ApplicationRequest;
import com.guseva.conference_system.dto.AuthorRequest;
import com.guseva.conference_system.dto.SupervisorRequest;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.entity.ApplicationStatus;
import com.guseva.conference_system.entity.Direction;
import com.guseva.conference_system.exception.ApplicationStateException;
import com.guseva.conference_system.exception.DeadlinePassedException;
import com.guseva.conference_system.exception.NotFoundException;
import com.guseva.conference_system.repository.ApplicationRepository;
import com.guseva.conference_system.repository.DirectionRepository;

/**
 * Сервис заявок: участник может подать, изменить и отозвать заявку, но только до времени Т1
 */
@Service
@Transactional
public class ApplicationService {

    // С чем работает сервис: репозитории (доступ к базе) и срок Т1.
    private final ApplicationRepository applications;
    private final DirectionRepository directions;
    private final OffsetDateTime deadlineT1;

    public ApplicationService(ApplicationRepository applications,
                              DirectionRepository directions,
                              @Value("${conference.deadline-t1}") OffsetDateTime deadlineT1) {
        this.applications = applications;
        this.directions = directions;
        this.deadlineT1 = deadlineT1;
    }

    /**
     * Подать новую заявку
     *
     * @param request  данные, введённые участником (название, направление, авторы, руководители)
     * @param fileName имя загруженного файла с тезисами
     * @param file     содержимое файла
     */
    public Application submit(ApplicationRequest request, String fileName, byte[] file) {
        //  подавать можно только до Т1
        checkDeadline();

        // найти направление участника
        Direction direction = findDirection(request.directionId());

        // создать заявку
        Application application = new Application(request.title(), direction, fileName, file, now());

        // добавить автора и руководителя
        syncAuthors(application, request.authors());
        syncSupervisors(application, request.supervisors());

        // сохранить
        return applications.save(application);
    }

    /**
     * Изменить поданную заявку
     * Если fileName == null, участник файл не менял - прежний файл остаётся
     */
    public Application update(Long id, ApplicationRequest request, String fileName, byte[] file) {
        //  подавать можно только до Т1
        checkDeadline();

        // найти заявку участника
        Application application = findSubmitted(id);

        // обновить основные поля заявки
        application.change(request.title(), findDirection(request.directionId()), now());

        // если участник прислал файл - обновить файл
        if (fileName != null) {
            application.replaceThesisFile(fileName, file);
        }

        // привести список авторов и руководителей к обновлённой информации
        syncAuthors(application, request.authors());
        syncSupervisors(application, request.supervisors());

        // Вызывать save не нужно: заявка уже "под наблюдением" Hibernate внутри транзакции.
        // Он сам заметит изменения и запишет их в базу, когда метод завершится.
        return application;
    }

    /**
     * Отозвать заявку
     * Запись из базы не удаляется, меняется только статус на "отозвана"
     */
    public Application withdraw(Long id) {
        checkDeadline();
        Application application = findSubmitted(id);
        application.withdraw(now());
        return application;
    }

    //  Чтение

    /** Найти заявку по номеру
     * Если такой нет - ошибка NotFoundException */
    @Transactional(readOnly = true)
    public Application get(Long id) {
        return applications.findById(id)
                .orElseThrow(() -> new NotFoundException("Заявка " + id + " не найдена"));
    }

    /** Список всех заявок */
    @Transactional(readOnly = true)
    public List<Application> list() {
        return applications.findAll();
    }


    //  Вспомогательные методы

    /** Текущий момент времени */
    private Instant now() {
        return Instant.now();
    }

    /** Правило срока: если Т1 уже прошло, любое действие участника запрещено */
    private void checkDeadline() {
        if (OffsetDateTime.now().isAfter(deadlineT1)) {
            throw new DeadlinePassedException();
        }
    }

    /**
     * Найти заявку и убедиться, что она не отозвана
     */
    private Application findSubmitted(Long id) {
        Application application = get(id);
        if (application.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new ApplicationStateException("Заявка " + id + " уже отозвана");
        }
        return application;
    }

    /** Найти направление по номеру
     * Если такого нет - ошибка NotFoundException */
    private Direction findDirection(Short id) {
        return directions.findById(id)
                .orElseThrow(() -> new NotFoundException("Направление " + id + " не найдено"));
    }

    /**
     * Привести авторов заявки к обновлённому списку
     */
    private void syncAuthors(Application application, List<AuthorRequest> items) {
        int existing = application.getAuthors().size(); // сколько авторов уже есть
        for (int i = 0; i < items.size(); i++) {
            AuthorRequest item = items.get(i);
            if (i < existing) {
                // автор с таким номером уже есть - обновляем его данные
                application.getAuthors().get(i).update(item.lastName(), item.firstName(),
                        item.middleName(), item.groupName(), item.phone(), item.email(), item.funding());
            } else {
                // такого номера ещё нет - добавляем нового
                application.addAuthor(item.lastName(), item.firstName(),
                        item.middleName(), item.groupName(), item.phone(), item.email(), item.funding());
            }
        }
        // если авторов стало меньше - убрать лишних с конца
        while (application.getAuthors().size() > items.size()) {
            application.removeLastAuthor();
        }
    }

    /**
     * Привести научных руководителей заявки к обновлённому списку
     * */
    private void syncSupervisors(Application application, List<SupervisorRequest> items) {
        int existing = application.getSupervisors().size();
        for (int i = 0; i < items.size(); i++) {
            SupervisorRequest item = items.get(i);
            if (i < existing) {
                // руководитель с таким номером уже есть - обновляем его данные
                application.getSupervisors().get(i).update(item.lastName(), item.firstName(),
                        item.middleName(), item.post());
            } else {
                // такого номера ещё нет - добавляем нового
                application.addSupervisor(item.lastName(), item.firstName(),
                        item.middleName(), item.post());
            }
        }
        // если авторов стало меньше - убрать лишних с конца
        while (application.getSupervisors().size() > items.size()) {
            application.removeLastSupervisor();
        }
    }
}