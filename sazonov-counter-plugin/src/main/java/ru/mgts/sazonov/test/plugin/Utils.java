package ru.mgts.sazonov.test.plugin;

public class Utils {
    public static String errorFormatting(Exception e, Object caller) {
        return String.format("Произошла ужасная ошибка: %s в классе: %s. Message:%n%s%n",
                e.getClass().getName(), caller.getClass().getName(), e.getLocalizedMessage());
    }
}
