
package Team4450.Lib;

//import edu.wpi.first.wpilibj.Sendable;
import Team4450.Lib.Wpilib.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.util.WPILibVersion;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Obtain and transmit version information to SmartDasboard as a Sendable.
 * Reads version info from jar manifest as written by build.gradle.
 * Courtesy of Team 2502.
 */
public class SendableVersion implements Sendable
{
	// Singleton class single instance.
    public static final SendableVersion INSTANCE = new SendableVersion();

    private String name;
    private String branch;
    private String commit;
    private String programVersion;
    private String robotlibVersion;
    private String time;
    private String user;
    private int	   updateCount;

    // Private constructor since this class is a singleton.
    
    private SendableVersion()
    {
        branch = "unknown";
        commit = "unknown";
        programVersion = "unknown";
        robotlibVersion = "unknown";
        time = "unknown";
        user = "unknown";
		
		registerSendable("SendableVersion");
    }

    /**
     * Initialize version info with the calling program's version, the
     * RobotLib version and information from manifest file in the program jar.
     * Must be called before using SmartDashboard.putData() to send this class.
     * @param programVersion Robot program version.
     */
    public void init(String programVersion)
    {
    	this.programVersion = programVersion;
    	
    	robotlibVersion = LibraryVersion.version;
    	
        Class<SendableVersion> clazz = SendableVersion.class;
        
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        
        if(!classPath.startsWith("jar")) { return; }
        
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        
        Manifest manifest;
        
        try
        {
            manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            
            time = attr.getValue("Time");
            user = attr.getValue("User");
            branch = attr.getValue("Branch");
            commit = attr.getValue("Commit");
        }
        catch(Exception ignored) { Util.logException(ignored); }
    }

    /**
     * Initialize Sendable. Do not call, used by SmartDashboard. The function defined
     * for each property will be called each time the SB updates this sendable.
     */
    @Override
    public void initSendable(SendableBuilder builder)
    {
        builder.addStringProperty("Branch: ", () -> branch, null);
        builder.addStringProperty("Commit: ", () -> commit, null);
        builder.addStringProperty("Program: ", () -> programVersion, null);
        builder.addStringProperty("RobotLib: ", () -> robotlibVersion, null);
        builder.addStringProperty("WPILib: ", () -> WPILibVersion.Version, null);
        builder.addStringProperty("Time: ", () -> time, null);
        builder.addStringProperty("User: ", () -> user, null);
        builder.addDoubleProperty("UpdateCount", this::getUpdateCount, null);
    }
    
    /**
     * Returns program version.
     * @return The program version.
     */
    public String getProgramVersion()
    {
    	return programVersion;
    }
    /**
     * Returns the git branch this program was compiled from.
     * @return The branch name.
     */
    public String getBranch()
    {
    	return branch;
    }
    
    /**
     * Return the most recent git commit id of this programs
     * source code.
     * @return The commit id.
     */
    public String getCommit()
    {
    	return commit;
    }
    
    /**
     * Return the time this program was compiled
     * @return The compile time as a string.
     */
    public String getTime()
    {
    	return time;
    }
    
    /**
     * Return the name of the user who compiled this program.
     * @return User name.
     */
    public String getUser()
    {
    	return user;
    }
    
    /**
     * Used to track when and how often this sendable is updated 
     * by the SmartDashboard.
     * @return The current update count.
     */
    public int getUpdateCount()
    {
    	return ++updateCount;
    }
}

