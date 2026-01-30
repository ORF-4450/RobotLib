package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * RoboRio monitoring task. Monitors battery voltage and brownout. Logs warnings to
 * console and shuffleboard LEDs. Runs as a separate thread from the 
 * Robot class. Runs until robot program is terminated.
 */

public class MonitorPower extends Thread implements Sendable
{
  // Theoretical max current from good FRC battery is 250 amps, brown out power
  // cuts start at 7 volts.
  private double			  		lowVoltage = 9, maxCurrent = 240;
  private double					sampleInterval = 1.0;	// Seconds
  private boolean					alarmInProgress = false, lowBatteryAlarm = false, overloadAlarm = false;
  
  /**
   * Static reference to the internal MonitorPower instance created by
   * getInstance() calls on this class. Must call a getInstance() before using.
   */
  public static MonitorPower	 		INSTANCE;
  
  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance. After that
  // you can use the returned instance reference in a variable in your code or use the
  // INSTANCE variable above to access the members of this class.
    
  /**
   * Get a reference to global MonitorPower Thread object.
   * @return Reference to global MonitorPower object.
   */
  public static MonitorPower getInstance() 
  {
	  Util.consoleLog();
    	
  	  if (INSTANCE == null) INSTANCE = new MonitorPower();
        
  	  return INSTANCE;
  }
  
  // Private constructors means callers must use getInstance.
  
  private MonitorPower()
  {
	  Util.consoleLog();
	  
	  this.setName("MonitorPower");
      
	  SendableRegistry.addLW(this, "MonitorPower");
  }
  
  /**
   * Is alarm active?
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
   * Reset the alarms. Typically called in auto init and telop init or
   * RobotContainer.resetFaults.
   */
  public void reset()
  {
	  alarmInProgress = false;
	  lowBatteryAlarm = false;
	  overloadAlarm = false;
  }
  
  /**
   * Set RoboRio power sampling interval. Also controls the alarm flash time.
   * @param interval Sampling interval in seconds, defaults to 1.
   * @return Itself
   */
  public MonitorPower withSampleInterval(double interval)
  {
	  sampleInterval = interval;
	  
	  return this;
  }

  /**
   * Sets voltage at and below that triggers low battery alarm.
   * @param voltage Voltage to trigger alarm. Defaults to 9.
   * @return Itself
   */
  public MonitorPower withLowVoltage(double voltage)
  {
	  lowVoltage = voltage;
	  
	  return this;
  }
  
  /**
   * Start monitoring thread. Called by Thread.start().
   */
  public void run()
  {        
	  boolean alarmFlash = false, alarmInProgress = false;
	  boolean alarmFlash2 = false;

	  try
	  {
		  Util.consoleLog();
        
          // Check battery voltage and brownout every second. If any problem
		  // detected start flashing the appropriate LED on SB. Flashing does not reset.
        
		  while (!isInterrupted())
          {		  
			  // Check RoboRio input voltage.
			  
			  double voltage = RobotController.getBatteryVoltage();
			  
			  if (voltage <= lowVoltage)
			  {
				  DriverStation.reportError(String.format("battery voltage warning: %.2fv", voltage), false);
			  
				  alarmInProgress = true;
				  lowBatteryAlarm = true;
			  } //else
				  //lowBatteryAlarm = false;
			  			  			  
			  // Check driver station brownout flag.
			  
			  if (RobotController.isBrownedOut())
			  {
				  DriverStation.reportError(String.format("brownout warning: %.1fv", voltage), false);
			  
				  alarmInProgress = true;
				  overloadAlarm = true;
			  } //else
				  //overloadAlarm = false;
				  
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
  
	
  @Override
  public void initSendable( SendableBuilder builder )
  {
	  builder.setSmartDashboardType("MonitorPower");
  	  builder.addBooleanProperty(".controllable", () -> false, null);
  	  builder.addDoubleProperty("Voltage", () -> RobotController.getBatteryVoltage(), null);
  	  //builder.addDoubleProperty("TotalCurrent", () -> pdp.getTotalCurrent(), null);
  	  builder.addBooleanProperty("LowBatteryAlarm", () -> lowBatteryAlarm, null);
  	  builder.addBooleanProperty("BrownOutAlarm", () -> overloadAlarm, null);
  }
}
