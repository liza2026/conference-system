package com.guseva.conference_system.util;

/** Вспомогательные функции для работы с именами файлов. */
public final class FileNames {

    private FileNames() {
    }

    /**
     * Расширение файла строчными буквами и без точки: "Тезисы.DOCX" -> "docx"
     * Если расширения нет (или имя не задано), возвращает пустую строку
     */
    public static String extension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
    }
}