package ru.mgts.sazonov.test.plugin;

import com.tibbo.aggregate.common.context.Context;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.context.ContextUtils;
import com.tibbo.aggregate.common.context.UncheckedCallerController;
import com.tibbo.aggregate.common.context.VariableDefinition;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.TableFormat;
import com.tibbo.aggregate.common.plugin.AbstractContextPlugin;
import com.tibbo.aggregate.common.plugin.PluginException;
import com.tibbo.aggregate.common.server.ServerContext;

import static ru.mgts.sazonov.test.plugin.DiskWriter.eventType.*;

/**
 * Работа плагина протестирована на версии сервера AggreGate v6.34.01-1894
 */

@SuppressWarnings({"rawtypes", "RedundantThrows"})
public class SazonovFirstPlugin extends AbstractContextPlugin {
    private VariableWatcher watcher;
    private final Counter counter = new Counter();
    protected static final String CONFIG_COUNTER_VARIABLE_DEFINITION_NAME = "needWriteToDisk";
    protected static final String PREFIX_FIELD_NAME = "contextContent";
    protected static final String TIMEOUT_WRITE_FIELD_NAME = "timeOutWrite";
    protected static final String CHECKBOX_FIELD_NAME = "isNeedWrite";

    protected static String PREFIX_VALUE = "default_prefix";
    protected static boolean CHECKBOX_VALUE = false;
    protected static int TIMEOUT_VALUE = 30;

    protected static final long MILLIS = 1000L;
    private static final TableFormat FORMAT = new TableFormat(1, 1);
    private static final TableFormat FILE_FORMAT = new TableFormat(1, 1);
    static final String CONFIG_FILE_VARIABLE_DEFINITION_NAME = "fileConfig";
    protected static String FILEPATH_VARIABLE_NAME = "filePath";
    protected static String FILE_NAME_VARIABLE_NAME = "fileName";
    protected static String FILEPATH = "C:";
    protected static String FILE_NAME = "test_out.txt";

    static {
        FORMAT.addField(FieldFormat.create(CHECKBOX_FIELD_NAME, FieldFormat.BOOLEAN_FIELD, "Выполнять запись на диск?", CHECKBOX_VALUE).setHelp("Записи о событиях обращения к методам будут записаны вне зависимости от того включен параметр или нет."));
        FORMAT.addField(FieldFormat.create(PREFIX_FIELD_NAME, FieldFormat.STRING_FIELD, "Префикс", PREFIX_VALUE));
        FORMAT.addField(FieldFormat.create(TIMEOUT_WRITE_FIELD_NAME, FieldFormat.INTEGER_FIELD, "Таймаут записи (сек.)", TIMEOUT_VALUE, false));
        FILE_FORMAT.addField(FieldFormat.create(FILEPATH_VARIABLE_NAME, FieldFormat.STRING_FIELD, "Путь для сохранения файла", FILEPATH, false).
                setHelp(String.format("Путь вводится в формате:%n\"C:\\example dir\"%nКаталог который указывается в качестве пути - должен существовать на диске, иначе вывод будет выполнен по дефолтному пути:%n\"C:\\test_out.txt\"")));
        FILE_FORMAT.addField(FieldFormat.create(FILE_NAME_VARIABLE_NAME, FieldFormat.STRING_FIELD, "Имя файла", FILE_NAME, false));
    }

    @Override
    public void globalInit(Context rootContext) throws PluginException {
        VariableDefinition counterDefinition = new VariableDefinition(CONFIG_COUNTER_VARIABLE_DEFINITION_NAME, FORMAT, true, true, "Настройки каунтера", ContextUtils.GROUP_DEFAULT);
        VariableDefinition fileStorageDefinition = new VariableDefinition(CONFIG_FILE_VARIABLE_DEFINITION_NAME, FILE_FORMAT, true, true, "Настройки хранения файлов", ContextUtils.GROUP_DEFAULT);

        this.createGlobalConfigContext(rootContext, false, new VariableDefinition[]{counterDefinition, fileStorageDefinition});
        DiskWriter.getInstance().writeToDisk(CALL_METHOD, "globalInit(Context rootContext)");
    }

    @Override
    public void globalDeinit(Context rootContext) throws PluginException {
        DiskWriter.getInstance().writeToDisk(CALL_METHOD, "globalDeinit(Context rootContext)");
        watcher.interrupt();
        counter.interrupt();
        DiskWriter.getInstance().shutdown();
    }

    @Override
    public void launch() throws PluginException {
        DiskWriter.getInstance().writeToDisk(CALL_METHOD, "launch()");
        watcher = new VariableWatcher(this.getGlobalConfigContext(), new UncheckedCallerController());
        watcher.start();
        counter.start();
    }

    //scope: metrics -> нет доступа к GlobalConfigContext
    @Override
    public void install(ServerContext context) throws ContextException, PluginException {
        DiskWriter.getInstance().writeToDisk(CALL_METHOD, "install(ServerContext context)");
    }
}
