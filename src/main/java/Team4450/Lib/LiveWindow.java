package Team4450.Lib;

import java.util.Map;
import java.util.TreeMap;

import org.wpilib.telemetry.Telemetry;
import org.wpilib.telemetry.TelemetryLoggable;
import org.wpilib.tunable.ComplexTunable;
import org.wpilib.tunable.TunableRegistry;
import org.wpilib.tunable.TunableTable;

/**
 * With WPILib 2027, Sendable and LiveWindow were dropped. Sendable was
 * replaced with ComplexTunable but the Tunable API did not have a replacement
 * for LiveWindow. LiveWindow allowed classes to register Sendables but not
 * actually send them to the Network tables unless LiveWindow was enabled. This
 * allowed for classes to be able to send debugging/test data to the NT only
 * LW was enabled. So we only enabled LW during test (now utility) mode,
 * thereby not sending debug data during regular modes, ie during matches.
 * This scheme allows for lots of debug/test data to be sent over the network
 * only during testing and not during matches.
 * 
 * So this class implements a simple LiveWindow replacement allowing ComplexTunables
 * to be registered but not published unless requested by calling publish on this
 * class.
 * 
 * Later added TelemetryLoggables. ComplexTunables are published once and WPILib
 * will refresh them every execution loop. TelemetryLoggables are not refreshed
 * automatically so we do that ourselves in Robot.utilityPeriodic.
 */
public final class LiveWindow {
    private static final TunableTable rootTable = TunableRegistry.getTable("/");
    private static final TunableTable liveWindow = rootTable.getTable("LiveWindow");

    private static Map<String, ComplexTunable>      tunables = new TreeMap<>();
    private static Map<String, TelemetryLoggable>   loggables = new TreeMap<>();

    private static boolean published = false;

    private LiveWindow() {
        throw new UnsupportedOperationException("This is a utility class!");
    }
    
    /**
     * Register a class that implements ComplexTunable to be published 
     * if LiveWindow.publish() is called. The tunable is published to
     * the network tables as a subtable of the table "LiveWindow". The
     * table elements are defined by the class implementing ComplexTunable.
     * You may add a ComplexTunable table as a subtable of any other table
     * by using the parent table name followed by / then the subtable name
     * in the name parameter.
     * @param tunable Class implementing ComplexTunable.
     * @param name Name of tunable.
     */
    public static void registerComplexTunable(ComplexTunable tunable, String name)
    {
        Util.consoleLog("%s", name);

        // Publishes the tunable if registry has already been published.
        if (published && tunables.get(name) == null) liveWindow.publish(name, tunable);

        tunables.putIfAbsent(name, tunable);
    }
    
    /**
     * Register a class that implements ComplexTunable to be published 
     * if LiveWindow.publishTunables() is called. Appends id to name to create
     * a unique name. The tunable is published to the network tables as a subtable
     * of the table "LiveWindow". The table elements are defined by the class 
     * implementing ComplexTunable. You may add a ComplexTunable table as a subtable 
     * of any other table by using the parent table name followed by / then the subtable 
     * name in the name parameter.
     * @param tunable Class implementing ComplexTunable.
     * @param name Name of tunable.
     * @param id Identifying integer.
     */
    public static void registerComplexTunable(ComplexTunable tunable, String name, int id)
    {
        registerComplexTunable(tunable, String.format("%s-%d", name, id));
    }
    
    /**
     * Register a class that implements TelemetryLoggable to be published 
     * if LiveWindow.publishLoggables() is called. The loggable is published to
     * the network tables as a subtable of the table "LiveWindow". The
     * table elements are defined by the class implementing TelemetryLoggable.
     * You may add a TelemetryLoggable table as a subtable of any other table
     * by using the parent table name followed by / then the subtable name
     * in the name parameter.
     * @param loggable Class implementing TelemetryLoggable.
     * @param name Name of loggable.
     */
    public static void registerTelemetryLoggable(TelemetryLoggable loggable, String name)
    {
        Util.consoleLog("%s", name);

        loggables.putIfAbsent(name, loggable);
    }
    
    /**
     * Register a class that implements TelemetryLoggable to be published 
     * if LiveWindow.publish() is called. Appends id to name to create
     * a unique name. The loggable is published to the network tables as a subtable 
     * of the table "LiveWindow". The table elements are defined by the class 
     * implementing TelemetryLoggable. You may add a TelemetryLoggable table as a 
     * subtable of any other table by using the parent table name followed by / then 
     * the subtable name in the name parameter.
     * @param loggable Class implementing TelemetryLoggable.
     * @param name Name of loggable.
     * @param id Identifying integer.
     */
    public static void registerTelemetryLoggable(TelemetryLoggable loggable, String name, int id)
    {
        registerTelemetryLoggable(loggable, String.format("%s-%d", name, id));
    }

    /**
     * Publish the registry of ComplexTunables and TelemetryLoggables to the network tables.
     * Only needs to be called once. WPILib handles repeated publishing of each tunable's 
     * data on each execution loop.
     */
    public static void publishTunables()
    {
        Util.consoleLog();

        for (Map.Entry<String, ComplexTunable> entry : tunables.entrySet())
        {
            liveWindow.publish(entry.getKey(), entry.getValue());
        }

        published = true;
    }

    /**
     * Publish the registry of TelemetryLoggables to the network tables. This is
     * a one time publishing of the loggables data so it must be called each 
     * execution loop. Typically in Robot.utilityPeriodic.
     */
    public static void publishLoggables()
    {
        for (Map.Entry<String, TelemetryLoggable> entry : loggables.entrySet())
        {
            Telemetry.log(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Indicates if LiveWindow has been published.
     * @return True if LW has been published.
     */
    public static boolean isPublished()
    {
        return published;
    }
}
