package Team4450.Lib;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.ctre.phoenix6.hardware.TalonFX;
import Team4450.Lib.Wpilib.PIDSource;
import Team4450.Lib.Wpilib.PIDSourceType;
//import Team4450.Lib.Wpilib.Sendable;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.CounterBase;
//import edu.wpi.first.wpilibj.PIDSource;
//import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.Timer;

/**
 * Wrapper for Talon FX Encoder in relative (quadrature) mode.
 */
public class FXEncoder implements CounterBase, PIDSource, DoubleSupplier, Sendable
{
	private TalonFX			talon;
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private PIDRateType		pidRateType = PIDRateType.ticksPer100ms;
	private double			maxPeriod = 0, wheelDiameter = 0, gearRatio = 1.0, lastSampleTime;
	private int				scaleFactor = 1, lastCount = 0, maxRate = 0, absoluteOffset;
	private boolean			inverted = false, direction = false;
	private String			name;
	private TalonFXSimState	simState;
	
	public static final int		TICKS_PER_REVOLUTION = 2048;
	private static final double	TICKS_PER_REVOLUTION_D = 2048.0;

	public FXEncoder()
	{	
	}
	
	/**
	 * Create FXEncoder and set the TalonFX the encoder is
	 * connected to.
	 * @param talon TalonFX object encoder is connected to.
	 */
	public FXEncoder( TalonFX talon )
	{
		this("FXEncoder", talon, 0);
	}
	
	/**
	 * Create FXEncoder and set the TalonFX the encoder is
	 * connected to.
	 * @param name Name assigned in Network Tables viewer.
	 * @param talon TalonFX object encoder is connected to.
	 */
	public FXEncoder( String name, TalonFX talon )
	{
		this(name, talon, 0);
	}
	
	/**
	 * Create FXEncoder setting the TalonFX the encoder is
	 * connected to and the wheel diameter of the wheel being monitored.
	 * @param talon TalonFX object encoder is connected to.
 	 * @param wheelDiameter Wheel diameter in inches.
	 */
	public FXEncoder( TalonFX talon, double wheelDiameter )
	{
		this("FXEncoder", talon, wheelDiameter);
	}
	
	/**
	 * Create FXEncoder setting the TalonFX the encoder is
	 * connected to and the wheel diameter of the wheel being monitored.
	 * @param name Name assigned in Network Tables viewer.
	 * @param talon TalonFX object encoder is connected to.
 	 * @param wheelDiameter Wheel diameter in inches.
	 */
	public FXEncoder( String name, TalonFX talon, double wheelDiameter )
	{
		//Util.consoleLog("%s", talon.getDescription());
		
		this.talon = talon;
		
		//rich Select Talon FX integrated encoder as feedback device.
		//this.talon.configSelectedFeedbackSensor(FeedbackDevice.IntegratedSensor);
		
		// May not need this for 2025. RotoSensor is default feedback source.
//		TalonFXConfigurator config = this.talon.getConfigurator();
//		FeedbackConfigs configs = new FeedbackConfigs();
//		configs.FeedbackSensorSource = FeedbackSensorSourceValue.RotorSensor;
//		config.apply(configs);
		
		setWheelDiameter(wheelDiameter);
        
        name = String.format("%s[%d]", name, talon.getDeviceID());

       	SendableRegistry.addLW(this, name);

        Util.consoleLog("%s", name);
		
		reset();
	}
    
    /**
     * Returns the name of the object instance.
     * @return Name of this object instance.
     */
    public String getName()
    {
    	return name;
    }
    
    /**
     * Sets the name of the object instance.
     * @param name Name of this object instance.
     */
    public void setName(String name)
    {
    	Util.consoleLog("%s", name);
    	
    	name = String.format("%s[%d]", name, talon.getDeviceID());
    	
    	SendableRegistry.setName(this, name);
    }
    
    /**
     * Release resources in preparation to destroy this object.
     */
    public void close()
    {
    	Util.consoleLog("%s", name);

    	SendableRegistry.remove(this);
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
	 * Return the encoder count since last reset. Note: 2048 counts per
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
//rich			return isInverted() ? (int) talon.getSensorCollection().getIntegratedSensorPosition() * -1 :
//				(int) talon.getSensorCollection().getIntegratedSensorPosition();
			//return isInverted() ? (int) talon.getp.getSelectedSensorPosition() * -1 :
			//	(int) talon.getSelectedSensorPosition();
			
			var rotorPosSignal = talon.getRotorPosition(true); // rotations.
			double rotorRotations = rotorPosSignal.getValueAsDouble();
			
			return isInverted() ? (int) (rotorRotations * -1.0 * TICKS_PER_REVOLUTION_D) :
				(int) (rotorRotations * TICKS_PER_REVOLUTION_D);
	}
	
