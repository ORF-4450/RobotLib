
package Team4450.Lib;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;

/**
 * Wrapper class for Double Action pneumatic valve.
 * Double action is a sliding valve. Calling open applies power to the open side momentarily causing
 * the valve to move to the open position. Power is then turned off and the valve
 * stays where it is (open). Calling close applies power to the close side momentarily causing the
 * valve to move to the closed position. Power is then turned off and the valve
 * stays where it is (closed).
 * 
 * Open and Close are arbitrary definitions, they are actually defined by the physical robot
 * valve piping/wiring and what you want the cylinder to do. Typically you would pipe the "open" side
 * of the valve to extend a cylinder and close to retract. For DA valves, our convention is to wire
 * the A side to the first port and pipe to open or extend the cylinder and B side to close or retract.
 * Again, these are conventions and the reality is what you design your valve to cylinder piping to be
 * and the wiring to the corresponding sides of the valve to the PCM ports.
 */

public class ValveDA
{
	private final Solenoid	valveOpenSide, valveCloseSide;
	private boolean			valveOpen = false;
	private int				port, pcmCanId = 0;

	/**
	 * Sets the time to apply power to make sure valve slide moves correctly.
	 * The movement takes time so power has to be applied until slide has
	 * moved from one side to the other. In seconds, defaults to .05 sec.
	 */
	public double           solenoidSlideTime;

	/**
	 * @param port PCM port wired to open/A side of valve. Close/B side is wired to PCM next port.
	 * Assumes PCM CAN Id 0.
	 */

	public ValveDA(int port)
	{
	  	Util.consoleLog("pcm=%d, port=%d", pcmCanId, port);

		valveOpenSide = new Solenoid(PneumaticsModuleType.CTREPCM, port);
		valveCloseSide = new Solenoid(PneumaticsModuleType.CTREPCM, port + 1);
    
		this.port = port;
		
		solenoidSlideTime = .05;
    
		//Close();
	}

	/**
	 * @param pcmCanId PCM CAN Id number.
	 * @param port PCM port wired to open/A side of valve. Close/B side is wired to PCM next port.
	 */

	public ValveDA(int pcmCanId, int port)
	{
	  	Util.consoleLog("pcm=%d, port=%d", pcmCanId, port);

		valveOpenSide = new Solenoid(pcmCanId, PneumaticsModuleType.CTREPCM, port);
		valveCloseSide = new Solenoid(pcmCanId, PneumaticsModuleType.CTREPCM, port + 1);
    
		this.port = port;
		this.pcmCanId = pcmCanId;
		
		solenoidSlideTime = .05;
    
		//Close();
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
	 */
	public void Open()
	{
	  	Util.consoleLog("pcm=%d, port=%d", pcmCanId, port);
    
		valveCloseSide.set(false);
    
		valveOpenSide.set(true);
		Timer.delay(solenoidSlideTime);
		valveOpenSide.set(false);
		
		valveOpen = true;
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
	  	Util.consoleLog("pcm=%d, port=%d", pcmCanId, port);
    
		valveOpenSide.set(false);
    
		valveCloseSide.set(true);
		Timer.delay(solenoidSlideTime);
		valveCloseSide.set(false);
		
		valveOpen = false;
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
	 * @return True if valve open (pressure on A side), false if closed.
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
}