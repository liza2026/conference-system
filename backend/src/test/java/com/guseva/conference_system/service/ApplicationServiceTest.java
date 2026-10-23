package com.guseva.conference_system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import com.guseva.conference_system.exception.ApplicationStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.guseva.conference_system.TestcontainersConfiguration;
import com.guseva.conference_system.dto.ApplicationRequest;
import com.guseva.conference_system.dto.AuthorRequest;
import com.guseva.conference_system.dto.SupervisorRequest;
import com.guseva.conference_system.entity.Application;
import com.guseva.conference_system.entity.ApplicationStatus;
import com.guseva.conference_system.entity.Author;
import com.guseva.conference_system.entity.Funding;
import com.guseva.conference_system.exception.NotFoundException;

import jakarta.persistence.EntityManager;

// Срок Т1 в будущем: все действия разрешены.
// @Transactional: после каждого теста изменения в базе откатываются, тесты не мешают друг другу.
@SpringBootTest(properties = "conference.deadline-t1=2099-12-31T23:59:59Z")
@Import(TestcontainersConfiguration.class)
@Transactional
class ApplicationServiceTest {

    @Autowired
    ApplicationService service;

    @Autowired
    EntityManager entityManager;

    // Подача сохраняет заявку со статусом SUBMITTED, двумя авторами по порядку и руководителем
    @Test
    void submitSavesApplication() {
        Application saved = service.submit(request("Название работы", 2, 2),
                "thesis.docx", new byte[] {1, 2, 3});
        reload();

        Application found = service.get(saved.getId());
        assertThat(found.getStatus()).isEqualTo(ApplicationStatus.SUBMITTED);
        assertThat(found.getTitle()).isEqualTo("Название работы");
        assertThat(found.getDirection().getId()).isEqualTo((short) 2);
        assertThat(found.getAuthors()).extracting(Author::getPosition)
                .containsExactly((short) 1, (short) 2);
        assertThat(found.getSupervisors()).hasSize(1);
        assertThat(found.getThesisFileName()).isEqualTo("thesis.docx");
        assertThat(found.getThesisFile()).isEqualTo(new byte[] {1, 2, 3});
    }

    // Подача с несуществующим направлением даёт NotFoundException
    @Test
    void submitUnknownDirectionFails() {
        assertThatThrownBy(() -> service.submit(request("Название", 99, 1),
                "thesis.docx", new byte[] {1}))
                .isInstanceOf(NotFoundException.class);
    }

    // Изменение меняет название и направление, а прежний файл остаётся, если новый не прислан
    @Test
    void updateChangesFieldsAndKeepsFile() {
        Application saved = service.submit(request("Старое название", 2, 1),
                "thesis.docx", new byte[] {1, 2, 3});
        reload();

        service.update(saved.getId(), request("Новое название", 7, 1), null, null);
        reload();

        Application found = service.get(saved.getId());
        assertThat(found.getTitle()).isEqualTo("Новое название");
        assertThat(found.getDirection().getId()).isEqualTo((short) 7);
        assertThat(found.getThesisFileName()).isEqualTo("thesis.docx");
        assertThat(found.getThesisFile()).isEqualTo(new byte[] {1, 2, 3});
    }

    // Если прислан новый файл, он заменяет прежний
    @Test
    void updateReplacesFile() {
        Application saved = service.submit(request("Название", 2, 1),
                "old.docx", new byte[] {1});
        reload();

        service.update(saved.getId(), request("Название", 2, 1), "new.doc", new byte[] {9, 9});
        reload();

        Application found = service.get(saved.getId());
        assertThat(found.getThesisFileName()).isEqualTo("new.doc");
        assertThat(found.getThesisFile()).isEqualTo(new byte[] {9, 9});
    }

    // Было два автора, стал один: лишний автор удаляется, ошибок уникальности нет
    @Test
    void updateTwoAuthorsToOne() {
        Application saved = service.submit(request("Название", 2, 2),
                "thesis.docx", new byte[] {1});
        reload();

        service.update(saved.getId(), request("Название", 2, 1), null, null);
        reload();

        Application found = service.get(saved.getId());
        assertThat(found.getAuthors()).extracting(Author::getLastName).containsExactly("Иванов");
    }

    // Был один автор, стало два: второй добавляется с номером 2
    @Test
    void updateOneAuthorToTwo() {
        Application saved = service.submit(request("Название", 2, 1),
                "thesis.docx", new byte[] {1});
        reload();

        service.update(saved.getId(), request("Название", 2, 2), null, null);
        reload();

        Application found = service.get(saved.getId());
        assertThat(found.getAuthors()).extracting(Author::getPosition)
                .containsExactly((short) 1, (short) 2);
        assertThat(found.getAuthors()).extracting(Author::getLastName)
                .containsExactly("Иванов", "Карпов");
    }

    // Отзыв меняет статус на WITHDRAWN, заявка из базы не удаляется
    @Test
    void withdrawSetsStatus() {
        Application saved = service.submit(request("Название", 2, 1),
                "thesis.docx", new byte[] {1});
        reload();

        service.withdraw(saved.getId());
        reload();

        assertThat(service.get(saved.getId()).getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    // Отозванную заявку изменить нельзя
    @Test
    void updateWithdrawnFails() {
        Application saved = service.submit(request("Название", 2, 1),
                "thesis.docx", new byte[] {1});
        service.withdraw(saved.getId());
        reload();

        assertThatThrownBy(() -> service.update(saved.getId(), request("Другое", 2, 1), null, null))
                .isInstanceOf(ApplicationStateException.class);
    }

    // Отозвать заявку второй раз нельзя
    @Test
    void withdrawTwiceFails() {
        Application saved = service.submit(request("Название", 2, 1),
                "thesis.docx", new byte[] {1});
        service.withdraw(saved.getId());
        reload();

        assertThatThrownBy(() -> service.withdraw(saved.getId()))
                .isInstanceOf(ApplicationStateException.class);
    }

    // Несуществующая заявка даёт NotFoundException
    @Test
    void getUnknownFails() {
        assertThatThrownBy(() -> service.get(999_999L))
                .isInstanceOf(NotFoundException.class);
    }

    // ---------- вспомогательные методы ----------

    // Записать накопленные изменения в базу и очистить кэш Hibernate:
    // дальше данные читаются из базы заново, и проверяются ограничения самой БД
    private void reload() {
        entityManager.flush();
        entityManager.clear();
    }

    // Готовая заявка: authorCount авторов (1 или 2) и один руководитель
    private ApplicationRequest request(String title, int directionId, int authorCount) {
        String[] lastNames = {"Иванов", "Карпов"};
        List<AuthorRequest> authors = new ArrayList<>();
        for (int i = 0; i < authorCount; i++) {
            authors.add(new AuthorRequest(lastNames[i], "Иван", null, "УТС-420",
                    "8-900-000-00-00", "author" + i + "@mail.ru", Funding.BUDGET));
        }
        List<SupervisorRequest> supervisors = List.of(
                new SupervisorRequest("Дроботов", "Алексей", "Владимирович", "доцент каф. АПП"));
        return new ApplicationRequest(title, (short) directionId, authors, supervisors);
    }
}