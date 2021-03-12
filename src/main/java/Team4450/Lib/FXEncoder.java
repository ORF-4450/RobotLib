package Team4450.Lib;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import Team4450.Lib.Wpilib.PIDSource;
import Team4450.Lib.Wpilib.PIDSourceType;
import edu.wpi.first.wpilibj.CounterBase;
import edu.wpi.first.wpilibj.Encoder;
//import edu.wpi.first.wpilibj.PIDSource;
//import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Timer;

/**
 * Wrapper for Talon FX Encoder in relative (quadrature) mode.
 */
public class FXEncoder implements CounterBase, PIDSource, DoubleSupplier
{
	private WPI_TalonFX		talon;
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private PIDRateType		pidRateType = PIDRateType.ticksPer100ms;
	private double			maxPeriod = 0, wheelDiameter = 0, gearRatio = 1.0, lastSampleTime;
	private int				scaleFactor = 1, lastCount = 0, maxRate = 0;
	private boolean			inverted = false, direction = false;
	private Encoder			simEncoder;
	
	public static final int	TICKS_PER_REVOLUTION = 2048;

	public FXEncoder()
	{	
	}
	
	/**
	 * Create FXEncoder and set the Talon the encoder is
	 * connected to.
	 * @param talon Talon FX object encoder is connected to.
	 */
	public FXEncoder( WPI_TalonFX talon )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;