	/**
	 * Return rate of rotation.
	 * @param rateType Ticks unit: per 100ms or per Second.
	 * @return The rotation rate in ticks per selected time unit.
	 */
	public int getRate( PIDRateType rateType )
	{
		get();	// Update direction and stopped.
		
		int rate = getRawRate();
		
		if (Math.abs(rate) > maxRate) maxRate = Math.abs(rate);
		
		if (rateType == PIDRateType.ticksPerSec)
			return rate * 10;
		else if (rateType == PIDRateType.ticksPer100ms)
			return rate;
		
		throw new IllegalArgumentException("Invalid PIDRateType");
	}
	
	/**
	 * Return the rate of encoder rotation in RPM.
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
//rich		return isInverted() ? (int) talon.getSensorCollection().getIntegratedSensorVelocity() * -1 :
//			(int) talon.getSensorCollection().getIntegratedSensorVelocity();
		//return isInverted() ? (int) talon.getSelectedSensorVelocity() * -1 :
		//	(int) talon.getSelectedSensorVelocity();
		
		var rotorVelSignal = talon.getRotorVelocity(true); // Rotations/second
		double rotorVelocity = rotorVelSignal.getValueAsDouble();
		
		return isInverted() ? (int) (rotorVelocity * -1.0 * TICKS_PER_REVOLUTION_D * 0.1) :
			(int) (rotorVelocity * TICKS_PER_REVOLUTION_D * 0.1);

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
		StatusCode	errorCode;
		
		errorCode = talon.setPosition(0);

		if (errorCode != StatusCode.OK) 
			Util.consoleLog("encoder reset failed (%d) %s", errorCode.value, errorCode.name());
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
		StatusCode	errorCode;
		
		if (timeout < 0) throw new IllegalArgumentException("Timeout < 0");

		//errorCode = talon.getSensorCollection().setIntegratedSensorPosition(0, timeout);
		errorCode = talon.setPosition(0);
		
		// The set function typically returns quite quickly but could take up to 10ms to send
		// the reset command. It may take up to 20 additional ms for the zeroing to be reflected
		// in a call to getSelectedSensorPosition() (our get()) as that is the default update period for the
		// encoder counts from FXEnc to the API. We go ahead and delay the requested amount to guarantee
		// we wait long enough that next get() call returns zero counts.
		
		if (errorCode == StatusCode.OK) 
			Timer.delay(timeout / 1000.0);	// delay value is seconds so we convert.
		else
			Util.consoleLog("encoder reset failed (%d) %s", errorCode.value, errorCode.name());
	
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
	public TalonFX getTalon()
	{
		return talon;
	}

	/**
	 * Sets the Talon the encoder is connected to.
	 * @param talon The Talon the encoder is connected to.
	 */
	public void setTalon( TalonFX talon )
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

		//StatusCode errorCode = this.talon.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, period);

		StatusCode errorCode = talon.getPosition(true).setUpdateFrequency(1000 / period);
		
