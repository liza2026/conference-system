
package com.guseva.conference_system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.guseva.conference_system.TestcontainersConfiguration;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

// Срок Т1 в будущем. Запросы идут в контроллер без запуска сервера (MockMvc).
// После каждого теста транзакция откатывается.
@SpringBootTest(properties = "conference.deadline-t1=2099-12-31T23:59:59Z")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ApplicationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository users;

    // Успешные запросы

    @Test
    void submitValidReturns201() throws Exception {
        submit(data("Название", 2, author("Иванов")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.thesisFileName")
                        .value("2 – Иванов И.И..docx"));
    }

    @Test
    void submitReturnsAuthorProfile() throws Exception {
        submit(data("Название", 2, author("Иванов")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authors[0].lastName").value("Иванов"))
                .andExpect(jsonPath("$.authors[0].firstName").value("Иван"))
                .andExpect(jsonPath("$.authors[0].email").exists());
    }

    @Test
    void updateWithoutFileReturns200() throws Exception {
        long id = submitValid();

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/applications/{id}", id)
                        .file(dataPart(data("Новое название", 7, author("Иванов")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Новое название"))
                .andExpect(jsonPath("$.directionId").value(7));
    }

    // Ошибки 400: некорректные данные

    @Test
    void submitThreeAuthorsFails() throws Exception {
        submit(data("Название", 2,
                        author("Иванов"),
                        author("Карпов"),
                        author("Сидоров")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitNoAuthorsFails() throws Exception {
        submit(data("Название", 2),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitPdfFails() throws Exception {
        submit(data("Название", 2, author("Иванов")),
                filePart("thesis.pdf", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    @Test
    void submitEmptyFileFails() throws Exception {
        submit(data("Название", 2, author("Иванов")),
                filePart("thesis.docx", new byte[0]))
                .andExpect(status().isBadRequest());
    }

    // Ошибки 404: объект не найден

    @Test
    void submitUnknownDirectionReturns404() throws Exception {
        submit(data("Название", 99, author("Иванов")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUnknownReturns404() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    // Ошибки 409: конфликт состояния заявки

    @Test
    void withdrawTwiceReturns409() throws Exception {
        long id = submitValid();

        mockMvc.perform(org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders.post(
                                "/api/applications/{id}/withdraw", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));

        mockMvc.perform(org.springframework.test.web.servlet.request
                        .MockMvcRequestBuilders.post(
                                "/api/applications/{id}/withdraw", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Заявка " + id + " уже отозвана"));
    }

    @Test
    void updateWithdrawnReturns409() throws Exception {
        long id = submitValid();

        mockMvc.perform(org.springframework.test.web.servlet.request
                .MockMvcRequestBuilders.post(
                        "/api/applications/{id}/withdraw", id));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/applications/{id}", id)
                        .file(dataPart(data("Другое", 2, author("Иванов")))))
                .andExpect(status().isConflict());
    }

    // Вспомогательные методы

    // Подать корректную заявку и вернуть её номер.
    private long submitValid() throws Exception {
        String body = submit(data("Название", 2, author("Иванов")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private ResultActions submit(String dataJson, MockMultipartFile file)
            throws Exception {
        return mockMvc.perform(multipart("/api/applications")
                .file(dataPart(dataJson))
                .file(file));
    }

    // JSON-часть запроса с данными заявки.
    private MockMultipartFile dataPart(String json) {
        return new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    // Файл тезисов.
    private MockMultipartFile filePart(String fileName, byte[] content) {
        return new MockMultipartFile(
                "file",
                fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                content);
    }

    // Создаём пользователя и возвращаем его ID.
    private String author(String lastName) {
        User user = users.save(new User(
                lastName,
                "Иван",
                "Иванович",
                "author-" + UUID.randomUUID() + "@mail.ru"));

        users.flush();

        return user.getId().toString();
    }

    // JSON заявки. Первый автор одновременно является отправителем.
    private String data(String title, int directionId, String... authors) {
        String submitterUserId = authors.length == 0 ? "null" : authors[0];

        return """
                {
                    "title": "%s",
                    "directionId": %d,
                    "submitterUserId": %s,
                    "authors": [%s]
                }
                """.formatted(
                title,
                directionId,
                submitterUserId,
                java.util.Arrays.stream(authors)
                        .map(id -> """
                                {"userId": %s}
                                """.formatted(id))
                        .collect(java.util.stream.Collectors.joining(",")));
    }
}