		// Select Talon FX integrated encoder as feedback device.
		this.talon.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);

		reset();
	}
	
	/**
	 * Create FXEncoder setting the Talon the encoder is
	 * connected to and the wheel diameter of the wheel being monitored.
	 * @param talon Talon FX object encoder is connected to.
 	 * @param wheelDiameter Wheel diameter in inches.
	 */
	public FXEncoder( WPI_TalonFX talon, double wheelDiameter )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		// Select Talon FX integrated encoder as feedback device.
		this.talon.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		
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
	
	/**
	 * Get the encoder cumulative tick count since reset.
	 * @return The tick count.
	 */
	private int getRaw()
	{
		if (simEncoder == null)
//			return isInverted() ? (int) talon.getSensorCollection().getIntegratedSensorPosition() * -1 :
//				(int) talon.getSensorCollection().getIntegratedSensorPosition();
			return isInverted() ? (int) talon.getSelectedSensorPosition() * -1 :
				(int) talon.getSelectedSensorPosition();
		else
			return isInverted() ? simEncoder.get() * -1 : simEncoder.get();
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
		else if (rateType == PIDRateType.ticksPer100ms)
			return rate;
		
		throw new IllegalArgumentException("Invalid PIDRateType");
	}
	
	/**
	 * Return the rate of rotation in RPM.
	 * @return Rotation in revolutions per minute.
	 */
	public int getRPM()
	{
		return getRate(PIDRateType.ticksPer100ms) * 600 / TICKS_PER_REVOLUTION;
	}
	
	/**
	 * Return the current velocity (distance unit per second).
	 * @param rateType MPS/FPS/IPS.
	 * @return Current velocity based on RPM and gear ratio in units requested.
	 */
	public double getVelocity( PIDRateType rateType )
	{
		if (rateType == PIDRateType.velocityMPS)
			return ((getRPM() * gearRatio) * (wheelDiameter * Math.PI) / 12.0 / 60.0) * .3048;
		else if (rateType == PIDRateType.velocityFPS)
			return ((getRPM() * gearRatio) * (wheelDiameter * Math.PI) / 12.0 / 60.0);
		else if (rateType == PIDRateType.velocityIPS)
			return ((getRPM() * gearRatio) * (wheelDiameter * Math.PI) / 60.0);
		
		throw new IllegalArgumentException("Invalid PIDRateType");
	}
	
	/**
	 * Return max rate of rotation recorded since start up.
	 * Relies on regular calls to getRate(), getRPM() or getVelocity().
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The highest rotation rate seen in ticks per selected unit.
	 */
	public int getMaxRate( PIDRateType rateType )
	{
		if (rateType == PIDRateType.ticksPerSec)
			return maxRate * 10;
		else if (rateType == PIDRateType.ticksPer100ms)
			return maxRate;
		
		throw new IllegalArgumentException("Invalid PIDRateType");
	}
	
	/**
	 * Return the max RPM recorded since start up.
	 * Relies on regular calls to getRate(), getRPM() or getVelocity().
	 * @return The highest RPM seen.
	 */
	public int getMaxRPM()
	{
		return getMaxRate(PIDRateType.ticksPer100ms) * 600 / TICKS_PER_REVOLUTION;
	}
	
	/**
	 * Returns the current rate of encoder rotation in ticks/100ms.
	 * @return
	 */
	private int getRawRate()
	{
//		return isInverted() ? (int) talon.getSensorCollection().getIntegratedSensorVelocity() * -1 :
//			(int) talon.getSensorCollection().getIntegratedSensorVelocity();
		return isInverted() ? (int) talon.getSelectedSensorVelocity() * -1 :
			(int) talon.getSelectedSensorVelocity();
	}
	
	/**
	 * Return the max velocity recorded since start up (distance unit per second).
	 * Relies on regular calls to getRate(), getRPM() or getVelocity().
	 * @param rateType MPS/FPS/IPS.
	 * @return Max velocity recorded based on RPM and gear ratio in units requested.
	 */
	public double getMaxVelocity( PIDRateType rateType )
	{
		if (rateType == PIDRateType.velocityMPS)
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * Math.PI) / 12.0 / 60.0) * .3048;
		else if (rateType == PIDRateType.velocityFPS)
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * Math.PI) / 12.0 / 60.0);
		else if (rateType == PIDRateType.velocityIPS)
			return ((getMaxRPM() * gearRatio) * (wheelDiameter * Math.PI) / 60.0);
		
		throw new IllegalArgumentException("Invalid PIDRateType");
	}

	/**
	 * Return the distance in inches the encoder has recorded since last
	 * reset. If encoder goes backwards the distance is reduced. Computed
	 * from encoder rotations x gear ratio factor x (wheel diameter(in) x PI).
	 * @return The distance in inches.
	 */
	public double getDistance()
	{
		return (getRotations() * gearRatio) * (wheelDiameter * Math.PI);
	}
	
	/**
	 * Return the distance the encoder has recorded since last
	 * reset. If encoder goes backwards the distance is reduced. Computed
	 * from encoder rotations x gear ratio factor x (wheel diameter(in) x 3.14).
	 * @param unit Unit type to return the distance in.
	 * @return The distance in selected unit.
	 */
	public double getDistance(DistanceUnit unit)
	{
		if (unit == DistanceUnit.Meters)
			return Util.inchesToMeters(getDistance());
		else if (unit == DistanceUnit.Feet)
			return getDistance() / 12.0;
		else if (unit == DistanceUnit.Inches)
			return getDistance();
			
		throw new IllegalArgumentException("Invalid DistanceUnit");
	}
	
	/**
	 * Reset the encoder count. Note: this method returns immediately but the
	 * encoder may take up to 30 milliseconds to actually reset. If you read
	 * the encoder too soon it may not be reset.
	 */
	@Override
	public void reset()
	{
		if (simEncoder == null)
			//talon.getSensorCollection().setIntegratedSensorPosition(0, 0);
			talon.setSelectedSensorPosition(0);
		else
			simEncoder.reset();
	}

	/**
	 * Reset the encoder count. Will wait the specified milliseconds for the
	 * encoder reset to complete. To make sure the next get() call returns zero counts
	 * the wait should be at least 30ms as the default update period of current counts
	 * is 20ms plus max of 10ms to transmit the reset command.
	 * @param timeout Number of milliseconds to wait for reset completion, zero for no wait.
	 * @return Zero if reset completes, non-zero if times out before reset complete.
	 */
	public int reset(int timeout)
	{
		ErrorCode	errorCode;
		
		if (timeout < 0) throw new IllegalArgumentException("Timeout < 0");

		if (simEncoder == null)
			//errorCode = talon.getSensorCollection().setIntegratedSensorPosition(0, timeout);
			errorCode = talon.setSelectedSensorPosition(0, 0, timeout);
		else
		{
			simEncoder.reset();
			errorCode = ErrorCode.OK;
		}
		
		// The set function typically returns quite quickly but could take up to 10ms to send
		// the reset command. It may take up to 20 additional ms for the zeroing to be reflected
		// in a call to getSelectedSensorPosition() (our get()) as that is the default update period for the
		// encoder counts from FXEnc to the API. We go ahead and delay the requested amount to guarantee
		// we wait long enough that next get() call returns zero counts.
		
		if (errorCode == ErrorCode.OK) 
			Timer.delay(timeout / 1000.0);	// delay value is seconds so we convert.
		else
			Util.consoleLog("encoder reset failed (%d)", errorCode.value);
	
		return errorCode.value;
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
	 * Return if robot is stopped. No movement for Max Period seconds. Relies on regular calls
	 * to get() or getRate() to update the movement status.
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
	 * Return the number of encoder full rotations since reset. Based on
	 * 4096 ticks per encoder full rotation.
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
	public WPI_TalonFX getTalon()
	{
		return talon;
	}

	/**
	 * Sets the Talon the encoder is connected to.
	 * @param talon The Talon the encoder is connected to.
	 */
	public void setTalon( WPI_TalonFX talon )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		reset();
	}
	
	/**
	 * Set the period that the Talon FX updates the RR with encoder count value.
	 * Defaults to ~20ms per CTRE doc. An update is called a frame. This method
	 * will take up to 10ms to complete.
	 * @param period Frame period in milliseconds.
	 */
	public void setStatusFramePeriod(int period)
	{
		if (period < 1) throw new IllegalArgumentException("Period must be >= 1  ms");

		this.talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, period);
		
		Timer.delay(.010);
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

	/**
	 * When getting distance info, selects which measurement unit to use.
	 */
	public enum DistanceUnit
	{
		Meters,
		Feet,
		Inches
	}

	/**
	 * Returns a double when this class is used as a DoubleSupplier.
	 * @return The current encoder count, same as get() method.
	 */
	@Override
	public double getAsDouble()
	{ 
		return get();
	}
	
	/**
	 * Sets the dummy encoder used during simulation to drive the SRX encoder.
	 * The dummy encoder is a regular Encoder which is passed to the EncoderSim
	 * Wpilib class and to this SRX encoder. The regular Encoder links this SRX 
	 * encoder to the simulation as exposed by EncoderSim. So when this simulated
	 * encoder is set, the SRXMagneticEncoderRelative class is driven by the simulated
	 * encoder NOT the actual encoders on the robot.
	 * @param encoder Encoder object used in simulation.
	 */
	public void setSimEncoder(Encoder encoder)
	{
		simEncoder = encoder;
	}
	
	/**
	 * Get the number of ticks (encoder counts) equal to the target distance.
	 * @param distance Target distance in inches.
	 * @param wheelDiameter In inches.
	 * @return Ticks to equal to the target distance.
	 */
	public static int getTicksForDistance(double distance, double wheelDiameter)
	{
		double distancePerTickInches = Math.PI * wheelDiameter / TICKS_PER_REVOLUTION;

		return (int) (distance / distancePerTickInches);
	}
}
