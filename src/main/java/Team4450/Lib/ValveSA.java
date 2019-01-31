
package Team4450.Lib;

import edu.wpi.first.wpilibj.Solenoid;

/**
 * Wrapper class for Single Action pneumatic valve.
 * Single action opens against a spring. When you call open power is applied and
 * opens the valve against the spring and remains on. When close is called, power
 * is turned off and the spring closes the valve.
 */

public class ValveSA
{
	private final Solenoid	valveOpenSide;
	private boolean			valveOpen = false;

	/**
	 * @param port DIO port wired to valve. Assumes PCM with CAN Id 0.
	 */
	public ValveSA(int port)
	{
	  	Util.consoleLog("port=%d", port); 

		valveOpenSide = new Solenoid(port);
	}

	/**
	 * @param pcmCanId PCM CAN Id number.
	 * @param port DIO port wired to valve.
	 */
	public ValveSA(int pcmCanId, int port)
	{
	  	Util.consoleLog("port=%d", port); 

		valveOpenSide = new Solenoid(pcmCanId, port);
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
		Util.consoleLog();
    
		valveOpenSide.set(true);
		
		valveOpen = true;
	}

	/**
	 * Close the valve.
	 */
	public void Close()
	{
		Util.consoleLog();
    
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
}