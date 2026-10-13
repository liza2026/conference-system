package com.guseva.conference_system.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FileNamesTest {

    // Расширение берётся после последней точки
    @Test
    void extensionAfterLastDot() {
        assertThat(FileNames.extension("thesis.docx")).isEqualTo("docx");
        assertThat(FileNames.extension("archive.tar.gz")).isEqualTo("gz");
    }

    // Расширение приводится к строчным буквам
    @Test
    void extensionIsLowercase() {
        assertThat(FileNames.extension("Тезисы.DOCX")).isEqualTo("docx");
    }

    // Нет расширения или имени - пустая строка
    @Test
    void extensionMissing() {
        assertThat(FileNames.extension("thesis")).isEmpty();
        assertThat(FileNames.extension("thesis.")).isEmpty();
        assertThat(FileNames.extension(null)).isEmpty();
    }
}