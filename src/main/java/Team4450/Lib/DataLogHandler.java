package Team4450.Lib;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;

/**
 * 
 * A custom implementation of Java's Log {@link Handler} that allows formatted
 * log entries to be appended to the current {@code DataLog} which will be saved
 * to RoboRio USB drive for later use in AdvantageScope log replay.
 * 
 * @since 4.8.3
 * @author Cole Wilson
 */
public class DataLogHandler extends Handler {
    private StringLogEntry dataLogEntry;

    /**
     * Default constructor to create a {@code DataLogHandler} object with the default
     * {@code DataLogEntry} key of "UtilConsoleLog"
     */
    public DataLogHandler() {
        this("UtilConsoleLog");
    }

    /**
     * Constructor to create a {@code DataLogHandler} object with the specified
     * {@code DataLogEntry} key
     * 
     * @param key the key to be used when logging to the current {@code DataLog}
     */
    public DataLogHandler(String key) {
        dataLogEntry = new StringLogEntry(DataLogManager.getLog(), key);
    }

    /**
     * Publish the given {@link LogRecord} to the current {@code DataLog} under the
     * pre-specified key in the constructor (or default key of "UtilConsoleLog"). This method
     * will auto-format the record using the formatter specified previously with
     * {@link DataLogHandler#setFormatter(java.util.logging.Formatter)}.
     * 
     * @param record the {@code LogRecord} to be published
     */
    @Override
    public void publish(LogRecord record) {
        dataLogEntry.append(getFormatter().format(record));
    }
    
    /** 
     * Does nothing, as the {@code DataLog} object does not need to be closed,
     * but still must be defined.
     */
    @Override
    public void close() throws SecurityException {}

    /**
     * Does nothing, as the flushing the stream is handled by the {@code DataLogManager}
     * but is required to be defined.
    */
    @Override
    public void flush() {}
}
