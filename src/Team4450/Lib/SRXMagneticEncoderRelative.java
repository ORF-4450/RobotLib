package Team4450.Lib;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

/**
 * Wrapper for SRX Magnetic Encoder used in relative (quadrature) mode.
 */
public class SRXMagneticEncoderRelative implements CounterBase, PIDSource
{
	private WPI_TalonSRX	talon;
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private double			maxPeriod = 0, wheelDiameter = 0, gearRatio = 1.0;
	private int				scaleFactor= 1;
	private boolean			inverted = false;

	public SRXMagneticEncoderRelative()
	{	
	}
	
	/**
	 * Create SRXMagneticEncoder and set the Talon the encoder is
	 * connected to.
	 * @param talon CanTalon object encoder is connected to.
	 */
	public SRXMagneticEncoderRelative( WPI_TalonSRX talon )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		reset();
	}
	
	/**
	 * Create SRMagneticEncoderRelate setting the Talon the encoder is
	 * connected to end the wheel diameter of the wheel being monitored.
	 * @param talon CanTalon object encoder is connected to.
 	 * @param wheelDiameter Wheel diameter in inches.
	 */
	public SRXMagneticEncoderRelative( WPI_TalonSRX talon, double wheelDiameter )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		setWheelDiameter(wheelDiameter);
		
		reset();
	}
	/**
	 * Sets the pid source type to be used for pidGet(). Only displacement
	 * (count) implemented.
	 * @param pidSourceType The pid source type.
	 */
	@Override
	public void setPIDSourceType( PIDSourceType pidSourceType )
	{
		this.pidSourceType = pidSourceType;
	}

	/**
	 * Return the current pid source type setting.
	 * @return The pid source type. Only displacement (count)
	 * implemented.
	 */
	@Override
	public PIDSourceType getPIDSourceType()
	{
		return pidSourceType;
	}

	/**
	 * Return the cumulative encoder count since last reset for input
	 * to a pid controller. Only displacement (count) implemented.
	 * @return The encoder count.
	 */
	@Override
	public double pidGet()
	{
		if (pidSourceType == PIDSourceType.kDisplacement)
			return get();
		else
			return 0;
	}

	/**
	 * Return the encoder count since last reset. 4096 counts per
	 * encoder revolution divided by the scale factor.
	 * @return The encoder count.
	 */
	@Override
	public int get()
	{
		return getRaw() / scaleFactor;
	}
	
	private int getRaw()
	{
		return isInverted() ? talon.getSensorCollection().getQuadraturePosition() * -1 :
			talon.getSensorCollection().getQuadraturePosition();
	}
	
	/**
	 * Return the distance in inches the encoder has recorded since last
	 * reset. If encoder goes backwards the distance is reduced. Computed
	 * from encoder rotations x gear ratio factor x (wheel diameter x 3.13).
	 * @return The distance in inches.
	 */
	public double getDistance()
	{
		return (getRotations() * gearRatio) * (wheelDiameter * 3.14);
	}

	/**
	 * Reset the encoder count.
	 */
	@Override
	public void reset()
	{
		talon.getSensorCollection().setQuadraturePosition(0, 0);
	}

	/**
	 * Not implemented.
	 */
	@Override
	public double getPeriod()
	{
		return 0;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public void setMaxPeriod( double maxPeriod )
	{
		this.maxPeriod = maxPeriod;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public boolean getStopped()
	{
		return false;
	}

	/**
	 * Not implemented.
	 */
	@Override
	public boolean getDirection()
	{
		return false;
	}
	
	/**
	 * Return the number of encoder rotations since reset. Based on
	 * 4096 ticks per encoder revolution.
	 * @return The number of rotations.
	 */
	public double getRotations()
	{
		return getRaw() / 4096.0;
	}

	/**
	 * @return The Talon the encoder is connected to.
	 */
	public WPI_TalonSRX getTalon()
	{
		return talon;
	}

	/**
	 * @param talon The Talon the encoder is connected to.
	 */
	public void setTalon( WPI_TalonSRX talon )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		reset();
	}

	/**
	 * @return True if inverted.
	 */
	public boolean isInverted()
	{
		return inverted;
	}

	/**
	 * @param inverted True to invert.
	 */
	public void setInverted( boolean inverted )
	{
		this.inverted = inverted;
	}

	/**
	 * Get current wheel diameter.
	 * @return The wheel diameter in inches.
	 */
	public double getWheelDiameter()
	{
		return wheelDiameter;
	}

	/**
	 * Set wheel diameter.
	 * @param wheelDiameter The wheel diameter in inches.
	 */
	public void setWheelDiameter( double wheelDiameter )
	{
		this.wheelDiameter = wheelDiameter;
	}

	/**
	 * Get the current gear ratio factor.
	 * @return The gear ratio factor.
	 */
	public double getGearRatio()
	{
		return gearRatio;
	}

	/**
	 * Set the gear ratio factor. This is a factor that the encoder rotation
	 * is multiplied by to yield the wheel rotation. Defaults to 1.
	 * @param gearRatio The gear ratio factor to set
	 */
	public void setGearRatio( double gearRatio )
	{
		this.gearRatio = gearRatio;
	}

	/**
	 * Returns current encoder count scale factor.
	 * @return The scale factor
	 */
	public int getScaleFactor()
	{
		return scaleFactor;
	}

	/**
	 * Set scaling factor that encoder counts will be divided by to reduce
	 * encoder count range.
	 * @param scaleFactor The scale factor to set.
	 */
	public void setScaleFactor( int scaleFactor )
	{
		if (scaleFactor < 1) throw new IllegalArgumentException("Scale factor must be >= 1");
		
		this.scaleFactor = scaleFactor;
	}
}
