
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;
import edu.wpi.first.wpilibj.SampleRobot;

/**
 * Task to monitor ultrasonic sensor and report distance to driver station.
 * Runs as a thread separate from Robot class. Runs until our
 * program is terminated from the RoboRio.
 * Displays distance value on DS.
 * Uses old style ultrasonic sensor.
 */

class MonitorDistance extends Thread
{
    SampleRobot 	robot;
    private static 	MonitorDistance	monitorDistance;

	private double	rangeInches;
	private double	rangeFeet;

    // Create single instance of this class and return that single instance to any callers.
    // This is the singleton class model. You don't use new, you use getInstance.
      
    /**
     * Get a reference to global MonitorDistance Thread object.
     * @param robot SampleRobot instance calling this function (use 'this').
     * @return Reference to global MonitorDistance object.
     */
    
    public static MonitorDistance getInstance(SampleRobot robot) 
    {
  	 Util.consoleLog();
      	
       if (monitorDistance == null) monitorDistance = new MonitorDistance(robot);
          
       return monitorDistance;
    }
    
	private MonitorDistance(SampleRobot robot)
	{
		Util.consoleLog();
        this.robot = robot;
        this.setName("MonitorDistance");
    }
    
    public double getRangeFeet()
    {
    	return rangeFeet;
    }
    
    public double getRangeInches()
    {
    	return rangeInches;
    }

    public void run()
    {
        Ultrasonic ultra = new Ultrasonic(5,7);

		try
		{
			Util.consoleLog();

			ultra.setAutomaticMode(true);

			while (true)
			{
				if (robot.isEnabled())
				{
					rangeInches = ultra.getRangeInches();
					rangeFeet = rangeInches / 12;
                    
					//Util.consoleLog("range=" + Util.format(rangeFeet));

					if (rangeFeet > 55) rangeFeet = 0.0;
					
					//SmartDashboard.putString("Range", String.format("%f", rangeFeet));
					LCD.printLine(3, "range=%f", rangeFeet);
				}
				else
				{
					//SmartDashboard.putString("Range", "0.0");
					LCD.printLine(3, "range=%f", 0.0);
				}

				Timer.delay(1.0);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
}