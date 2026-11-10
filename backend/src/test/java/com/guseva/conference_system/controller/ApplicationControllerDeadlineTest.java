
package com.guseva.conference_system.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.guseva.conference_system.TestcontainersConfiguration;
import com.guseva.conference_system.entity.User;
import com.guseva.conference_system.repository.UserRepository;

// Срок Т1 в прошлом.
@SpringBootTest(properties = "conference.deadline-t1=2000-01-01T00:00:00Z")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
class ApplicationControllerDeadlineTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository users;

    // Корректная заявка после Т1:
    // данные проходят валидацию, но сервис отказывает с ответом 409.
    @Test
    void submitAfterDeadlineReturns409() throws Exception {
        User user = users.save(new User(
                "Иванов",
                "Иван",
                "Иванович",
                "author-" + UUID.randomUUID() + "@mail.ru"));

        users.flush();

        String userId = user.getId().toString();

        String json = """
                {
                    "title": "Название",
                    "directionId": 2,
                    "submitterUserId": %s,
                    "authors": [
                        {"userId": %s}
                    ]
                }
                """.formatted(userId, userId);

        MockMultipartHttpServletRequestBuilder request =
                multipart("/api/applications")
                        .file(new MockMultipartFile(
                                "data",
                                "",
                                MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile(
                                "file",
                                "thesis.docx",
                                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                                new byte[] {1}));

        mockMvc.perform(request)
                .andExpect(status().isConflict());
    }
}
