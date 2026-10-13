package com.guseva.conference_system.util;

/** Вспомогательные функции для работы с ФИО */
public final class PersonNames {

    private PersonNames() {
    }

    /** "Иванов", "Иван", "Иванович" -> "Иванов И.И."; без отчества -> "Иванов И." */
    public static String shortName(String lastName, String firstName, String middleName) {
        StringBuilder result = new StringBuilder(lastName.trim())
                .append(' ')
                .append(initial(firstName));
        if (middleName != null && !middleName.isBlank()) {
            result.append(initial(middleName));
        }
        return result.toString();
    }

    private static String initial(String name) {
        return Character.toUpperCase(name.trim().charAt(0)) + ".";
    }
}