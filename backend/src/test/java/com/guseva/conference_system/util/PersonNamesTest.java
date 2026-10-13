package com.guseva.conference_system.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonNamesTest {

    // Полное ФИО сокращается до фамилии и двух инициалов
    @Test
    void shortNameWithMiddleName() {
        assertThat(PersonNames.shortName("Иванов", "Иван", "Иванович"))
                .isEqualTo("Иванов И.И.");
    }

    // null отчество
    @Test
    void shortNameWithoutMiddleName() {
        assertThat(PersonNames.shortName("Карпов", "Кирилл", null))
                .isEqualTo("Карпов К.");
    }

    // Пустое отчество
    @Test
    void shortNameWithBlankMiddleName() {
        assertThat(PersonNames.shortName("Карпов", "Кирилл", "  "))
                .isEqualTo("Карпов К.");
    }

    // Имя, введённое с маленькой буквы - в итоге всё заглавное
    @Test
    void shortNameLowercaseFirstName() {
        assertThat(PersonNames.shortName("Иванов", "иван", null))
                .isEqualTo("Иванов И.");
    }

    // Двойная фамилия должна сохраниться целиком
    @Test
    void shortNameDoubleBarrelLastName() {
        assertThat(PersonNames.shortName("Салтыков-Щедрин", "Михаил", "Евграфович"))
                .isEqualTo("Салтыков-Щедрин М.Е.");
    }

    // Иностранное имя с отчеством (например, при двойном гражданстве)
    @Test
    void shortNameForeignWithMiddleName() {
        assertThat(PersonNames.shortName("Smith", "John", "Robert"))
                .isEqualTo("Smith J.R.");
    }

    // Иностранная фамилия с дефисом
    @Test
    void shortNameForeignDoubleBarrel() {
        assertThat(PersonNames.shortName("Garcia-Marquez", "Gabriel", "Jose"))
                .isEqualTo("Garcia-Marquez G.J.");
    }

    // Иностранная фамилия с апострофом (O'Brien)
    @Test
    void shortNameForeignWithApostrophe() {
        assertThat(PersonNames.shortName("O'Brien", "Conan", null))
                .isEqualTo("O'Brien C.");
    }

    // Имя из одной буквы
    @Test
    void shortNameSingleLetterFirstName() {
        assertThat(PersonNames.shortName("Ли", "Я", null))
                .isEqualTo("Ли Я.");
    }

    // Отчество из одной буквы
    @Test
    void shortNameSingleLetterMiddleName() {
        assertThat(PersonNames.shortName("Иванов", "Иван", "П"))
                .isEqualTo("Иванов И.П.");
    }

    // Имя с ведущими и замыкающими пробелами - пробелы обрезаются
    @Test
    void shortNameWithTrimmedSpaces() {
        assertThat(PersonNames.shortName("  Иванов  ", "  Иван  ", "  Иванович  "))
                .isEqualTo("Иванов И.И.");
    }

    // Отчество null, имя с пробелами
    @Test
    void shortNameWithSpacesAndNullMiddleName() {
        assertThat(PersonNames.shortName("Карпов", "  Кирилл  ", null))
                .isEqualTo("Карпов К.");
    }

    // Пустая строка вместо отчества
    @Test
    void shortNameWithEmptyMiddleName() {
        assertThat(PersonNames.shortName("Карпов", "Кирилл", ""))
                .isEqualTo("Карпов К.");
    }

    // Иностранное имя с двойным именем через пробел
    @Test
    void shortNameForeignDoubleFirstName() {
        assertThat(PersonNames.shortName("Smith", "John Paul", null))
                .isEqualTo("Smith J.");
    }

    // Иностранное имя с двойным именем через дефис
    @Test
    void shortNameForeignHyphenatedFirstName() {
        assertThat(PersonNames.shortName("Smith", "John-Paul", null))
                .isEqualTo("Smith J.");
    }
}