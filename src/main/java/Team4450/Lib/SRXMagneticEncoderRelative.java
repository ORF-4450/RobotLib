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
	private PIDRateType		pidRateType = PIDRateType.ticksPer100ms;
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
	 * Sets the pid source type to be used for pidGet() calls by PID controllers. When
	 * selecting rate, be sure to set the PIDRateType to use.
	 * @param pidSourceType The pid source type (displacement(default)/rate).
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
			switch (pidRateType)
			{
				case ticksPer100ms:
					return getRate(PIDRateType.ticksPer100ms);
					
				case ticksPerSec:
					return getRate(PIDRateType.ticksPerSec);
					
				case RPM:
					return getRPM();
					
				case velocityFPS:
					return getVelocity(PIDRateType.velocityFPS);
					
				case velocityMPS:
					return getVelocity(PIDRateType.velocityMPS);
					
				case velocityIPS:
					return getVelocity(PIDRateType.velocityIPS);
					
				default:
					return getRate(PIDRateType.ticksPer100ms);
			}
	}

	/**
	 * Return the encoder count since last reset. Note: 4096 counts per
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
	 * Return rate of rotation.
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The rotation rate in ticks per selected unit.
	 */
	public int getRate( PIDRateType rateType )
	{
		get();	// Update direction and stopped.
		
		int rate = getRawRate();
		
		if (Math.abs(rate) > maxRate) maxRate = rate;
		
		if (rateType == PIDRateType.ticksPerSec)
			return rate * 10;
		else
			return rate;
	}
	
	/**
	 * Return the rate of rotation in RPM.
	 * @return Rotation in revolutions per minute.
	 */
	public int getRPM()
	{
		return getRate(PIDRateType.ticksPer100ms) * 600 / 4096;
	}
	
	/**
	 * Return the current velocity (distance unit per second).
	 * @param rateType MPS/FPS/IPS.
	 * @return Current velocity based on RPM and gear ratio in units requested.
	 */
	public double getVelocity( PIDRateType rateType )
	{
		if (rateType == PIDRateType.velocityMPS)
			return ((getRPM() * gearRatio) * (wheelDiameter * 3.14) / 12 / 60) * .3048;
		else if (rateType == PIDRateType.velocityFPS)
			return ((getRPM() * gearRatio) * (wheelDiameter * 3.14) / 12 / 60);
		else
			return ((getRPM() * gearRatio) * (wheelDiameter * 3.14) / 60);
	}
	
	/**
	 * Return max rate of rotation recorded since start up.
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The highest rotation rate seen in ticks per selected unit.
	 */
	public int getMaxRate( PIDRateType rateType )
	{
		if (rateType == PIDRateType.ticksPerSec)
			return maxRate * 10;
		else
			return maxRate;
	}
	
	/**
	 * Return the max RPM recorded since start up.
	 * @return The highest RPM seen.
	 */
	public int getMaxRPM()
	{
		return getMaxRate(PIDRateType.ticksPer100ms) * 600 / 4096;
	}
	
	private int getRawRate()
	{
		return isInverted() ? talon.getSensorCollection().getQuadratureVelocity() * -1 :
			talon.getSensorCollection().getQuadratureVelocity();
	}
	
	/**
	 * Return the max velocity recorded since start up (distance unit per second).
	 * @param rateType MPS/FPS/IPS.
	 * @return Max velocity recorded based on RPM and gear ratio in units requested.
	 */
	public double getMaxVelocity( PIDRateType rateType )
	{
		if (rateType == PIDRateType.velocityMPS)
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * 3.14) / 12 / 60) * .3048;
		else if (rateType == PIDRateType.velocityFPS)
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * 3.14) / 12 / 60);
		else
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * 3.14) / 60);
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
	 * Returns the Talon the encoder is connected to.
	 * @return The Talon instance the encoder is connected to.
	 */
	public WPI_TalonSRX getTalon()
	{
		return talon;
	}

	/**
	 * Sets the Talon the encoder is connected to.
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
	 * is multiplied by to yield the wheel rotation. Defaults to 1 meaning a
	 * 1:1 ratio between encoder shaft revolutions and wheel revolutions.
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
	
	/**
	 * Reset max rotation rate and max RPM.
	 */
	public void resetMaxRate()
	{
		maxRate = 0;
	}
	
	/**
	 * Set the PID rate type (unit) to use when doing rate PID.
	 * @param rateType The rate type to send to PID controllers.
	 */
	public void setPIDRateType( PIDRateType rateType )
	{
		pidRateType = rateType;
	}

	/**
	 * Return the current PIDRateType set.
	 * @return The PIDRateType.
	 */
	public PIDRateType getPIDRateType()
	{
		return pidRateType;
	}

	/**
	 * When using PID Source Type of rate, selects which rate 
	 * measurement to use.
	 */
	public enum PIDRateType
	{
		/**
		 * Rate of rotation in ticks per 100ms.
		 */
		ticksPer100ms,
		
		/**
		 * Rate of rotation in ticks per second.
		 */
		ticksPerSec,
		
		/**
		 * Rate of rotation in revolutions per minute.
		 */
		RPM,
		
		/**
		 * Speed in feet per second based on wheel size and
		 * gear ratio.
		 */
		velocityFPS,
		
		/**
		 * Speed in meters per second based on wheel size and
		 * gear ratio.
		 */
		velocityMPS,
		
		/**
		 * Speed in inches per second based on wheel size and
		 * gear ratio.
		 */
		velocityIPS
	}
}
