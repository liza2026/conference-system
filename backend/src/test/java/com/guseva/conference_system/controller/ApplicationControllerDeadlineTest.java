package com.guseva.conference_system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.guseva.conference_system.TestcontainersConfiguration;

// Срок Т1 в прошлом
@SpringBootTest(properties = "conference.deadline-t1=2000-01-01T00:00:00Z")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ApplicationControllerDeadlineTest {

    @Autowired
    MockMvc mockMvc;

    // Корректная заявка после Т1: данные проходят проверку, но сервис отказывает, ответ 409
    @Test
    void submitAfterDeadlineReturns409() throws Exception {
        String json = """
                {"title": "Название", "directionId": 2,
                 "authors": [{"lastName": "Иванов", "firstName": "Иван",
                              "groupName": "УТС-420", "phone": "8-900-000-00-00",
                              "email": "ivanov@mail.ru", "funding": "BUDGET"}],
                 "supervisors": [{"lastName": "Дроботов", "firstName": "Алексей",
                                  "post": "доцент каф. АПП"}]}""";

        mockMvc.perform(multipart("/api/applications")
                        .file(new MockMultipartFile("data", "", MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "thesis.docx",
                                MediaType.APPLICATION_OCTET_STREAM_VALUE, new byte[] {1})))
                .andExpect(status().isConflict());
    }
}