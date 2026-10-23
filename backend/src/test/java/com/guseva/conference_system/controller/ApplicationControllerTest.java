package com.guseva.conference_system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

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
import com.jayway.jsonpath.JsonPath;

// Срок Т1 в будущем. Запросы идут в контроллер без запуска сервера (MockMvc)
// @Transactional: после каждого теста база откатывается
@SpringBootTest(properties = "conference.deadline-t1=2099-12-31T23:59:59Z")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ApplicationControllerTest {

    @Autowired
    MockMvc mockMvc;

    // успешные запросы

    // Корректная заявка принимается: 201, статус SUBMITTED, имя файла собрано по шаблону
    @Test
    void submitValidReturns201() throws Exception {
        submit(data("Название", 2, author("Иванов")), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.thesisFileName").value("2 – Иванов И.И..docx"));
    }

    // Пробелы по краям фамилии убираются до проверки
    @Test
    void submitTrimsSpaces() throws Exception {
        submit(data("Название", 2, author("  Иванов  ")), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.authors[0].lastName").value("Иванов"));
    }

    // Изменение без нового файла: 200, название и направление новые
    @Test
    void updateWithoutFileReturns200() throws Exception {
        long id = submitValid();

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/applications/{id}", id)
                        .file(dataPart(data("Новое название", 7, author("Иванов")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Новое название"))
                .andExpect(jsonPath("$.directionId").value(7));
    }

    // ошибки 400: плохие данные

    // Пустая фамилия автора
    @Test
    void submitEmptyLastNameFails() throws Exception {
        submit(data("Название", 2, author("")), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Фамилия с цифрами или спецсимволами
    @Test
    void submitInvalidLastNameFails() throws Exception {
        submit(data("Название", 2, author("Иванов123")), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Некорректный e-mail
    @Test
    void submitInvalidEmailFails() throws Exception {
        submit(data("Название", 2, author("Иванов", "не-почта")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Три автора: больше двух нельзя
    @Test
    void submitThreeAuthorsFails() throws Exception {
        submit(data("Название", 2, author("Иванов"), author("Карпов"), author("Сидоров")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Ни одного автора
    @Test
    void submitNoAuthorsFails() throws Exception {
        submit(data("Название", 2), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Файл не в формате .doc/.docx
    @Test
    void submitPdfFails() throws Exception {
        submit(data("Название", 2, author("Иванов")), filePart("thesis.pdf", new byte[] {1}))
                .andExpect(status().isBadRequest());
    }

    // Пустой файл
    @Test
    void submitEmptyFileFails() throws Exception {
        submit(data("Название", 2, author("Иванов")), filePart("thesis.docx", new byte[0]))
                .andExpect(status().isBadRequest());
    }

    // ошибки 404: не найдено

    // Несуществующее направление
    @Test
    void submitUnknownDirectionReturns404() throws Exception {
        submit(data("Название", 99, author("Иванов")), filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isNotFound());
    }

    // Несуществующая заявка
    @Test
    void getUnknownReturns404() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    // ошибки 409: противоречит состоянию заявки

    // Повторный отзыв
    @Test
    void withdrawTwiceReturns409() throws Exception {
        long id = submitValid();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/applications/{id}/withdraw", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/applications/{id}/withdraw", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Заявка " + id + " уже отозвана"));
    }

    // Изменение отозванной заявки
    @Test
    void updateWithdrawnReturns409() throws Exception {
        long id = submitValid();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/applications/{id}/withdraw", id));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/applications/{id}", id)
                        .file(dataPart(data("Другое", 2, author("Иванов")))))
                .andExpect(status().isConflict());
    }

    // вспомогательные методы

    // Подать корректную заявку и вернуть её номер
    private long submitValid() throws Exception {
        String body = submit(data("Название", 2, author("Иванов")),
                filePart("thesis.docx", new byte[] {1}))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private ResultActions submit(String dataJson, MockMultipartFile file) throws Exception {
        return mockMvc.perform(multipart("/api/applications")
                .file(dataPart(dataJson))
                .file(file));
    }

    // Часть запроса "data": JSON с данными заявки
    private MockMultipartFile dataPart(String json) {
        return new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes(StandardCharsets.UTF_8));
    }

    // Часть запроса "file": файл тезисов
    private MockMultipartFile filePart(String fileName, byte[] content) {
        return new MockMultipartFile("file", fileName,
                MediaType.APPLICATION_OCTET_STREAM_VALUE, content);
    }

    // JSON одного автора
    private String author(String lastName) {
        return author(lastName, "ivanov@mail.ru");
    }

    private String author(String lastName, String email) {
        return """
                {"lastName": "%s", "firstName": "Иван", "middleName": "Иванович",
                 "groupName": "УТС-420", "phone": "8-900-000-00-00",
                 "email": "%s", "funding": "BUDGET"}""".formatted(lastName, email);
    }

    // JSON данных заявки с заданными авторами и одним руководителем
    private String data(String title, int directionId, String... authors) {
        return """
                {"title": "%s", "directionId": %d,
                 "authors": [%s],
                 "supervisors": [{"lastName": "Дроботов", "firstName": "Алексей",
                                  "post": "доцент каф. АПП"}]}"""
                .formatted(title, directionId, String.join(",", authors));
    }
}