		if (errorCode == StatusCode.OK) 
			Timer.delay(.010);
		else
			Util.consoleLog("set status frame period failed (%d) %s", errorCode.value, errorCode.name());		
	}
	
	/**
	 * Set the period that the Talon updates the RR with the absolute encoder value.
	 * Defaults to ~240ms per CTRE doc. An update is called a frame. This method
	 * will take 10ms to complete. It sets the status frame 21.
	 * @param period Frame update period in milliseconds.
	 */
	public void setStatusFrame8Period(int period)
	{
		if (period < 1) throw new IllegalArgumentException("Period must be >= 1  ms");
		
		// Waiting for absolute encoder doc for 2025.

//		StatusCode errorCode = this.talon.setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, period);
//		
//		if (errorCode == StatusCode.OK) 
//			Timer.delay(.010);
//		else
//			Util.consoleLog("set status frame period failed (%d) %s", errorCode.value, errorCode.name());		
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
	 * Get the number of ticks (encoder counts) equal to the target distance. Requires
	 * a non-zero wheel diameter set for the encoder.
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
	 * Initialize the built-in simulation support in the FX encoder. Must be called before sim
	 * run starts. As of 2025, this is only needed if not using simulation on the TalonFX either
	 * by direct code or using the Talon_FX wrapper class (which has TalonFX simulation built-in).
	 */
	public void initializeSim()
	{
		simState = talon.getSimState();
	}
	
	// https://github.com/CrossTheRoadElec/Phoenix-Examples-Languages/blob/master/Java%20General/DifferentialDrive_Simulation/src/main/java/frc/robot/Robot.java

	/**
	 * During simulation sets the current values for the encoder. Requires
	 * a non-zero wheel diameter set for the encoder.  As of 2025, this is 
	 * only needed if not using simulation on the TalonFX either by direct 
	 * code or using the Talon_FX wrapper class (which has TalonFX simulation built-in).
	 * @param position Current position in meters (from DifferentialDrivesim).
	 * @param velocity Current velocity in meters/sec (from DifferentialDrivesim).
	 */
	public void setSimValues(double position, double velocity)
	{
		//Util.consoleLog("%.3f, %.3f", position, velocity);
		
		simState.setRawRotorPosition(metersToTicks(position) / TICKS_PER_REVOLUTION);
		
		simState.setRotorVelocity(velocityToTicks(velocity) / TICKS_PER_REVOLUTION);
	}
	
	/**
	 * Resets the encoder by calling reset() or reset(waitTime) but also resets
	 * the internal simulation position of the encoder to zero. Used to rerun a
	 * auto program under sim, restarting the sim position tracking at zero.
	 * @param timeout Number of milliseconds to wait for reset completion, zero for no wait.
	 * @return Zero if reset completes, non-zero if times out before reset complete.
	 */
	public int resetSim(int timeout)
	{
		Util.consoleLog();
		
		setSimValues(0, 0);
		
		if (timeout == 0)
		{
			reset();
			return 0;
		}
		else
			return reset(timeout);
	}
	
	/**
	 * Convert meters into encoder ticks. Gear ration applied. Requires
	 * a non-zero wheel diameter set for the encoder.
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
	public static double ticksToDegrees(int ticks) 
	{
		double deg = (int) ticks * 360.0 / TICKS_PER_REVOLUTION_D;

		/* truncate to 0.1 res */
		deg *= 10;
		deg = (int) deg;
		deg /= 10;

		return deg;
	}
	
	/**
	 * Return the absolute position of encoder in ticks. Default update period is
	 * ~240ms. Change with setStatusFrame21Period().
	 * @return The encoder position, as 0-2048 clockwise.
	 */
	public int getAbsolutePosition()
	{
		return (Math.abs(get() - (get() / TICKS_PER_REVOLUTION * TICKS_PER_REVOLUTION))) - absoluteOffset;
	}
	
	/**
	 * Return the absolute position of encoder in degrees. Default update period is
	 * ~240ms. Change with setStatusFrame21Period().
	 * @return The encoder position, as 0-360.0 clockwise.
	 */
	public double getAbsolutePositionDeg()
	{
		return ticksToDegrees(getAbsolutePosition());
	}
	
	/**
	 * Return the absolute position of encoder as a Rotation2d object. Default update period is
	 * ~240ms. Change with setStatusFrame21Period().
	 * @return The encoder position in radians as a Rotation2d.
	 */
	public Rotation2d getAbsolutePosition2D()
	{
		return Rotation2d.fromDegrees(-ticksToDegrees(getAbsolutePosition()));
	}
	
	/**
	 * Set an offset to be applied to the absolute position to move the internal
	 * zero direction to the desired zero direction. Offset is applied to calls
	 * to getAbsolutePosition().
	 * @param offset In ticks, 0-2048 representing 0-360 degrees, going clockwise.
	 */
	public void setAbsoluteOffset(int offset)
	{
		Util.checkRange(offset, 0, TICKS_PER_REVOLUTION);
		
		absoluteOffset = offset;
	}
	
	@Override
	public void initSendable( SendableBuilder builder )
	{
		builder.setSmartDashboardType("FXEncoder");
    	builder.addBooleanProperty(".controllable", () -> false, null);
	    builder.addDoubleProperty("Position (ticks)", this::get, null);
	    builder.addDoubleProperty("AbsPosition (ticks)", this::getAbsolutePosition, null);
	    builder.addDoubleProperty("RPM", this::getRPM, null);
	    builder.addDoubleProperty("MaxRPM", this::getMaxRPM, null);
	    builder.addDoubleProperty("Velocity(mps)", () -> getVelocity(PIDRateType.velocityMPS), null);
	    builder.addDoubleProperty("MaxVelocity(mps)", () -> getMaxVelocity(PIDRateType.velocityMPS), null);
	}
}
