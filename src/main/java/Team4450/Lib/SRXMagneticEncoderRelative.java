package Team4450.Lib;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced;
import com.ctre.phoenix.motorcontrol.TalonSRXSimCollection;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import Team4450.Lib.Wpilib.PIDSource;
import Team4450.Lib.Wpilib.PIDSourceType;
import edu.wpi.first.wpilibj.CounterBase;
//import edu.wpi.first.wpilibj.PIDSource;
//import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Timer;

/**
 * Wrapper for Talon SRX Magnetic Encoder used in relative (quadrature) mode.
 */
public class SRXMagneticEncoderRelative implements CounterBase, PIDSource, DoubleSupplier
{
	private WPI_TalonSRX	talon;
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private PIDRateType		pidRateType = PIDRateType.ticksPer100ms;
	private double			maxPeriod = 0, wheelDiameter = 0, gearRatio = 1.0, lastSampleTime;
	private int				scaleFactor = 1, lastCount = 0, maxRate = 0;
	private boolean			inverted = false, direction = false;
	
	private TalonSRXSimCollection	simCollection;
	
	public static final int			TICKS_PER_REVOLUTION = 4096;
	private static final double		TICKS_PER_REVOLUTION_D = 4096.0;
	
	public SRXMagneticEncoderRelative()
	{	
	}
	
