
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * Task to monitor ultrasonic sensor and report distance to driver station.
 * Runs as a thread separate from Robot class. Runs until our
 * program is terminated from the RoboRio.
 * Displays distance value on DS.
 * Uses old style ultrasonic sensor.
 */

public class MonitorDistance extends Thread implements Sendable
{
    RobotBase 			robot;
    private int			port;
    private Ultrasonic	ultra;
    private double		delay = 1.0;	// seconds.

	private double	rangeInches;
	private double	rangeFeet;
	  
	/**
	 * Static reference to the internal MonitorDistance instance created by
	 * getInstance() calls on this class. Must call a getInstance() before using.
	 */
	public static MonitorDistance	INSTANCE;

    // Create single instance of this class and return that single instance to any callers.
    // This is the singleton class model. You don't use new, you use getInstance.
    
    /**
     * Get a reference to global MonitorDistance Thread object.
     * @param robot RobotBase instance calling this function (use 'this').
  	 * Defaults to DIO port 0 for first wire. Port + 1 will be allocated
   	 * for the second wire.
     * @return Reference to global MonitorDistance object.
     */
      
    public static MonitorDistance getInstance(RobotBase robot) 
    {
    	 Util.consoleLog();
        	
         if (INSTANCE == null) INSTANCE = new MonitorDistance(robot, 0);
            
         return INSTANCE;
    }
      
    /**
     * Get a reference to global MonitorDistance Thread object.
     * @param robot RobotBase instance calling this function (use 'this').
	 * @param port DIO port for first wire. Port + 1 will be allocated
	 * for the second wire.
     * @return Reference to global MonitorDistance object.
     */
    
    public static MonitorDistance getInstance(RobotBase robot, int port) 
    {
    	Util.consoleLog();
      	
    	if (INSTANCE == null) INSTANCE = new MonitorDistance(robot, port);
          
    	return INSTANCE;
    }
    
    /**
     * Get a reference to global MonitorDistance Thread object.
     * @param robot RobotBase instance calling this function (use 'this').
     * @param ultraSonic Ultrasonic sensor instance.
     * @return Reference to global MonitorDistance object.
     */
  
    public static MonitorDistance getInstance(RobotBase robot, Ultrasonic ultraSonic) 
    {
    	Util.consoleLog();
    	
    	if (INSTANCE == null) INSTANCE = new MonitorDistance(robot, ultraSonic);
        
    	return INSTANCE;
    }
    
	private MonitorDistance(RobotBase robot, int port)
	{
		Util.consoleLog("ports=%d,%d", port, port + 1);
		
        this.robot = robot;
        this.port = port;
        this.setName("MonitorDistance");

        ultra = new Ultrasonic(port, port + 1);
        
  	  	SendableRegistry.addLW(this, "MonitorDistance", port);
    }
    
	private MonitorDistance(RobotBase robot, Ultrasonic ultraSonic)
	{
		Util.consoleLog();
		
        this.robot = robot;
        this.setName("MonitorDistance");
        
        ultra = ultraSonic;
        
  	  	SendableRegistry.addLW(this, "MonitorDistance", ultra.getEchoChannel());
    }
    
    /**
     * Set the delay between samples of the sensor.
     * @param delay Delay in seconds.
     */
    
    public void setDelay(double delay)
    {
    	this.delay = delay;
    }
    
    /**
     * Returns the current sample delay.
     * @return Delay in seconds.
     */
    
    public double getDelay()
    {
    	return delay;
    }
    
    /**
     * Return last measured range to surface.
     * @return Range in feet.
     */
    
    public double getRangeFeet()
    {
    	return rangeFeet;
    }
    
    /**
     * Return last measured range to surface.
     * @return Range in inches.
     */

    public double getRangeInches()
    {
    	return rangeInches;
    }

    public void run()
    {
		try
		{
			ultra.setAutomaticMode(true);

			while (true)
			{
				if (robot.isEnabled())
				{
					rangeInches = ultra.getRangeInches();
					rangeFeet = rangeInches / 12;

					if (rangeFeet > 55) rangeFeet = rangeInches = 0.0;
				}

				Timer.delay(delay);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
    @Override
    public void initSendable( SendableBuilder builder )
    {
    	builder.setSmartDashboardType("MonitorDistance");
    	builder.addBooleanProperty(".controllable", () -> false, null);
    	builder.addDoubleProperty("Range(in)", this::getRangeInches, null);
    }
}