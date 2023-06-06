
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
 * CTRE/REV Power Distribution Panel/Hub monitoring task. Monitors battery
 * voltage, current draw (overload) and brownout. Logs warnings to
 * console and shuffleboard LEDs. Runs as a separate thread from the 
 * Robot class. Runs until robot program is terminated.
 */

public class MonitorPDP extends Thread implements Sendable
{
  // Theoretical max current from good FRC battery is 250 amps, brown out power
  // cuts start at 7 volts.
  private final double		  		LOW_BATTERY = 8, MAX_CURRENT = 240;
  private PowerDistribution			pdp;
  private double					sampleInterval = 1.0;	// Seconds
  private boolean					alarmInProgress = false, lowBatteryAlarm = false, overloadAlarm = false;
  private boolean					ports[] = new boolean[24];
  private int						numPorts;
  
  /**
   * Static reference to the internal MonitorPDP instance created by
   * getInstance() calls on this class. Must call a getInstance() before using.
   */
  public static MonitorPDP	 		INSTANCE;
  
  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance. After that
  // you can use the returned instance reference in a variable in your code or use the
  // INSTANCE variable above to access the members of this class. Assumes robot will have
  // only one PDP.
    
  /**
   * Get a reference to global MonitorPDP Thread object.
   * @return Reference to global MonitorPDP object.
   */
      
  public static MonitorPDP getInstance() 
  {
	  Util.consoleLog();
    	
  	  if (INSTANCE == null) INSTANCE = new MonitorPDP();
        
  	  return INSTANCE;
  }
  
  /**
   * Get a reference to global MonitorPDP Thread object.
   * @param pdp PowerDistributionPanel instance.
   * @return Reference to global MonitorPDP object.
   */
        
  public static MonitorPDP getInstance(PowerDistribution pdp) 
  {
  	  Util.consoleLog();
      	
   	  if (INSTANCE == null) INSTANCE = new MonitorPDP(pdp);
          
   	  return INSTANCE;
  }

  // Private constructors means callers must use getInstance.
  
  private MonitorPDP()
  {
	  Util.consoleLog();
	  pdp = new PowerDistribution();
	  this.setName("MonitorPDP");
      
	  SendableRegistry.addLW(this, "MonitorPDP");
  }

  private MonitorPDP(PowerDistribution pdp)
  {
	  Util.consoleLog();
	  this.pdp = pdp;
	  this.setName("MonitorPDP");
      
	  SendableRegistry.addLW(this, "MonitorPDP");
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
   * Enable PDP/PDH port to be monitored. 0-15 for PDP, 0-23 for PDH.
   * @param port Port number 0..23.
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
   * Start monitoring. Called by Thread.start().
   */
  public void run()
  {        
	  boolean alarmFlash = false, alarmInProgress = false;
	  boolean alarmFlash2 = false;

	  try
	  {
		  Util.consoleLog();
		  
		  numPorts = pdp.getNumChannels();
        
          // Check battery voltage, max current and brownout every second. If any problem
		  // detected start flashing the appropriate LED on SB. Flashing does not reset.
        
		  while (!isInterrupted())
          {
			  //alarmInProgress = false;
			  
			  // Check PDP input voltage.
			  
			  if (pdp.getVoltage() < LOW_BATTERY)
			  {
				  DriverStation.reportError(String.format("battery voltage warning: %.2fv", pdp.getVoltage()), false);
			  
				  alarmInProgress = true;
				  lowBatteryAlarm = true;
			  } //else
				  //lowBatteryAlarm = false;
			  
			  // Check PDP total current flow.
			  
			  if (pdp.getTotalCurrent() > MAX_CURRENT)
			  {
				  DriverStation.reportError(String.format("battery total current warning: %.1f amps", pdp.getTotalCurrent()), false);
			  
				  alarmInProgress = true;
				  overloadAlarm = true;
			  } //else
				  //overloadAlarm = false;
			  
			  // check the PDP output port current levels for enabled ports.
			  
			  if (numPorts == 16)
				  for (int i = 0; i < 16; i++)
				  {
					  if (ports[i])
	    				  if (((i < 4 && i > 11) && pdp.getCurrent(i) > 40) | ((i > 3 && i < 12) && pdp.getCurrent(i) > 30))
	    					  DriverStation.reportError(String.format("pdp port %d current warning: %.1f amps", i,  pdp.getCurrent(i)), false);
				  }
			  else
				  for (int i = 0; i < 24; i++)
				  {
					  if (ports[i])
	    				  if (pdp.getCurrent(i) > 40)
	    					  DriverStation.reportError(String.format("pdp port %d current warning: %.1f amps", i,  pdp.getCurrent(i)), false);
				  }	  
			  
			  // Check driver station brownout flag.
			  
			  if (RobotController.isBrownedOut())
			  {
				  DriverStation.reportError(String.format("brownout warning: %.1fv", pdp.getVoltage()), false);
			  
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
	  builder.setSmartDashboardType("MonitorPDP");
  	  builder.addBooleanProperty(".controllable", () -> false, null);
  	  builder.addDoubleProperty("Voltage", () -> pdp.getVoltage(), null);
  	  builder.addDoubleProperty("TotalCurrent", () -> pdp.getTotalCurrent(), null);
  	  builder.addBooleanProperty("LowBatteryAlarm", () -> lowBatteryAlarm, null);
  	  builder.addBooleanProperty("BrownOutAlarm", () -> overloadAlarm, null);
  }
}