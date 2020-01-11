
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.RobotBase;

/**
 * Task to monitor ultrasonic sensor and report distance to driver station.
 * Runs as a thread separate from Robot class. Runs until our
 * program is terminated from the RoboRio.
 * Displays distance value on DS.
 * Uses old style ultrasonic sensor.
 */

public class MonitorDistance extends Thread
{
    RobotBase 		robot;
    private int			port;
    private Ultrasonic	ultra;
    private double		delay = 1.0;	// seconds.
    private static 		MonitorDistance	monitorDistance;

	private double	rangeInches;
	private double	rangeFeet;

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
        	
         if (monitorDistance == null) monitorDistance = new MonitorDistance(robot, 0);
            
         return monitorDistance;
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
      	
    	if (monitorDistance == null) monitorDistance = new MonitorDistance(robot, port);
          
    	return monitorDistance;
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
    	
    	if (monitorDistance == null) monitorDistance = new MonitorDistance(robot, ultraSonic);
        
    	return monitorDistance;
    }
    
	private MonitorDistance(RobotBase robot, int port)
	{
		Util.consoleLog("ports=%d,%d", port, port + 1);
		
        this.robot = robot;
        this.port = port;
        this.setName("MonitorDistance");

        ultra = new Ultrasonic(port, port + 1);
    }
    
	private MonitorDistance(RobotBase robot, Ultrasonic ultraSonic)
	{
		Util.consoleLog();
		
        this.robot = robot;
        this.setName("MonitorDistance");

        ultra = ultraSonic;
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
}