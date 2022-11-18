package Team4450.Lib;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.Joystick;

/**
 * This is a wrapper class for WPILib Joystick class. Allows us to add
 * or modify Joystick functionality.
 */
public class WpiJoyStick extends Joystick 
{
	private double	deadZone = 0.1, deadZoneX = 0, deadZoneY = 0, invertX = 1.0, invertY = 1.0;

	public WpiJoyStick(int port) 
	{
		super(port);
	}
	
	/**
	 * Set global axis dead zone. Applied if no axis specific dead zone is set.
	 * @param dz Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZone(double dz) throws Exception
	{
		Util.checkRange(dz, 0.0, 1.0, "Dead Zone.");
		
		deadZone = dz;
	}
	
	/**
	 * Set X axis dead zone.
	 * @param dzX Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZoneX(double dzX) throws Exception
	{
		Util.checkRange(dzX, 0.0, 1.0, "Dead Zone.");
		
		deadZoneX = dzX;
	}
	
	/**
	 * Set Y axis dead zone.
	 * @param dzY Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZoneY(double dzY) throws Exception
	{
		Util.checkRange(dzY, 0.0, 1.0, "Dead Zone.");
		
		deadZoneY = dzY;
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
	 * Get Joy Stick X axis deflection value with dead zone and invert applied.
	 * @return X axis deflection.
	 */
	public double GetX()
	{
		// Note: we can't just override the base class getX() function because WPI 
		// marked getX() as final. Don't know why one would do that, but it requires
		// us to use a different function name.
		
		double x = super.getX();
		
		if (deadZoneX > 0 && Math.abs(x) < deadZoneX)
			x = 0;
		else if (deadZone > 0 && Math.abs(x) < deadZone)
			x = 0;
		
		return x * invertX;
	}
	
	/**
	 * Get Joy Stick X axis deflection value with dead zone and invert applied.
	 * @return X axis deflection.
	 */
	public DoubleSupplier GetXDS()
	{
		return () -> GetX();
	}		
	
	/**
	 * Get Joy Stick Y axis deflection value with dead zone and invert applied.
	 * @return Y axis deflection.
	 */
	public double GetY()
	{
		double y = super.getY();
		
		if (deadZoneY > 0 && Math.abs(y) < deadZoneY)
			y = 0;
		else if (deadZone > 0 && Math.abs(y) < deadZone)
			y = 0;
		
		return y * invertY;
	}
	
	/**
	 * Get Joy Stick Y axis deflection value with dead zone and invert applied.
	 * @return Y axis deflection.
	 */
	public DoubleSupplier GetYDS()
	{
		return () -> GetX();
	}	
	
    /**
    *  JoyStick button id enumeration. 
    */
	public enum JoyStickButtonIDs
	{
        TOP_MIDDLE (3),
        TOP_LEFT (4),
        TOP_RIGHT (5),
        TRIGGER (1),
        TOP_BACK (2),
        LEFT_FRONT (6),
        LEFT_REAR (7),
        RIGHT_FRONT (11),
        RIGHT_REAR (10),
        BACK_LEFT (8),
        BACK_RIGHT (9);
          
        public int value;
          
        private JoyStickButtonIDs(int value) 
        {
        	this.value = value;
        }
	};
}
