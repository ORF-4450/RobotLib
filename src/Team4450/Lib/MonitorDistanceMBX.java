
package Team4450.Lib;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.SampleRobot;
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
    SampleRobot		robot;
    private static 	MonitorDistanceMBX	monitorDistance;
    private int		port = 1;
	private double	rangeInches;
	private double	rangeFeet;

    // Create single instance of this class and return that single instance to any callers.
    // This is the singleton class model. You don't use new, you use getInstance.
      
    /**
     * Get a reference to global MonitorDistanceMBX Thread object. Defaults to
     * Analog port 1 for ultrasonic sensor.
     * @param robot SampleRobot instance calling this function (use 'this').
     * @return Reference to global MonitorDistanceMBX object.
     */
    
    public static MonitorDistanceMBX getInstance(SampleRobot robot) 
    {
    	Util.consoleLog();
      	
    	if (monitorDistance == null) monitorDistance = new MonitorDistanceMBX(robot);
          
    	return monitorDistance;
    }
    
     /**
      * Get a reference to global MonitorDistanceMBX Thread object.
      * @param robot SampleRobot instance calling this function (use 'this').
      * @param port Analog port number for ultasonic sensor.
      * @return Reference to global MonitorDistanceMBX object.
      */
      
     public static MonitorDistanceMBX getInstance(SampleRobot robot, int port) 
     {
    	 Util.consoleLog();
        	
         if (monitorDistance == null) monitorDistance = new MonitorDistanceMBX(robot, port);
            
         return monitorDistance;
      }

    private MonitorDistanceMBX(SampleRobot robot)
	{
		Util.consoleLog();
        this.robot = robot;
        this.setName("MonitorDistanceMBX");
    }

    private MonitorDistanceMBX(SampleRobot robot, int port)
	{
		Util.consoleLog();
        this.robot = robot;
        this.port = port;
        this.setName("MonitorDistanceMBX");
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
        AnalogInput 	ultra = new AnalogInput(port);

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
					
					//SmartDashboard.putString("Range", String.format("%f", rangeFeet));
					//LCD.printLine(3, "range=%f, voltage=%f", rangeFeet, ultra.getVoltage());
				}
				else
				{
					//SmartDashboard.putString("Range", "0.0");
					//LCD.printLine(3, "range=%f", 0.0);
				}

				Timer.delay(1.0);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
}