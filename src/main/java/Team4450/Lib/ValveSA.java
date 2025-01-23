
package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * Wrapper class for Single Action pneumatic valve.
 * Single action opens against a spring. When you call open power is applied and
 * opens the valve against the spring and remains on. When close is called, power
 * is turned off and the spring closes the valve.
 */

public class ValveSA implements Sendable
{
	private final Solenoid	valveOpenSide;
	private boolean			valveOpen = false;
	private int				port, canId;

	/**
	 * @param port DIO port wired to valve. Assumes Control Module CAN Id 0.
	 * @param moduleType Pneumatics control module type.
	 */
	public ValveSA(int port, PneumaticsModuleType moduleType)
	{
	  	Util.consoleLog("canid=%d, port=%d", canId, port, moduleType);

	  	this.port = port;
	  	
		valveOpenSide = new Solenoid(moduleType, port);
	}

	/**
	 * @param canId Control Module CAN Id number.
	 * @param port DIO port wired to valve.
	 * @param moduleType Pneumatics control module type.
	 */
	public ValveSA(int canId, int port, PneumaticsModuleType moduleType)
	{
	  	Util.consoleLog("canid=%d, port=%d", canId, port);

	  	this.port = port;
	  	this.canId = canId;
	  	
		valveOpenSide = new Solenoid(canId, moduleType, port);
	}

	/**
	 * Release resources.
	 */
	public void dispose()
	{
		Util.consoleLog();
		
		Close();
		
		valveOpenSide.close();
	}

	/**
	 * Open the valve.
	 */
	public void Open()
	{
	  	Util.consoleLog("pcm=%d, port=%d", canId, port);
    
		valveOpenSide.set(true);
		
		valveOpen = true;
	}

	/**
	 * Close the valve.
	 */
	public void Close()
	{
	  	Util.consoleLog("pcm=%d, port=%d", canId, port);
    
		valveOpenSide.set(false);
		
		valveOpen = false;
	}
	
	/**
	 * Indicates if the valve is open, that is pressure is applied to the A
	 * side of the valve (single action only has the A side). SA cylinders
	 * typically have a spring that pulls in the rod so the no pressure
	 * condition is considered closed. Applying air to the one port connected
	 * to the A side of the valve extends the cylinder and is considered open.
	 * @return True if valve is open (pressure on A side).
	 */
	public boolean isOpen()
	{
		return valveOpen;
	}
	
	@Override
	public void initSendable( SendableBuilder builder )
	{
		builder.setSmartDashboardType("ValveSA");
    	builder.addBooleanProperty(".controllable", () -> false, null);
	    builder.addBooleanProperty("Open", this::isOpen, null);
	}
}