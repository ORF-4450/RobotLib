
package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
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

public class MonitorDistanceMBX extends Thread implements Sendable
{
    RobotBase				robot;
    private int				port = 1;
    private AnalogInput 	ultra;
	private double			rangeInches;
	private double			rangeFeet;
	private double			delay = 1.0;	// Seconds.
	  
	/**
	 * Static reference to the internal MonitorDistanceMBX instance created by
	 * getInstance() calls on this class. Must call a getInstance() before using.
	 */ 
	public static MonitorDistanceMBX	INSTANCE;

    // Create single instance of this class and return that single instance to any callers.
    // This is the singleton class model. You don't use new, you use getInstance. After that
	// you can use the returned instance reference in a variable in your code or use the
	// INSTANCE variable above to access the members of this class.
      
	// note: Creating this class with the singleton style means there can only be one
	// instance of this class and so one distance sensor. This technically is incorrect
	// as you could have more than one sensor. In our world, we have only ever used one
	// distance sensor, but, that may not be the case in the future requiring this class
	// be converted back to multi-instance class constructors. (05.17.22)
	
    /**
     * Get a reference to global MonitorDistanceMBX Thread object. Defaults to
     * Analog port 1 for ultrasonic sensor.
     * @param robot RobotBase instance calling this function (use 'this').
     * @return Reference to global MonitorDistanceMBX object.
     */
    
    public static MonitorDistanceMBX getInstance(RobotBase robot) 
    {
    	Util.consoleLog();
      	
    	if (INSTANCE == null) INSTANCE = new MonitorDistanceMBX(robot);
          
    	return INSTANCE;
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
        	
        if (INSTANCE == null) INSTANCE = new MonitorDistanceMBX(robot, port);
            
        return INSTANCE;
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
        	
        if (INSTANCE == null) INSTANCE = new MonitorDistanceMBX(robot, ultraSonic);
            
        return INSTANCE;
    }
    

    // Private constructor forces use of getInstance().

    private MonitorDistanceMBX(RobotBase robot)
	{
		Util.consoleLog("port=%d", port);
        this.robot = robot;
        this.setName("MonitorDistanceMBX");

        ultra = new AnalogInput(port);
        
  	  	SendableRegistry.addLW(this, "MonitorDistanceMBX", ultra.getChannel());
  	  	SendableRegistry.setName(ultra, "MonitorDistanceAIO", ultra.getChannel());
    }

    private MonitorDistanceMBX(RobotBase robot, int port)
	{
		Util.consoleLog("port=%d", port);
        this.robot = robot;
        this.port = port;
        this.setName("MonitorDistanceMBX");
        
        ultra = new AnalogInput(port);
        
  	  	SendableRegistry.addLW(this, "MonitorDistanceMBX", ultra.getChannel());
  	  	SendableRegistry.setName(ultra, "MonitorDistanceAIO", ultra.getChannel());
	}

    private MonitorDistanceMBX(RobotBase robot, AnalogInput ultraSonic)
	{
		Util.consoleLog("port=%d", ultraSonic.getChannel());
        this.robot = robot;
        this.setName("MonitorDistanceMBX");
        
        ultra = ultraSonic;
        
  	  	SendableRegistry.addLW(this, "MonitorDistanceMBX", ultra.getChannel());
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
	
    @Override
    public void initSendable( SendableBuilder builder )
    {
    	builder.setSmartDashboardType("MonitorDistanceMBX");
    	builder.addBooleanProperty(".controllable", () -> false, null);
    	builder.addDoubleProperty("Range(in)", this::getRangeInches, null);
    }
}