	/**
	 * Create SRXMagneticEncoderRelative and set the Talon the encoder is
	 * connected to.
	 * @param talon Talon SRX object instance encoder is connected to.
	 */
	public SRXMagneticEncoderRelative( WPI_TalonSRX talon )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		// Select Talon CTRE Magnetic encoder relative mode as feedback device.
		this.talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);

		reset();
	}
	
	/**
	 * Create SRXMagneticEncoderRelative setting the Talon the encoder is
	 * connected to and the wheel diameter of the wheel being monitored.
	 * @param talon Talon SRX object encoder is connected to.
 	 * @param wheelDiameter Wheel diameter in inches.
	 */
	public SRXMagneticEncoderRelative( WPI_TalonSRX talon, double wheelDiameter )
	{
		Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		// Select Talon CTRE Magnetic encoder relative mode as feedback device.
		this.talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative);
		
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
					
				case velocityRPM:
					return getVelocity(PIDRateType.velocityRPM);
					
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
//			return isInverted() ? talon.getSensorCollection().getQuadraturePosition() * -1 :
//				talon.getSensorCollection().getQuadraturePosition();
		return isInverted() ? (int) talon.getSelectedSensorPosition() * -1 :
			(int) talon.getSelectedSensorPosition();
	}
	
	/**
	 * Return rate of rotation in ticks.
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The rotation rate in ticks per selected time unit.
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
	 * Return the rate of rotation in RPM. This is encoder RPM, gear ratio not applied.
	 * @return Rotation rate in revolutions per minute.
	 */
	public int getRPM()
	{
		return getRate(PIDRateType.ticksPer100ms) * 600 / TICKS_PER_REVOLUTION;
	}
	
	/**
	 * Return the current wheel velocity (distance unit per second).
	 * @param rateType RPM/MPS/FPS/IPS.
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
		else if (rateType == PIDRateType.velocityRPM)
			return getRPM() * gearRatio;

		throw new IllegalArgumentException("Invalid PIDRateType");
	}
	
	/**
	 * Return max rate of rotation recorded since start up.
	 * Relies on regular calls to getRate(), getRPM() or getVelocity().
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The highest rotation rate seen in ticks per selected time unit.
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
	 * Return the max RPM recorded since start up. This is encoder RPM, gear ratio not applied.
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
//		return isInverted() ? talon.getSensorCollection().getQuadratureVelocity() * -1 :
//			talon.getSensorCollection().getQuadratureVelocity();
		return isInverted() ? (int) talon.getSelectedSensorVelocity() * -1 :
			(int) talon.getSelectedSensorVelocity();
	}
	
	/**
	 * Return the max wheel velocity recorded since start up (distance unit per second).
	 * Relies on regular calls to getRate(), getRPM() or getVelocity().
	 * @param rateType RPM/MPS/FPS/IPS.
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
		else if (rateType == PIDRateType.velocityRPM)
			return getMaxRPM() * gearRatio;
		
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
//			talon.getSensorCollection().setQuadraturePosition(0, 0);
		talon.setSelectedSensorPosition(0);
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

//			errorCode = talon.getSensorCollection().setQuadraturePosition(0, timeout);
		errorCode = talon.setSelectedSensorPosition(0, 0, timeout);
		
		// The set function typically returns quite quickly but could take up to 10ms to send
		// the reset command. It may take up to 20 additional ms for the zeroing to be reflected
		// in a call to getSelectedPosition() (our get()) as that is the default update period for the
		// encoder counts from SRXEnc to the API. We go ahead and delay the requested amount to guarantee
		// we wait long enough that next get() call returns zero counts.
		
		if (errorCode == ErrorCode.OK) 
			Timer.delay(timeout / 1000.0);
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
		return getRaw() / TICKS_PER_REVOLUTION_D;
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
	 * Set the period that the Talon updates the RR with encoder count value.
	 * Defaults to 20ms per CTRE doc. An update is called a frame. This method
	 * will take 10ms to complete. It sets the status frame 2.
	 * @param period Frame update period in milliseconds.
	 */
	public void setStatusFramePeriod(int period)
	{
		if (period < 1) throw new IllegalArgumentException("Period must be >= 1  ms");

		this.talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, period);
		
		Timer.delay(.010);
	}
	
	/**
	 * Set the period that the Talon updates the RR with the pulse width value.
	 * Pulse width value is used for reading absolute position.
	 * Defaults to ~150ms per CTRE doc. An update is called a frame. This method
	 * will take 10ms to complete. It sets the status frame 8.
	 * @param period Frame update period in milliseconds.
	 */
	public void setStatusFrame8Period(int period)
	{
		if (period < 1) throw new IllegalArgumentException("Period must be >= 1  ms");

		this.talon.setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, period);
		
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
	 * Get the current gear ratio.
	 * @return The gear ratio factor.
	 */
	public double getGearRatio()
	{
		return gearRatio;
	}

	/**
	 * Set the gear ratio. This the number of encoder rotations per
	 * one wheel rotation. Defaults to 1 meaning a 1:1 ratio between encoder
	 * shaft revolutions and wheel revolutions. A value of 2 would be 2:1 or
	 * 2 encoder rotations per 1 wheel rotations. Used when the encoder is not
	 * mounted to the wheel axle.
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
		 * Rate of encoder rotation in ticks per 100ms.
		 */
		ticksPer100ms,
		
		/**
		 * Rate of encoder rotation in ticks per second.
		 */
		ticksPerSec,
		
		/**
		 * Rate of encoder rotation in revolutions per minute.
		 */
		RPM,
		
		/**
		 * Speed in RPM based on wheel size and gear ratio.
		 */
		velocityRPM,

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
	 * Get the number of ticks (encoder counts) equal to the target distance.
	 * Assumes 1:1 wheel/encoder gear ratio.
	 * @param distance Target distance in feet.
	 * @param wheelDiameter In inches.
	 * @return Ticks to equal to the target distance.
	 */
	public static int getTicksForDistance(double distance, double wheelDiameter)
	{
		double distancePerTickFeet = (Math.PI * wheelDiameter / TICKS_PER_REVOLUTION) / 12;
		return (int) (distance / distancePerTickFeet);
	}
	
	/**
	 * Get the number of ticks (encoder counts) equal to the target distance.
	 * @param distance Target distance in feet.
	 * @param wheelDiameter In inches.
	 * @param gearRatio The number of encoder rotations to 1 wheel rotation.
	 * @return Ticks to equal to the target distance.
	 */
	public static int getTicksForDistance(double distance, double wheelDiameter, double gearRatio)
	{
		double distancePerTickFeet = (Math.PI * wheelDiameter / TICKS_PER_REVOLUTION) / 12;
		distancePerTickFeet *= gearRatio;
		return (int) (distance / distancePerTickFeet);
	}
	
	/**
	 * Initialize the built-in simulation support in the SRX encoder. Must be called before sim
	 * run starts. Note that the built-in sim support is not reliable as of this time so we will
	 * retain our original solution with dummy encoders. This code is retained in the event that
	 * the built-in support is fixed. We will have to test after CTRE updates to see if the problems
	 * get fixed. The main issue is spurious values being returned and encoder resets not being
	 * obeyed. It mostly works...
	 */
	public void initializeSim()
	{
		simCollection = talon.getSimCollection();
	}
	
	// https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20General/DifferentialDrive_Simulation/src/main/java/frc/robot/Robot.java

	/**
	 * During simulation sets the current values for the encoder.
	 * @param position Current position in meters (from DifferentialDrivesim).
	 * @param velocity Current velocity in meters/sec (from DifferentialDrivesim).
	 */
	public void setSimValues(double position, double velocity)
	{
		//Util.consoleLog("%.3f, %.3f", position, velocity);
		
		simCollection.setQuadratureRawPosition(metersToTicks(position));
		simCollection.setQuadratureVelocity(velocityToTicks(velocity));
		
		simCollection.setPulseWidthPosition(metersToTicks(position) % TICKS_PER_REVOLUTION);
		simCollection.setPulseWidthVelocity(velocityToTicks(velocity));
	}
	
	/**
	 * Convert meters into encoder ticks. Gear ratio applied.
	 * @param meters Meters value.
	 * @return Encoder ticks.
	 */
	public int metersToTicks(double meters) 
	{
	    double rotations = meters / (Math.PI * Util.inchesToMeters(wheelDiameter));
	    rotations *= gearRatio;
	    return (int) (TICKS_PER_REVOLUTION_D * rotations);
	}
	
	/**
	 * Convert velocity into encoder ticks. Gear ratio applied.
	 * @param velocityMetersPerSecond Velocity in meters/second.
	 * @return Encoder ticks per 100ms.
	 */
	private int velocityToTicks(double velocityMetersPerSecond)
	{
	    double rotationsPerSecond = velocityMetersPerSecond / (Math.PI * Util.inchesToMeters(wheelDiameter));
	    rotationsPerSecond *= gearRatio;
	    double rotationsPer100ms = rotationsPerSecond / 10d;
	    int ticksPer100ms = (int) (rotationsPer100ms * TICKS_PER_REVOLUTION_D);
	    return ticksPer100ms;
	}
	
	/**
	 * Convert ticks to degrees.
	 * @param ticks Ticks.
	 * @return Degrees 0-360. Truncated to .1 degree.
	 */
	public static double ticksToDegrees(double ticks) 
	{
		double deg = ticks * 360.0 / TICKS_PER_REVOLUTION_D;

		/* truncate to 0.1 res */
		deg *= 10;
		deg = (int) deg;
		deg /= 10;

		return deg;
	}
	
	/**
	 * Return the absolute position of encoder in ticks. Default update period is
	 * ~150ms. Change with setStatusFrame8Period().
	 * @return The encoder position, as 0-4096.
	 */
	public int getAbsolutePosition()
	{
		return talon.getSensorCollection().getPulseWidthPosition() & 0xFFF;
	}
}
