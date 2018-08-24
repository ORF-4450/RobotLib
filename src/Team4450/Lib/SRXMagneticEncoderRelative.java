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
	private double			maxPeriod = 0, wheelDiameter = 0, gearRatio = 1.0, lastSampleTime;
	private int				scaleFactor = 1, lastCount = 0, maxRate = 0;
	private boolean			inverted = false, direction = false;

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
	 * connected to and the wheel diameter of the wheel being monitored.
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
	 * Sets the pid source type to be used for pidGet(). 
	 * @param pidSourceType The pid source type (displacement/rate).
	 */
	@Override
	public void setPIDSourceType( PIDSourceType pidSourceType )
	{
		this.pidSourceType = pidSourceType;
	}

	/**
	 * Return the current pid source type setting.
	 * @return The pid source type.
	 */
	@Override
	public PIDSourceType getPIDSourceType()
	{
		return pidSourceType;
	}

	/**
	 * Return the cumulative encoder count since last reset for input
	 * to a pid controller, or return the current rate of change of the
	 * encoder (velocity).
	 * @return The encoder current count or rate of change (ticks/100ms).
	 */
	@Override
	public double pidGet()
	{
		if (pidSourceType == PIDSourceType.kDisplacement)
			return get();
		else
			return getRate();
	}

	/**
	 * Return the encoder count since last reset. 4096 counts per
	 * encoder revolution divided by the scale factor.
	 * @return The encoder count.
	 */
	@Override
	public int get()
	{
		int count = getRaw() / scaleFactor;
		
		if (count == lastCount)
			if (lastSampleTime == 0) lastSampleTime = Util.timeStamp();
		
		if (count > lastCount)
		{
			direction = true;
			lastSampleTime = 0;
		}
		
		if (count < lastCount) 
		{
			direction = false;
			lastSampleTime = 0;
		}
		
		lastCount = count;
		
		return count;
	}
	
	private int getRaw()
	{
		return isInverted() ? talon.getSensorCollection().getQuadraturePosition() * -1 :
			talon.getSensorCollection().getQuadraturePosition();
	}
	
	/**
	 * Return rate of rotation (velocity).
	 * @return The rotation rate in ticks per 100ms.
	 */
	public int getRate()
	{
		get();	// Update direction and stopped.
		
		int rate = getRawRate();
		
		if (Math.abs(rate) > maxRate) maxRate = rate;
		
		return rate;
	}
	
	/**
	 * Return the rate of rotation in RPM.
	 * @return Rotation in revolutions per second.
	 */
	public int getRPM()
	{
		return getRawRate() * 600 / 4096;
	}
	
	/**
	 * Return max rate of rotation (velocity) recorded since
	 * start up.
	 * @return The highest rotation rate seen in ticks per 100ms.
	 */
	public int getMaxRate()
	{
		return maxRate;
	}
	
	/**
	 * Return the max RPM recorded since start up.
	 * @return The highest RPM seen.
	 */
	public int getMaxRPM()
	{
		return getMaxRate() * 600 / 4096;
	}
	
	private int getRawRate()
	{
		return isInverted() ? talon.getSensorCollection().getQuadratureVelocity() * -1 :
			talon.getSensorCollection().getQuadratureVelocity();
	}
	
	/**
	 * Return the distance in inches the encoder has recorded since last
	 * reset. If encoder goes backwards the distance is reduced. Computed
	 * from encoder rotations x gear ratio factor x (wheel diameter x 3.14).
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
	 * Set the time that should elapse with no movement to determine
	 * if the robot is stopped.
	 * @param maxPeriod The time in seconds.
	 */
	@Override
	public void setMaxPeriod( double maxPeriod )
	{
		if (maxPeriod < 0) throw new IllegalArgumentException("Max Period must be >= 0");
		
		this.maxPeriod = maxPeriod;
	}

	/**
	 * Return if robot is stopped. No movement for Max Period seconds.
	 * @return True if stopped, false if moving.
	 */
	@Override
	public boolean getStopped()
	{
		if (maxPeriod == 0 || lastSampleTime == 0) return false;
		
		if (Util.getElaspedTime(lastSampleTime) >= maxPeriod) return true;
		
		return false;
	}

	/**
	 * Return direction of encoder movement. Assumes positive encoder counting
	 * as indicating the "forward" direction of the  robot.
	 * @return The direction (true = forward, false = backward).
	 */
	@Override
	public boolean getDirection()
	{
		return direction;
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
		if (wheelDiameter < 1) throw new IllegalArgumentException("Wheel diameter must be >= 1");
		
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
