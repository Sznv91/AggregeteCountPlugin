package ru.mgts.sazonov.test.plugin;

import java.util.Date;

import static ru.mgts.sazonov.test.plugin.DiskWriter.eventType.WITHOUT_FORMATTING;
import static ru.mgts.sazonov.test.plugin.SazonovFirstPlugin.*;

/**
 * Класс выполняющий смещение счётчика на 1 каждый интервал заданный в
 * <br>
 * {@link SazonovFirstPlugin#TIMEOUT_VALUE}
 * <br>
 * Затем выполняющий запись на диск с помощью метода
 * {@link DiskWriter#writeToDisk}
 */
public class Counter extends Thread {
    private long counter = 0L;

    @Override
    public void run() {
        count();
    }

    private void count() {
        while (true) {
            if (CHECKBOX_VALUE){
                DiskWriter.getInstance().writeToDisk(DiskWriter.eventType.WITHOUT_FORMATTING,
                        String.format("%s Counter: %s Date:%s%n", PREFIX_VALUE, counter, new Date()));
                counter++;
            }
            try {
                Thread.sleep(TIMEOUT_VALUE * MILLIS);
            } catch (InterruptedException e) {
                DiskWriter.getInstance().
                        writeToDisk(WITHOUT_FORMATTING, Utils.errorFormatting(e, this));
            }
        }
    }
}
