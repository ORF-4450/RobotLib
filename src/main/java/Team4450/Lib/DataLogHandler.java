package Team4450.Lib;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;

/**
 * Publishes values to a DataLog
 */
public class DataLogHandler extends Handler {
    private StringLogEntry dataLogEntry;

    public DataLogHandler() {
        dataLogEntry = new StringLogEntry(DataLogManager.getLog(), "UtilConsoleLog");
    }

    @Override
    public void publish(LogRecord record) {
        dataLogEntry.append(getFormatter().format(record));
    }
    
    @Override
    public void close() throws SecurityException {}

    @Override
    public void flush() {}
}
