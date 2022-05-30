package Team4450.Lib;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.wpi.first.wpilibj.RobotController;

/**
 * A class used to record the time spent in functions to identify long running
 * functions. Call enterFunction() at start of a function and exitFunction()
 * at the end of the function. Will track total time spent in all the functions
 * where these calls are made until the list of functions is printed and reset
 * by printFunctions(). Use the INSTANCE member instead of getting a reference
 * variable: FunctionTimer.INSTANCE.enterFunction()
 */
public class FunctionTracer 
{
    // Singleton class pattern single instance.
    public static final FunctionTracer INSTANCE = new FunctionTracer();

    //private final Map<String, FunctionMarker> functions = new HashMap<>();

    private final Map<String, FunctionMarker> functions = Collections.synchronizedMap(new HashMap<>());
    
    private FunctionTracer() {}

    /**
     * Returns a reference pointer to the global instance of Tracer.
     * @return Instance pointer.
     */
    public FunctionTracer getInstance() { return INSTANCE; }

    private class FunctionMarker
    {
        public long		cumulative, start;
        public long		threadId;

        public FunctionMarker(long startTime, long cumulativeTime)
        {
            start = startTime;
            cumulative = cumulativeTime;
            
            threadId = Thread.currentThread().getId();
        }
    }

    /**
     * Call as the first statement in a function to prime the tracking of the
     * time spent in the function.
     * @param name The function name.
     */
    public void enterFunction(String name)
    {
        long now = RobotController.getFPGATime();

        FunctionMarker marker = functions.get(name);

        if  (marker == null)
            marker = new FunctionMarker(now, 0);
        else
            marker.start = now;
        
        functions.put(name, marker);
    }

    /**
     * Call as the last statement in a function to record the time spent
     * in the function.
     * @param name Function name. Must be the same name used on enterFunction().
     */
    public void exitFunction(String name)
    {
        long now = RobotController.getFPGATime();

        FunctionMarker marker = functions.get(name);

        if  (marker == null)
        	return;
        else
            marker.cumulative += now - marker.start;
        
        functions.put(name, marker);
    }

    /**
     * Clear the list of functions.
     */
    public void clearFunctions()
    {
        functions.clear();
    }

    /**
     * Print list of functions called and accumulated time since last call
     * to this function or clearFunctions();
     * @param out The print stream to print the list  to.
     */
    public void printFunctions(PrintStream out)
    {
        StringBuilder   sb = new StringBuilder();

        functions.forEach(
            (key, marker) -> {
                sb.append(String.format("\t%s<%d>: %.4fs\n", key, marker.threadId, marker.cumulative / 1.0e6));
            });
        
        if (sb.length() > 0) out.print(sb.toString());

        clearFunctions();
    }
}
