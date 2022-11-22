package Team4450.Lib;

import java.util.function.DoubleSupplier;

/**
 * This is a wrapper class for WPILib XBoxController class. Allows us to add
 * or modify XBoxController functionality.
 */
public class XboxController extends edu.wpi.first.wpilibj.XboxController 
{
	private double	deadZone = 0.0, invertX = 1.0, invertY = 1.0;
	
	public XboxController(int port) 
	{
		super(port);
	}
	
	/**
	 * Set global joystick axis dead zone.
	 * @param dz Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZone(double dz) throws Exception
	{
		Util.checkRange(dz, 0.0, 1.0, "Dead Zone.");
		
		deadZone = dz;
	}
	
	/**
	 * Invert the X axis output.
	 * @param invert True is invert, false is normal.
	 */
	public void invertX(boolean invert)
	{
		if (invert)
			invertX = -1.0;
		else
			invertX = 1.0;
	}
	
	/**
	 * Invert the Y axis output.
	 * @param invert True is invert, false is normal.
	 */
	public void invertY(boolean invert)
	{
		if (invert)
			invertY = -1.0;
		else
			invertY = 1.0;
	}
	
	/**
	 * Get left joystick X value with dead zone and invert applied.
	 * @return X axis deflection value.
	 */
	@Override
	public double getLeftX()
	{
		double x = super.getLeftX();
		if (Math.abs(x) < deadZone) x = 0.0;
		return x * invertX;
	}
	
	/**
	 * Get left joystick X value with dead zone and invert applied.
	 * This is a convenience function returning getLeftX() as a DoubleSupplier.
	 * @return X axis deflection value.
	 */
	public DoubleSupplier getLeftXDS()
	{
		// () -> is a lamba expression that wraps the getLeftX() function in a
		// double supplier instance.
		return () -> getLeftX();
	}	
	
	/**
	 * Get left joystick Y value with dead zone and invert applied.
	 * Y axis normally returns - for forward + for backward. It makes more sense
	 * to have + be forward and - be backward hence the invert capability.
	 * @return Y axis deflection value.
	 */
	@Override
	public double getLeftY()
	{
		
		double y = super.getLeftY();
		if (Math.abs(y) < deadZone) y = 0.0;
		return y * invertY;
	}
	
	/**
	 * Get left joystick Y value with dead zone and invert applied.
	 * This is a convenience function returning getLeftY() as a DoubleSupplier.
	 * @return Y axis deflection value.
	 */	
	public DoubleSupplier getLeftYDS()
	{
		return () -> getLeftY();
	}
	
	/**
	 * Get right joystick X value with dead zone and invert applied.
	 * @return X axis deflection value.
	 */
	@Override
	public double getRightX()
	{
		double x = super.getRightX();
		if (Math.abs(x) < deadZone) x = 0.0;
		return x * invertX;
	}
	
	/**
	 * Get right joystick X value with dead zone and invert applied.
	 * This is a convenience function returning getRightX() as a DoubleSupplier.
	 * @return X axis deflection value.
	 */	
	public DoubleSupplier getRightXDS()
	{
		return () -> getRightX();
	}	
	
	/**
	 * Get right joystick Y value with dead zone and invert applied.
	 * @return Y axis deflection value.
	 */
	@Override
	public double getRightY()
	{
		double y = super.getRightY();
		if (Math.abs(y) < deadZone) y = 0.0;
		return y * invertY;
	}
	
	/**
	 * Get right joystick Y value with dead zone and invert applied.
	 * This is a convenience function returning getRightY() as a DoubleSupplier.
	 * @return Y axis deflection value.
	 */	
	public DoubleSupplier getRightYDS()
	{
		return () -> getRightY();
	}
	
	/**
	 * Get left trigger as a boolean.
	 * @return True if trigger axis is not zero.
	 */
	public boolean getLeftTrigger()
	{
		if (super.getLeftTriggerAxis() != 0)
		    return true;
		else
			return false;
	}

	/**
	 * Get right trigger as a boolean.
	 * @return True if trigger axis is not zero.
	 */
	public boolean getRightTrigger()
	{
		if (super.getRightTriggerAxis() != 0)
		    return true;
		else
			return false;
	}
	
	/**
	 * Compare POV value to target.
	 * @param angle Target angle.
	 * @return True if POV angle matches target, false if not.
	 */
	public boolean getPOVAngle(int angle)
	{
		if (super.getPOV() == angle)
			return true;
		else
			return false;
	}
	 
	/**
	 *  Game Pad Button id enumeration. 
	 */
	public enum GamePadButtonIDs
	{
	    A (1),
	    B (2),
	    X (3),
	    Y (4),
	    LEFT_BUMPER (5),
	    RIGHT_BUMPER (6),
	    BACK (7),
	    START (8),
	    POV (9);
	      
	    public int value;
	      
	    private GamePadButtonIDs(int value) 
	    {
	    	  this.value = value;
	    }
	}
}
