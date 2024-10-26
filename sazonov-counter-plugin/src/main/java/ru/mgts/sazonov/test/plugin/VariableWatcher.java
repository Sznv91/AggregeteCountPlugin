package ru.mgts.sazonov.test.plugin;

import com.tibbo.aggregate.common.context.CallerController;
import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.ContextException;

import static ru.mgts.sazonov.test.plugin.DiskWriter.eventType.*;
import static ru.mgts.sazonov.test.plugin.SazonovFirstPlugin.*;

/**
 * Класс наблюдающий за изменениями состояния переменных определённых в классе
 * {@link SazonovFirstPlugin}
 */
@SuppressWarnings({"rawtypes", "RedundantThrows"})
public class VariableWatcher extends Thread {
    private static final int RESCAN_INTERVAL = 10;
    private final Context context;
    private final CallerController caller;

    public VariableWatcher(Context context, CallerController caller) {
        this.context = context;
        this.caller = caller;
    }

    private void watch() {
        while (true) {
            try {
                boolean isEnableToWrite = context.getVariable(CONFIG_COUNTER_VARIABLE_DEFINITION_NAME, caller).rec().getBoolean(CHECKBOX_FIELD_NAME);
                String tmpPrefix = context.getVariable(CONFIG_COUNTER_VARIABLE_DEFINITION_NAME, caller).rec().getString(PREFIX_FIELD_NAME);
                int tmpTimeout = context.getVariable(CONFIG_COUNTER_VARIABLE_DEFINITION_NAME, caller).rec().getInt(TIMEOUT_WRITE_FIELD_NAME);
                String tmpFilePath = context.getVariable(CONFIG_FILE_VARIABLE_DEFINITION_NAME, caller).rec().getString(FILEPATH_VARIABLE_NAME);
                String tmpFileName = context.getVariable(CONFIG_FILE_VARIABLE_DEFINITION_NAME, caller).rec().getString(FILE_NAME_VARIABLE_NAME);


                if (isEnableToWrite != CHECKBOX_VALUE) {
                    DiskWriter.getInstance().writeToDisk(CHANGE_VARIABLE, CHECKBOX_FIELD_NAME, String.valueOf(CHECKBOX_VALUE), String.valueOf(isEnableToWrite));
                    CHECKBOX_VALUE = isEnableToWrite;
                }
                if (!tmpPrefix.equals(PREFIX_VALUE)) {
                    DiskWriter.getInstance().writeToDisk(CHANGE_VARIABLE, PREFIX_FIELD_NAME, PREFIX_VALUE, tmpPrefix);
                    PREFIX_VALUE = tmpPrefix;
                }
                if (tmpTimeout != TIMEOUT_VALUE) {
                    DiskWriter.getInstance().writeToDisk(CHANGE_VARIABLE, TIMEOUT_WRITE_FIELD_NAME, String.valueOf(TIMEOUT_VALUE), String.valueOf(tmpTimeout));
                    TIMEOUT_VALUE = tmpTimeout;
                }
                if (!tmpFilePath.equals(FILEPATH) || !tmpFileName.equals(FILE_NAME)) {
                    DiskWriter.getInstance().writeToDisk(CHANGE_VARIABLE, "Filepath\\FileName", FILEPATH + "\\" + FILE_NAME, tmpFilePath + "\\" + tmpFileName);
                    FILEPATH = tmpFilePath;
                    FILE_NAME = tmpFileName;
                    DiskWriter.getInstance().reNew();
                }

                Thread.sleep(RESCAN_INTERVAL * MILLIS);
            } catch (InterruptedException | ContextException e) {
                DiskWriter.getInstance().
                        writeToDisk(WITHOUT_FORMATTING, Utils.errorFormatting(e, this));
            }

        }


    }

    @Override
    public void run() {
        watch();
    }
}
