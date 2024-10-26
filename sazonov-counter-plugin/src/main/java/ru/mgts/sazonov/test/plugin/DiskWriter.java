package ru.mgts.sazonov.test.plugin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import static ru.mgts.sazonov.test.plugin.SazonovFirstPlugin.FILEPATH;
import static ru.mgts.sazonov.test.plugin.SazonovFirstPlugin.FILE_NAME;

public class DiskWriter {
    private static DiskWriter instance;

    private DiskWriter() {
    }

    public static DiskWriter getInstance() {
        if (instance == null) {
            instance = new DiskWriter();
        }
        return instance;
    }

    public void reNew(){
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        instance = null;
    }

    private FileWriter writer;

    {
        try {
            writer = new FileWriter(FILEPATH + "\\" + FILE_NAME);
            writer.write("<-------------Start new session------------->" + System.lineSeparator());
        } catch (IOException e) {
            try {
                writer = new FileWriter("C:\\test_out.txt");
                writer.write("<-------------Start new session------------->" + System.lineSeparator());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void shutdown() {
        try {
            writer.write("<-------------End new session------------->" + System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        instance = null;
    }

    public enum eventType {
        CHANGE_VARIABLE,
        CALL_METHOD,
        WITHOUT_FORMATTING
    }

    private static final String ChangeVariableTemplate = "Изменение состояния переменной %s. Старое значение: %s, новое значение:%s. Время: %s%n";
    private static final String CallMethodTemplate = "Выполнен запрос в метод: %s. Время: %s%n";

    /**
     * @param type    Изменение переменной или вызов метода.
     * @param message Изменение переменной: 3 аргумента (Название переменной, старое значение, новое значение).
     *                Вызов метода: 1 аргумент. Название вызывающего метода.
     */
    public void writeToDisk(eventType type, String... message) {
        String preparedMessage = "";
        switch (type) {
            case WITHOUT_FORMATTING:
                preparedMessage = message[0];
                break;
            case CHANGE_VARIABLE:
                preparedMessage = String.format(ChangeVariableTemplate, message[0], message[1], message[2], new Date());
                break;
            case CALL_METHOD:
                preparedMessage = String.format(CallMethodTemplate, message[0], new Date());
                break;
        }
        try {
            writer.write(preparedMessage);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
