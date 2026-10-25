package Team4450.Lib;

import java.util.Map;
import java.util.TreeMap;

import org.wpilib.tunable.ComplexTunable;
import org.wpilib.tunable.TunableRegistry;
import org.wpilib.tunable.TunableTable;

/**
 * With WPILib 2027, Sendable and LiveWindow were dropped. Sendable was
 * replaced with ComplexTunable but the Tunable API did not have a replacement
 * for LiveWindow. LiveWindow allowed classes to register Sendables but not
 * actually send them to the Network tables unless LiveWindw was enabled. This
 * allowed for classes to be able to send debugging/test data to the NT only
 * LW was enabled. So we only enabled LW during test (now utility) mode,
 * thereby not sending debug data during regular modes, ie during matches.
 * This scheme allows for lots of debug/test data to be sent over the network
 * only during testing and not during matches.
 * 
 * So this class implements a simple LiveWindow replacement allowing ComplexTunables
 * to be registered but not published unless requested by calling publish on this
 * class.
 */
public final class LiveWindow {
    private static final TunableTable rootTable = TunableRegistry.getTable("/");
    private static final TunableTable liveWindow = rootTable.getTable("LiveWindow");

    private static Map<String, ComplexTunable> tunables = new TreeMap<>();

    private static boolean published = false;

    private LiveWindow() {
        throw new UnsupportedOperationException("This is a utility class!");
    }
    
    /**
     * Register a class that implements ComplexTunable to be published 
     * if LiveWindow.publish() is called.
     * @param tunable Class implementing ComplexTunable.
     * @param name Name of tunable.
     */
    public static void registerComplexTunable(ComplexTunable tunable, String name)
    {
        // Publishes the tunable if registry has already been published.
        if (published && tunables.get(name) == null)
            liveWindow.publish(name, tunable);

        tunables.putIfAbsent(name, tunable);
    }
    
    /**
     * Register a class that implements ComplexTunable to be published 
     * if LiveWindow.publish() is called. Appends id to name to create
     * a unique name.
     * @param tunable Class implementing ComplexTunable.
     * @param name Name of tunable.
     * @param id Identifying integer.
     */
    public static void registerComplexTunable(ComplexTunable tunable, String name, int id)
    {
        registerComplexTunable(tunable, String.format("%s-%d", name, id));
    }

    /**
     * Publish the registry of ComplexTunables to the network tables.
     */
    public static void publish()
    {
        for (Map.Entry<String, ComplexTunable> entry : tunables.entrySet())
        {
            liveWindow.publish(entry.getKey(), entry.getValue());
        }

        published = true;
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
