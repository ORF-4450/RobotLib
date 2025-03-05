
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * Wrapper class for Double Action pneumatic valve.
 * Double action is a sliding valve. Calling open applies power to the open side momentarily causing
 * the valve to move to the open position. Power is then turned off and the valve
 * stays where it is (open). Calling close applies power to the close side momentarily causing the
 * valve to move to the closed position. Power is then turned off and the valve
 * stays where it is (closed). Note that the actual movement of the valve which lasts for whatever
 * the slide time is set to is done in a separate thread so as not to delay the calling thread.
 * 
 * Open and Close are arbitrary definitions, they are actually defined by the physical robot
 * valve piping/wiring and what you want the cylinder to do. Typically you would pipe the "open" side
 * of the valve to extend a cylinder and close to retract. For DA valves, our convention is to wire
 * the A side to the first port and pipe to open or extend the cylinder and B side to close or retract.
 * This means we pipe B side to the "at rest" or not "active" position of the cylinder. A side is piped
 * to the "active" or "doing the desired action" position of the cylinder. This based on the SMC manifold
 * and valves which have A and B sides. So for a valve you would wire the A side of the valve to the port
 * number on the constructor and the B side to that port + 1. Hoses go into A and B air ports to the ends
 * of the target cylinder corredsponding to at rest and active.
 */

public class ValveDA implements Sendable
{
	private final Solenoid	valveOpenSide, valveCloseSide;
	private boolean			valveOpen = false;
	private int				port, canId = 0;
	private String			name = "ValveDA";

	/**
	 * Sets the time to apply power to make sure valve slide moves correctly.
	 * The movement takes time so power has to be applied until slide has
	 * moved from one side to the other. In seconds, defaults to .02 sec.
	 * This default determined using Test mode.ve
	 */
	public double           solenoidSlideTime = .02;

	/**
	 * Create instance of ValveDA class. Assumes CAN Id 0 for CTRE module, Id 1 for REV module.
	 * @param port Control Module port wired to open/A side of valve. Close/B side is wired to module next port.
	 * @param moduleType Pneumatic control module type.
	 */

	public ValveDA(int port, PneumaticsModuleType moduleType)
	{
		if (moduleType == PneumaticsModuleType.REVPH) canId = 1;
		
	  	Util.consoleLog("canid=%d, port=%d", canId, port);

		valveOpenSide = new Solenoid(moduleType, port);
		valveCloseSide = new Solenoid(moduleType, port + 1);
    
		this.port = port;
        
		name = String.format("%s[%d-%d]", name, canId, port);
		
		SendableRegistry.addLW(this, "ValveDA", name);
	}

	/**
	 * Create instance of ValveDA class.
	 * @param canId Control Module CAN Id number, 0 for first CTRE module, 1 for first REV module.
	 * @param port Control module port wired to open/A side of valve. Close/B side is wired to module next port.
	 * @param moduleType Pneumatic control module type.
	 */

	public ValveDA(int canId, int port, PneumaticsModuleType moduleType)
	{
	  	Util.consoleLog("canid=%d, port=%d", canId, port);

		valveOpenSide = new Solenoid(canId, moduleType, port);
		valveCloseSide = new Solenoid(canId, moduleType, port + 1);
    
		this.port = port;
		this.canId = canId;
        
		name = String.format("%s[%d-%d]", name, canId, port);
        
		SendableRegistry.addLW(this, "ValveDA", name);
	}
	
	/**
	 * Sets the name used on the LiveWindow display for an instance of this class.
	 * @param name The name to display.
	 */
	public void setName(String name)
	{
        
		this.name = String.format("%s[%d-%d]", name, canId, port);
        
		SendableRegistry.setName(this, this.name);
	}

	/**
	 * Release resources.
	 */
	public void dispose()
	{
		Util.consoleLog();
		
		valveOpenSide.close();
		valveCloseSide.close();
	}

	/**
	 * Open the valve (pressurize port. This is A side).
	 * This function delays calling thread for the valve
	 * slide time (default 20ms).
	 */
	public void Open()
	{
	  	Util.consoleLog("canid=%d, port=%d", canId, port);
    
		valveCloseSide.set(false);
    
//		valveOpenSide.set(true);
//		Timer.delay(solenoidSlideTime);
//		valveOpenSide.set(false);
		
		new Thread(() -> {
			try {
				valveOpenSide.set(true);
				Timer.delay(solenoidSlideTime);
				valveOpenSide.set(false);
				valveOpen = true;
			} catch (Exception e) { }
		  }).start();
	}
	
	/**
	 * Pressurize the A side of the valve.
	 */
	public void SetA()
	{
		Util.consoleLog();
		
		Open();
	}

	/**
	 * Close the valve (pressurize port+1. This is B side).
	 */
	public void Close()
	{
	  	Util.consoleLog("canid=%d, port=%d", canId, port);
    
		valveOpenSide.set(false);
    
//		valveCloseSide.set(true);
//		Timer.delay(solenoidSlideTime);
//		valveCloseSide.set(false);
		
		new Thread(() -> {
			try {
				valveCloseSide.set(true);
				Timer.delay(solenoidSlideTime);
				valveCloseSide.set(false);
				valveOpen = false;
			} catch (Exception e) { }
		  }).start();		
	}
	
	/**
	 * Pressurize the B side of the valve.
	 */
	public void SetB()
	{
		Util.consoleLog();
		
		Close();
	}
	
	/**
	 * Returns open state of valve. Open is air on the A side of the
	 * valve, which is open based on our convention. Note, this value
	 * is not reliable until your first call to open/SetA or close/SetB to set
	 * the initial physical state of the valve.
	 * @return True if valve open (pressure on A side), false if closed (pressure on B side).
	 */
	public boolean isOpen()
	{
		return valveOpen;
	}
	
	/**
	 * Indicates if pressure is applied to the A side of the valve. Note, this value
	 * is not reliable until your first call to open/SetA or close/SetB to set
	 * the initial physical state of the valve.
	 * @return True of pressure on A side, false if pressure on B side
	 */
	public boolean isA()
	{
		return valveOpen;
	}
	
	/**
	 * Sets the time to apply power to make sure valve slide moves correctly.
	 * The movement takes time so power has to be applied until slide has
	 * moved from one side to the other. In seconds, defaults to .02 sec.
	 * @param slideTime Time power is applied in seconds.
	 */	
	public void setSlideTime(double slideTime)
	{
		solenoidSlideTime = slideTime;
	}
	
	@Override
	public void initSendable( SendableBuilder builder )
	{
		builder.setSmartDashboardType("ValveDA");
    	builder.addBooleanProperty(".controllable", () -> false, null);
	    builder.addBooleanProperty("Open(A)", this::isOpen, null);
    	builder.addDoubleProperty("SlideTime", () -> solenoidSlideTime, this::setSlideTime);
	}
} 