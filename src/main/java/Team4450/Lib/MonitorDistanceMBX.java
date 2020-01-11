
package Team4450.Lib;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Timer;

/**
 * Task to monitor ultrasonic sensor and report distance to driver station.
 * Runs as a thread separate from Robot class. Runs until our
 * program is terminated from the RoboRio.
 * Displays distance value on DS.
 * Uses MaxBotix ultrasonic sensor.
 */

public class MonitorDistanceMBX extends Thread
{
    RobotBase				robot;
    private static 			MonitorDistanceMBX	monitorDistance;
    private int				port = 1;
    private AnalogInput 	ultra;
	private double			rangeInches;
	private double			rangeFeet;
	private double			delay = 1.0;	// Seconds.

    // Create single instance of this class and return that single instance to any callers.
    // This is the singleton class model. You don't use new, you use getInstance.
      
    /**
     * Get a reference to global MonitorDistanceMBX Thread object. Defaults to
     * Analog port 1 for ultrasonic sensor.
     * @param robot RobotBase instance calling this function (use 'this').
     * @return Reference to global MonitorDistanceMBX object.
     */
    
    public static MonitorDistanceMBX getInstance(RobotBase robot) 
    {
    	Util.consoleLog();
      	
    	if (monitorDistance == null) monitorDistance = new MonitorDistanceMBX(robot);
          
    	return monitorDistance;
    }
    
    /**
     * Get a reference to global MonitorDistanceMBX Thread object.
     * @param robot RobotBase instance calling this function (use 'this').
     * @param port Analog port number for ultasonic sensor.
     * @return Reference to global MonitorDistanceMBX object.
     */
      
    public static MonitorDistanceMBX getInstance(RobotBase robot, int port) 
    {
    	Util.consoleLog();
        	
        if (monitorDistance == null) monitorDistance = new MonitorDistanceMBX(robot, port);
            
        return monitorDistance;
    }
    
    /**
     * Get a reference to global MonitorDistanceMBX Thread object.
     * @param robot RobotBase instance calling this function (use 'this').
     * @param ultraSonic AnalogInput instance for ultra sonic sensor.
     * @return Reference to global MonitorDistanceMBX object.
     */
      
    public static MonitorDistanceMBX getInstance(RobotBase robot, AnalogInput ultraSonic) 
    {
    	Util.consoleLog();
        	
        if (monitorDistance == null) monitorDistance = new MonitorDistanceMBX(robot, ultraSonic);
            
        return monitorDistance;
    }

    private MonitorDistanceMBX(RobotBase robot)
	{
		Util.consoleLog("port=%d", port);
        this.robot = robot;
        this.setName("MonitorDistanceMBX");

        ultra = new AnalogInput(port);
    }

    private MonitorDistanceMBX(RobotBase robot, int port)
	{
		Util.consoleLog("port=%d", port);
        this.robot = robot;
        this.port = port;
        this.setName("MonitorDistanceMBX");
    
        ultra = new AnalogInput(port);
	}

    private MonitorDistanceMBX(RobotBase robot, AnalogInput ultraSonic)
	{
		Util.consoleLog("port=%d", ultraSonic.getChannel());
        this.robot = robot;
        this.setName("MonitorDistanceMBX");
    
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
			Util.consoleLog();

			while (true)
			{
				if (robot.isEnabled())
				{
					rangeInches = ultra.getVoltage() / .0098;
					rangeFeet = rangeInches / 12;

					if (rangeFeet > 20) rangeInches = rangeFeet = 0.0;
				}
				else

				Timer.delay(delay);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
}