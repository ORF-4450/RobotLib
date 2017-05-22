
package Team4450.Lib;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Compressor;

/**
 * Compressor monitoring task.
 * Runs as a separate thread from our MyRobot class. Runs until our
 * program is terminated from the RoboRio. Displays compressor on/off
 * LED on DS.
 */

public class MonitorCompressor extends Thread
{
  private final Compressor			compressor = new Compressor(0);
  private static MonitorCompressor	monitorCompressor;
  private static AnalogInput		pressureSensor;
  public double						delay = 2.0;

  // Create single instance of this class and return that single instance to any callers.
  // This is the singleton class model. You don't use new, you use getInstance.
    
  /**
   * Get a reference to global MonitorCompressor Thread object.
   * @return Reference to global MonitorCompressor object.
   */
  
  public static MonitorCompressor getInstance() 
  {
	 Util.consoleLog();
    	
     if (monitorCompressor == null) monitorCompressor = new MonitorCompressor(-1);
        
     return monitorCompressor;
  }
  
  /**
   * Get a reference to global MonitorCompressor Thread object. Monitor pressure
   * on analog I/O port.
   * @pararm pressureSensorPort Analog input port sensor is plugged into.
   * @return Reference to global MonitorCompressor object.
   */
    
  public static MonitorCompressor getInstance(int pressureSensorPort) 
  {
  	 Util.consoleLog();
      	
     if (monitorCompressor == null) monitorCompressor = new MonitorCompressor(pressureSensorPort);
          
     return monitorCompressor;
  }

  private MonitorCompressor(int pressureSensorPort)
  {
	  Util.consoleLog("port=%d", pressureSensorPort);
	  this.setName("MonitorCompressor");
	  
	  if (pressureSensorPort > -1) pressureSensor = new AnalogInput(pressureSensorPort);
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
  
  public double getVoltate()
  {
	  if (pressureSensor != null) return pressureSensor.getVoltage();
    
	  return 0;
  }
  
  public double convertV2PSI(double voltage)
  {
	  return voltage * 37.5;
  }
  
  /**
   * Start monitoring. Called by Thread.start().
   */
  public void run()
  {      
	boolean	saveState = false, compressorState;
	
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
			
			if (pressureSensor != null) SmartDashboard.putNumber("AirPressure", (int) convertV2PSI(pressureSensor.getVoltage()));
			
			Timer.delay(delay);
		}
	}
	catch (Throwable e) {Util.logException(e);}
  }
}
