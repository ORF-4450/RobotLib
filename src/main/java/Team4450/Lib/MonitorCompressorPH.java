
package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;

/**
 * Compressor monitoring task. For REV Pneumatics Hub only.
 * Runs as a separate thread from our MyRobot class. Runs until our
 * program is terminated from the RoboRio. Displays compressor on/off
 * LED on DS. Can also monitor an air pressure sensor and report the
 * pressure to the DS. Assumes compressor is plugged into the first
 * PH, device id 1.
 */

public class MonitorCompressorPH extends Thread implements Sendable
{
  private final Compressor			compressor;
  private double					delay = 2.0, lowPressureThreshold = 0.0, correction = 0.0;
  private boolean					lowPressureAlarm = false, ledState = false, compressorState;
  
  /**
   * Static reference to the internal MonitorCompressorPH instance created by
   * getInstance() calls on this class. Must call a getInstance() before using.
   */
  public static MonitorCompressorPH	INSTANCE;

  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance. After that
  // you can use the returned instance reference in a variable in your code or use the
  // INSTANCE variable above to access the members of this class. Assumes robot will have
  // only one compressor.
  
  /**
   * Get a reference to global MonitorCompressorPH Thread object. Monitors compressor on/off
   * and sets DS LED named Compressor accordingly. Also monitors pressure on analog I/O port.
   * Pressure is displayed on DS gauge called AirPressure. Can also do an alarm LED called 
   * LowPressure if you set the low pressure threshold.
   * @param compressor Reference to a Compressor object of type REVPH.
   * @return Reference to global MonitorCompressorPH object.
   */
    
  public static MonitorCompressorPH getInstance(Compressor compressor) 
  {
  	 Util.consoleLog();
      	
     if (INSTANCE == null) INSTANCE = new MonitorCompressorPH(compressor);
          
     return INSTANCE;
  }

  private MonitorCompressorPH(Compressor compressor)
  {
	  Util.consoleLog();
	  
	  this.compressor = compressor;
	  
	  this.setName("MonitorCompressorPH");

	  SmartDashboard.putBoolean("LowPressure", false);
	  
	  SendableRegistry.addLW(this, "MonitorCompressorPH", 1);
  }
    
  /**
   * If monitoring pressure, return the current pressure from port 0.
   * @return Current pressure in PSI.
   */
  public double getPressure()
  {
	  Double pressure = compressor.getPressure();
	  
	  if (Double.isNaN(pressure)) pressure = 0.0;
	  
	  return pressure + correction;
  }
  
  /**
   * Return the pressure sensor current voltage on port 0.
   * @return Sensor voltage.
   */
  public double getVoltage()
  {
	  return compressor.getAnalogVoltage();
  }
  
  /**
   * Set the delay of the sampling loop. Longer delay works when only monitoring compressor on/off.
   * Shorter delay may be appropriate when monitoring pressure.
   * @param seconds Delay in second between samples. Minimum .5 second. Defaults to 2 seconds.
   */
  public void setDelay(double seconds)
  {
	  if (delay < 0.5) delay = 0.5;
	  
	  delay = seconds;
  }
  
  /**
   * Set correction value to be added to the pressure to make it
   * match the gauge.
   * @param psi Correction value in PSI.
   */
  public void setCorrection(double psi)
  {
	  correction = psi;
  }
  
  /**
   * Enable low pressure alarm LED on DS by setting pressure at which
   * alarm will trigger. Expects LED to be named LowPressure.
   * @param psi The low pressure in PSI.
   */
  public void SetLowPressureAlarm(double psi)
  {
	  if (psi < 0) psi = 0;
	  
	  lowPressureThreshold = psi;
  }
  
  /**
   * Start monitoring. Called by Thread.start().
   */
  public void run()
  {      
	boolean	saveState = false;
	double	pressure;
	
	try
	{
		Util.consoleLog();

		while (!isInterrupted())
		{
			compressorState = compressor.isEnabled();
			
			if (compressorState != saveState)
			{
				saveState = compressorState;
				SmartDashboard.putBoolean("Compressor", saveState);
				Util.consoleLog("compressor on=%b", saveState);
			}
			
			pressure = getPressure();			
			
			SmartDashboard.putNumber("AirPressure", (int) pressure);
		
			if (lowPressureThreshold > 0)
			{
				if (pressure <= lowPressureThreshold)
				{
					if (!lowPressureAlarm) DriverStation.reportError(String.format("low air pressure alarm: %dpsi", (int) pressure), false);

					lowPressureAlarm = true;
				}
				else
				{
					if (lowPressureAlarm) DriverStation.reportError("low air pressure alarm cleared", false);
					
					lowPressureAlarm = false;
				}
				
				if (lowPressureAlarm)
					ledState = !ledState;
				else
					ledState = false;

				SmartDashboard.putBoolean("LowPressure", ledState);
			}
			
			Timer.delay(delay);
		}
	}
	catch (Throwable e) {Util.logException(e);}
  }
	
  @Override
  public void initSendable( SendableBuilder builder )
  {
	builder.setSmartDashboardType("MonitorCompressorPH");
  	builder.addBooleanProperty(".controllable", () -> false, null);
    builder.addDoubleProperty("PSI", this::getPressure, null);
    builder.addBooleanProperty("On", () -> compressorState, null);
    builder.addBooleanProperty("Alarm", () -> lowPressureAlarm, null);
  }
}
