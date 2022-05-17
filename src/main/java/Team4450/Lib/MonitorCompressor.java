
package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;

/**
 * Compressor monitoring task.
 * Runs as a separate thread from our MyRobot class. Runs until our
 * program is terminated from the RoboRio. Displays compressor on/off
 * LED on DS. Can also monitor an air pressure sensor and report the
 * pressure to the DS. Assumes compressor is plugged into the first
 * PCM, device id 0.
 * Note: for 2022 only CTRE PCM is supported at this time. REV Pneumatic
 * Hub will be added later.
 */

public class MonitorCompressor extends Thread implements Sendable
{
  private final Compressor			compressor = new Compressor(0, PneumaticsModuleType.CTREPCM);
  private AnalogInput				pressureSensor;
  private double					delay = 2.0, lowPressureThreshold = 0.0, correction = 0.0;
  private boolean					lowPressureAlarm = false, ledState = false, compressorState;
  
  /**
   * Static reference to the internal MonitorCompressor instance created by
   * getInstance() calls on this class. Must call a getInstance() before using.
   */
  public static MonitorCompressor	INSTANCE;

  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance.
    
  /**
   * Get a reference to global MonitorCompressor Thread object. Only monitors compressor on/off
   * and sets DS LED named Compressor accordingly. Assumes sensor plugged into analog port 0.
   * @return Reference to global MonitorCompressor object.
   */
  
  public static MonitorCompressor getInstance() 
  {
	 Util.consoleLog();
    	
     if (INSTANCE == null) INSTANCE = new MonitorCompressor(0);
        
     return INSTANCE;
  }
  
  /**
   * Get a reference to global MonitorCompressor Thread object. Monitors compressor on/off
   * and sets DS LED named Compressor accordingly. Also monitors pressure on analog I/O port.
   * Pressure is displayed on DS gauge called AirPressure. Can also do an alarm LED called 
   * LowPressure if you set the low pressure threshold.
   * @param pressureSensorPort Analog input port number pressure sensor is plugged into.
   * @return Reference to global MonitorCompressor object.
   */
    
  public static MonitorCompressor getInstance(int pressureSensorPort) 
  {
  	 Util.consoleLog();
      	
     if (INSTANCE == null) INSTANCE = new MonitorCompressor(pressureSensorPort);

     return INSTANCE;
  }
  
  /**
   * Get a reference to global MonitorCompressor Thread object. Monitors compressor on/off
   * and sets DS LED named Compressor accordingly. Also monitors pressure on analog I/O port.
   * Pressure is displayed on DS gauge called AirPressure. Can also do an alarm LED called 
   * LowPressure if you set the low pressure threshold.
   * @param pressureSensor AnalogInput instance for port pressure sensor is plugged into.
   * @return Reference to global MonitorCompressor object.
   */
    
  public static MonitorCompressor getInstance(AnalogInput pressureSensor) 
  {
  	 Util.consoleLog();
      	
     if (INSTANCE == null) INSTANCE = new MonitorCompressor(pressureSensor);
          
     return INSTANCE;
  }

  private MonitorCompressor(int pressureSensorPort)
  {
	  Util.consoleLog("port=%d", pressureSensorPort);
	  
	  this.setName("MonitorCompressor");

	  SmartDashboard.putBoolean("LowPressure", false);
	  
	  if (pressureSensorPort > -1) pressureSensor = new AnalogInput(pressureSensorPort);
	  
	  SendableRegistry.addLW(this, "MonitorCompressor", pressureSensorPort);
  }

  private MonitorCompressor(AnalogInput pressureSensor)
  {
	  Util.consoleLog("port=%d", pressureSensor.getChannel());
	  
	  this.setName("MonitorCompressor");

	  SmartDashboard.putBoolean("LowPressure", false);
	  
	  this.pressureSensor = pressureSensor;	  
      
	  SendableRegistry.addLW(this, "MonitorCompressor", pressureSensor.getChannel());
  }
    
  /**
   * If monitoring pressure, return the current pressure.
   * @return Current pressure in PSI.
   */
  public int getPressure()
  {
	  if (pressureSensor != null) return (int) convertV2PSI(pressureSensor.getVoltage());
	  
	  return 0;
  }
  
  /**
   * Return the pressure sensor current voltage.
   * @return Sensor voltage.
   */
  public double getVoltage()
  {
	  if (pressureSensor != null) return pressureSensor.getVoltage();
    
	  return 0;
  }
  
  /**
   * Convert the pressure sensor voltage to PSI.
   * @param voltage Input voltage from sensor.
   * @return Pressure in PSI.
   */
  public double convertV2PSI(double voltage)
  {
	  return voltage * 42.9 + correction;
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

		while (true)
		{
			compressorState = compressor.enabled();
			
			if (compressorState != saveState)
			{
				saveState = compressorState;
				SmartDashboard.putBoolean("Compressor", saveState);
				Util.consoleLog("compressor on=%b", saveState);
			}
			
			if (pressureSensor != null) 
			{
				pressure = convertV2PSI(pressureSensor.getVoltage());
				
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
			}
			
			Timer.delay(delay);
		}
	}
	catch (Throwable e) {Util.logException(e);}
  }
	
  @Override
  public void initSendable( SendableBuilder builder )
  {
	builder.setSmartDashboardType("MonitorCompressor");
  	builder.addBooleanProperty(".controllable", () -> false, null);
    builder.addDoubleProperty("PSI", this::getPressure, null);
    builder.addBooleanProperty("On", () -> compressorState, null);
    builder.addBooleanProperty("Alarm", () -> lowPressureAlarm, null);
  }
}
