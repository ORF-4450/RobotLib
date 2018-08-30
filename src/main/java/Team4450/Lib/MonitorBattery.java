
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.hal.PowerJNI;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Battery voltage monitoring task.
 * Runs as a separate thread from the Robot class. Runs until our
 * program is terminated from the RoboRio.
 * Displays warning LED on DS on low voltage.
 */

public class MonitorBattery extends Thread
{
  private final double		  	LOW_BATTERY = 11.7;
  private static MonitorBattery	monitorBattery;

  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance.
    
  /**
   * Get a reference to global MonitorBattery Thread object.
   * @return Reference to global MonitorBattery object.
   */
      
  public static MonitorBattery getInstance() 
  {
	  Util.consoleLog();
    	
  	  if (monitorBattery == null) monitorBattery = new MonitorBattery();
        
  	  return monitorBattery;
  }

  // Private constructor means callers must use getInstance.

  private MonitorBattery()
  {
	  Util.consoleLog();
	  this.setName("MonitorBattery");
  }
    
  /**
   * Start monitoring. Called by Thread.start().
   */
  public void run()
  {        
	  boolean alarmFlash = false;

	  try
	  {
		  Util.consoleLog();
        
          // Check battery voltage every 10 seconds. If voltage below threshold
          // shift to one second interval and flash dashboard led. Voltage can
          // sag below threshold under load and then come back up so this code 
          // will turn off the led warning if voltage goes back above threshold.
        
		  while (true)
          {
			  if (PowerJNI.getVinVoltage() < LOW_BATTERY) //TODO: Replace with RobotController.getBatteryVoltage() See: wpilibsuite/allwpilib#870
			  {
				  if (alarmFlash)
				  {
					  alarmFlash = false;
				  }
				  else
				  {
					  alarmFlash = true;
				  }
        
				  SmartDashboard.putBoolean("Low Battery", alarmFlash);
        
				  Timer.delay(1.0);
			  }
			  else
			  {
				  SmartDashboard.putBoolean("Low Battery", false);
        
				  Timer.delay(10.0);
			  }
          }
	  }
	  catch (Throwable e)	{Util.logException(e);}
  }
}