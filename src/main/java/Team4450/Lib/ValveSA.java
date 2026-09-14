
package Team4450.Lib;

//import edu.wpi.first.util.sendable.Sendable;
//import edu.wpi.first.util.sendable.SendableBuilder;
//import edu.wpi.first.util.sendable.SendableRegistry;
import org.wpilib.hardware.pneumatic.PneumaticsModuleType;
import org.wpilib.hardware.pneumatic.Solenoid;
import org.wpilib.hardware.bus.CANPort;

/**
 * Wrapper class for Single Action pneumatic valve.
 * Single action opens against a spring. When you call open power is applied and
 * opens the valve against the spring and remains on. When close is called, power
 * is turned off and the spring closes the valve.
 */

public class ValveSA //implements Sendable
{
	private final Solenoid	valveOpenSide;
	private boolean			valveOpen = false;
	private int				port, canPort;
	private String			name = "ValveSA";

	/**
	 * Create instance of ValveSA class.
	 * @param canPort Can bus pneumatic module is connected to. S0-S4.
	 * @param port Control module port wired to valve. 
	 * @param moduleType Pneumatic control module type.
	 */
	public ValveSA(CANPort canPort, int port, PneumaticsModuleType moduleType)
	{
		//if (moduleType == PneumaticsModuleType.REVPH) canId = 1;
		
	  	Util.consoleLog("canid=%d, port=%d", canPort.value, port, moduleType);

	  	this.port = port;
	  	this.canPort = canPort.value;
	  	
		valveOpenSide = new Solenoid(canPort, moduleType, port);
        
		name = String.format("%s[%d-%d]", name, canPort.value, port);
        
		//SendableRegistry.addLW(this, "ValveSA", name);
	}
	
	/**
	 * Sets the name used on the LiveWindow display for an instance of this class.
	 * @param name The name to display.
	 */
	public void setName(String name)
	{
        
		this.name = String.format("%s[%d-%d]", name, canPort, port);
        
		//SendableRegistry.setName(this, this.name);
		
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
	  	Util.consoleLog("pcm=%d, port=%d", canPort, port);
    
		valveOpenSide.set(true);
		
		valveOpen = true;
	}

	/**
	 * Close the valve.
	 */
	public void Close()
	{
	  	Util.consoleLog("pcm=%d, port=%d", canPort, port);
    
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
	
//	@Override
//	public void initSendable( SendableBuilder builder )
//	{
//		builder.setSmartDashboardType("ValveSA");
//    	builder.addBooleanProperty(".controllable", () -> false, null);
//	    builder.addBooleanProperty("Open", this::isOpen, null);
//	}
}