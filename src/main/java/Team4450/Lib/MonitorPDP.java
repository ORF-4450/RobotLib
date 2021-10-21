
package Team4450.Lib;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Power Distribution Panel monitoring task.
 * Runs as a separate thread from the Robot class. Runs until robot
 * program is terminated from the RoboRio.
 */

public class MonitorPDP extends Thread
{
  private final double		  		LOW_BATTERY = 11, MAX_CURRENT = 180;
  private static MonitorPDP			monitorPDP;
  private PowerDistribution			pdp;
  private double					sampleInterval = 1.0;	// Seconds
  private boolean					alarmInProgress = false, lowBatteryAlarm = false, overloadAlarm = false;
  private boolean					ports[] = new boolean[16];
  
  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance.
    
  /**
   * Get a reference to global MonitorPDP Thread object.
   * @return Reference to global MonitorPDP object.
   */
      
  public static MonitorPDP getInstance() 
  {
	  Util.consoleLog();
    	
  	  if (monitorPDP == null) monitorPDP = new MonitorPDP();
        
  	  return monitorPDP;
  }
  
  /**
   * Get a reference to global MonitorPDP Thread object.
   * @param pdp PowerDistributionPanel instance.
   * @return Reference to global MonitorPDP object.
   */
        
  public static MonitorPDP getInstance(PowerDistribution pdp) 
  {
  	  Util.consoleLog();
      	
   	  if (monitorPDP == null) monitorPDP = new MonitorPDP(pdp);
          
   	  return monitorPDP;
  }

  // Private constructors means callers must use getInstance.
  
  private MonitorPDP()
  {
	  Util.consoleLog();
	  pdp = new PowerDistribution();
	  this.setName("MonitorPDP");
  }

  private MonitorPDP(PowerDistribution pdp)
  {
	  Util.consoleLog();
	  this.pdp = pdp;
	  this.setName("MonitorPDP");
  }
 
  /**
   * Set PDP sampling interval.
   * @param interval Sampling interval in seconds,
   */
  
  public void setSampleInterval(double interval)
  {
	  sampleInterval = interval;
  }
  
  /**
   * Returns the current sample interval.
   * @return Sample interval in seconds.
   */
  
  public double getSampleInterval()
  {
	  return sampleInterval;
  }

  /**
   * Enable PDP port to be monitored.
   * @param port Port number 0..15.
   * @param enabled True to monitor port.
   */
  
  public void enablePort(int port, boolean enabled)
  {
	  ports[port] = enabled;
  }
  
  /**
   * Is PDP alarm active?
   * @return True if alarm in progress, false if not.
   */
  
  public boolean isAlarmed()
  {
	  return alarmInProgress;
  }
  
  /**
   * Is alarm a low battery alarm?
   * @return True if low battery alarm in progress.
   */
  
  public boolean isLowBatteryAlarm()
  {
	  return lowBatteryAlarm;
  }
  
  /**
   * Is alarm an overload alarm?
   * @return True if overload alarm in progress.
   */
  
  public boolean isOverloadAlarm()
  {
	  return overloadAlarm;
  }
  
  /**
   * Start monitoring. Called by Thread.start().
   */
  public void run()
  {        
	  boolean alarmFlash = false, alarmInProgress = false, lowBatteryAlarm = false, overloadAlarm = false;
	  boolean alarmFlash2 = false;

	  try
	  {
		  Util.consoleLog();
        
          // Check battery voltage and brownout every second.
        
		  while (true)
          {
			  alarmInProgress = lowBatteryAlarm = overloadAlarm = false;
			  
			  // Check PDP input voltage.
			  
			  if (pdp.getVoltage() < LOW_BATTERY)
			  {
				  Util.consoleLog("battery voltage warning: %.1fv", pdp.getVoltage());
			  
				  alarmInProgress = true;
				  lowBatteryAlarm = true;
			  }
			  
			  // Check PDP total current flow.
			  
			  if (pdp.getTotalCurrent() > MAX_CURRENT)
			  {
				  Util.consoleLog("battery total current warning: %.1famps", pdp.getTotalCurrent());
			  
				  alarmInProgress = true;
				  overloadAlarm = true;
			  }
			  
			  // check the PDP output port current levels for enabled ports.
			  
			  for (int i = 0; i < 16; i++)
			  {
				  if (ports[i])
    				  if (((i < 4 && i > 11) && pdp.getCurrent(i) > 40) | ((i > 3 && i < 12) && pdp.getCurrent(i) > 30))
    					  Util.consoleLog("pdp port %d current warning: %.1famps", i,  pdp.getCurrent(i));
			  }
			  
			  // Check driver station brownout flag.
			  
			  if (RobotController.isBrownedOut())
			  {
				  Util.consoleLog("brownout warning: %.1fv", pdp.getVoltage());
			  
				  alarmInProgress = true;
				  overloadAlarm = true;
			  }
				  
			  // flash DS leds for alarms.
			  
			  if (alarmInProgress && lowBatteryAlarm)
			  {
				  if (alarmFlash)
					  alarmFlash = false;
				  else
					  alarmFlash = true;
        
				  SmartDashboard.putBoolean("Low Battery", alarmFlash);
        	  }
			  else
			  {
				  SmartDashboard.putBoolean("Low Battery", false);
        	  }
			  
    		  if (alarmInProgress && overloadAlarm)
    		  {
    			  if (alarmFlash2)
    				  alarmFlash2 = false;
    			  else
    				  alarmFlash2 = true;
        
    			  SmartDashboard.putBoolean("Overload", alarmFlash2);
        	  }
    		  else
    		  {
    			  SmartDashboard.putBoolean("Overload", false);
        	  }

			  Timer.delay(sampleInterval);
          }
	  }
	  catch (Throwable e) {Util.logException(e);}
  }
}