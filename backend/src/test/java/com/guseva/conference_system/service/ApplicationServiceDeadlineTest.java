package com.guseva.conference_system.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.guseva.conference_system.TestcontainersConfiguration;
import com.guseva.conference_system.dto.ApplicationRequest;
import com.guseva.conference_system.exception.DeadlinePassedException;

// Срок Т1 в прошлом: подавать, изменять и отзывать заявки уже нельзя
@SpringBootTest(properties = "conference.deadline-t1=2000-01-01T00:00:00Z")
@Import(TestcontainersConfiguration.class)
class ApplicationServiceDeadlineTest {

    @Autowired
    ApplicationService service;

    // Пустая заявка: срок проверяется раньше всего остального, поэтому содержимое не важно
    private final ApplicationRequest request =
            new ApplicationRequest("Название", (short) 2, List.of(), List.of());

    // После Т1 подать заявку нельзя
    @Test
    void submitAfterDeadlineFails() {
        assertThatThrownBy(() -> service.submit(request, "thesis.docx", new byte[] {1}))
                .isInstanceOf(DeadlinePassedException.class);
    }

    // После Т1 изменить заявку нельзя (срок проверяется раньше, чем поиск заявки)
    @Test
    void updateAfterDeadlineFails() {
        assertThatThrownBy(() -> service.update(1L, request, null, null))
                .isInstanceOf(DeadlinePassedException.class);
    }

    // После Т1 отозвать заявку нельзя
    @Test
    void withdrawAfterDeadlineFails() {
        assertThatThrownBy(() -> service.withdraw(1L))
                .isInstanceOf(DeadlinePassedException.class);
    }
}