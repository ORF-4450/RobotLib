package Team4450.Lib;

import java.util.EventListener;
import java.util.EventObject;
import java.util.function.DoubleSupplier;

import com.kauailabs.navx.frc.AHRS;

import Team4450.Lib.Wpilib.PIDSource;
import Team4450.Lib.Wpilib.PIDSourceType;
import Team4450.Lib.Wpilib.Sendable;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.AnalogGyro;
//import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Notifier;
//import edu.wpi.first.wpilibj.PIDSource;
//import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SendableRegistry;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Wrapper class for NavX MXP navigation sensor board.
 */

public class NavX implements Sendable, PIDSource, DoubleSupplier
{
	private static NavX		navx;
	private AHRS			ahrs;
	private double 			totalAngle = 0, targetHeading = 0;
	private double			yawResetDelay = .175;
	//private String			name = "NavX", subSystem = "Ungrouped";
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private PIDDispType		pidDispType = PIDDispType.getYaw;
	private double			lastLinearAccelX = 0.0, lastLinearAccelY = 0.0, collisionThreshold = 0.0;
	private double			eventMonitoringInterval = 1.0;
	private boolean			eventMonitoringEnabled =  false;
	
	private NavXEventListener	eventListener;

	private final eventMonitor	runnable = new eventMonitor();
	private final Notifier 		eventNotifier = new Notifier(runnable);
	
	private AnalogGyro			simGyro;
	private double				simGyroResetAngle;
	
	/**
	 * Identifies port the NavX is plugged into
	 */
	public enum PortType {SPI, I2C, I2C_MXP, USB};
	
	/**
	 * Specifies pin type when accessing NavX pins.
	 */
	public enum PinType {DigitalIO, PWM, AnalogIn, AnalogOut};
	    
	private final int MAX_NAVX_MXP_DIGIO_PIN_NUMBER      = 9;
	private final int MAX_NAVX_MXP_ANALOGIN_PIN_NUMBER   = 3;
	private final int MAX_NAVX_MXP_ANALOGOUT_PIN_NUMBER  = 1;
	private final int NUM_ROBORIO_ONBOARD_DIGIO_PINS     = 10;
	private final int NUM_ROBORIO_ONBOARD_PWM_PINS       = 10;
	private final int NUM_ROBORIO_ONBOARD_ANALOGIN_PINS  = 4;
	    
	private NavX(PortType portType)
	{
		Util.consoleLog();
		
		// NavX is plugged into the RoboRio MXP port.
		
		switch (portType)
		{
			case SPI:
				ahrs = new AHRS(SPI.Port.kMXP);
				break;
				
			case I2C:
				ahrs = new AHRS(I2C.Port.kOnboard);
				break;

			case I2C_MXP:
				ahrs = new AHRS(I2C.Port.kMXP);
				break;

			case USB:
				ahrs = new AHRS(SerialPort.Port.kUSB);
				
				Timer.delay(1);	// delay to ensure USB port is opened.
				
				break;
				
			default:
				ahrs = new AHRS(SPI.Port.kMXP);
		}
		
		//ahrs.enableBoardlevelYawReset(true);
		
		registerSendable("NavX");
	}
	
	/**
	 * Return global instance of NavX object. First call creates the NavX global
	 * object and starts the calibration process. Calibration can take up 10 seconds.
	 * Uses SPI PortType to access the NavX.
	 * @return NavX object reference.
	 */
	public static NavX getInstance()
	{
		Util.consoleLog();
		
		if (navx == null) navx = new NavX(PortType.SPI);
		
		return navx;
	}
	
	/**
	 * Return global instance of NavX object. First call creates the NavX global
	 * object and starts the calibration process. Calibration can take up 10 seconds.
	 * @param portType Specify the interface port to be used to access the NavX.
	 * @return NavX object reference.
	 */
	public static NavX getInstance(PortType portType)
	{
		Util.consoleLog();
		
		if (navx == null) navx = new NavX(portType);
		
		return navx;
	}
	
	/**
	 * Return global instance of NavX AHRS object. AHRS is
	 * Attitude/Heading Reference System. AHRS object is the
	 * NavX library and provides access to all NavX methods and
	 * properties.
	 * @return AHRS object reference.
	 */
	public AHRS getAHRS()
	{
		return ahrs;
	}
	
	/**
	 * Returns the firmware version of the NavX.
	 * @return The current firmware version in x.x format.
	 */
	public String getFirmwareVersion()
	{
		return ahrs.getFirmwareVersion();
	}
	
	/**
	 * Return yaw angle from zero point, which is direction robot was pointing when
	 * resetYaw() last called. Uses Navx internal function which is updated on a
	 * timed basis (update rate). There can be a delay between calling resetYaw()
	 * and getting a zero returned from getYaw(), as much as 150ms or more.
	 * Note: Under simulation this function will not return a correct value after
	 * yaw reset until after the first pass through the simulationPeriodic() 
	 * function of the robot code.
	 * @return Yaw angle in degrees -180 to +180, - is left of zero, + is right (clockwise) .
	 */
	public float getYaw()
	{
		if  (simGyro == null)
			return ahrs.getYaw();
		else
			return (float) (simGyro.getAngle() - simGyroResetAngle);
	}
	
	/**
	 * Return the result of getYaw() in radians.
	 * @return Current yaw in radians.
	 */
	public double getYawR()
	{
		return Math.toRadians(getYaw());
	}
	
	/**
	 * Return total yaw angle accumulated since last call to resetYaw().
	 * @return Total yaw angle in degrees.
	 */
	public double getTotalYaw()
	{
		if (simGyro == null)
			return ahrs.getAngle();
		else
			return simGyro.getAngle();
	}
	
	/**
	 * Return total yaw angle accumulated since last call to resetYaw().
	 * Note: Rotation2d angle is + for left of zero, - for right. This is
	 * opposite our convention of - for left + for right (clockwise).
	 * @return Total yaw angle in a Rotation2d object.
	 */
	public Rotation2d getTotalYaw2d()
	{
		return Rotation2d.fromDegrees(-getTotalYaw());
	}
	
	/**
	 * Returns Yaw angle between target heading set by setTargetHeading()
	 * and the current robot heading.
	 * @return Yaw angle in degrees (0-180), - is yaw left of target heading, + is yaw right of target.
	 */
	public double getHeadingYaw()
	{
		double	yaw;
		
		//Util.consoleLog();
		
		yaw = getHeading() - targetHeading;
		
		if (yaw >= 180)
			yaw = -(360 - yaw);
		else if (yaw <= -180) 
			yaw = 360 + yaw;
		
		return yaw;
	}
	
	/**
	 * Returns Yaw angle between target heading set by setTargetHeading()
	 * and the current robot heading.
	 * @return Yaw angle in radians, + is yaw left of target heading, - is yaw right of target.
	 */
	public double getHeadingYawR()
	{
		return Math.toRadians(-getHeadingYaw());
	}
	
	/**
	 * Return yaw rate.
	 * @return Yaw rate in degrees/second.
	 */
	public double getYawRate()
	{
		if (simGyro == null)
			return ahrs.getRate();
		else
			return simGyro.getRate();
	}
	
	/**
	 * Return the rotational velocity in degrees/second for selected axis.
	 * @param axis Desired gyro axis.
	 * @return rotational velocity (degrees/second)
	 */
	public double getRotationalVelocity(GyroAxis axis) 
	{
		switch (axis)
		{
			case X:
				return navx.getAHRS().getRawGyroX();
				
			case Y:
				return navx.getAHRS().getRawGyroY();
				
			case Z:
				return navx.getAHRS().getRawGyroZ();
		}
		
		return 0;
	}	

	/**
	 * Return current robot heading (0-359.n) relative to direction robot was
	 * pointed at last heading reset (setHeading). Will return fractional angle.
	 * 1 degree is right of zero (clockwise) and 359 is left (counter clockwise).
	 * Note: Under simulation this function will not return a correct value after
	 * yaw reset until after the first pass through the simulationPeriodic() 
	 * function of the robot code.
	 * @return Robot heading in degrees.
	 */
	public double getHeading()
	{
		double heading;
		
		heading = getTotalYaw() + totalAngle;

		heading = heading - ((int) (heading / 360) * 360);
		
		if (heading < 0) heading += 360;

		//LCD.printLine(9, "angle=%.2f  totangle=%.2f  hdg=%.2f", ahrs.getAngle(), totalAngle, heading);
		
		return heading;
	}
	
	/**
	 * Return current robot heading (0-6.28144 (359.9)) relative to direction robot was
	 * pointed at last heading reset (setHeading). Will return fractional angle.
	 * 1 degree is left of zero (counter clockwise) and 359 is right (clockwise).
	 * This is how headings are done when working with radians.
	 * @return Robot heading in radians.
	 */
	public double getHeadingR()
	{
		return Math.abs(Math.toRadians(360 - getHeading()));
	}
	
	/**
	 * Convert the supplied degree value to radians.
	 * @param degrees Specified degrees value.
	 * @return Degrees value converted to radians.
	 */
	public double getRadians(double degrees)
	{
		// Don't remember why the 360- here...does not seem to make sense.
		//return Math.toRadians(360 - degrees);
		return Math.toRadians(degrees);
	}
	
	/**
	 * Return current robot heading (0-359) relative to direction robot was
	 * pointed at last heading reset (setHeading). 
	 * @return Robot heading in whole degrees.
	 */
	public int getHeadingInt()
	{
		return (int) getHeading();
	}
	
	/**
	 * Return current robot heading (0-359) relative to direction robot was
	 * pointed at last heading reset (setHeading) in Radians.
	 * @return Robot heading in whole radians.
	 */
	public int getHeadingIntR()
	{
		return (int) getHeadingR();
	}
	
	/**
	 * Set heading tracking start angle.
	 * @param heading Heading from 0-359 that is used to set the
	 * internal heading to the direction the robot is pointing relative to
	 * the direction the driver is looking. Typically called at the
	 * start of autonomous per the starting direction of the robot.
	 * 0 degrees is always straight down the field going clockwise
	 * toward 359.
	 */
	public void setHeading(double heading)
	{
		Util.consoleLog("%.2f", heading);
		
		Util.checkRange(heading, 0, 359, "heading");

		totalAngle = heading;
		
		simGyroResetAngle = 0;
	}

	/**
	 * Set target heading for yaw measured from current heading. 
	 * @param heading Target heading 0-359. Cannot be more than 180
	 * degrees from robot current heading.
	 */
	public void setTargetHeading(double heading)
	{
		//double	gap;
		
		Util.checkRange(heading, 0, 359.9999, "target heading");
		
//		gap = getHeading() - heading;
//		
//		if (Math.abs(gap) > 180)
//			if (360 - gap > 180) throw new IllegalArgumentException("target heading > 180 from current heading");
			
		targetHeading = heading;
	}
	
	/**
	 * Returns connected state of NavX device.
	 * @return True if connected.
	 */
	public boolean isConnected()
	{
		return ahrs.isConnected();
	}
	
	/**
	 * Returns calibrating state of Navx device.
	 * @return True if calibrating.
	 */
	public boolean isCalibrating()
	{
		return ahrs.isCalibrating();
	}
	
	/**
	 * Return moving state of NavX device.
	 * @return True if moving.
	 */
	public boolean isMoving()
	{
		return ahrs.isMoving();
	}
	
	/**
	 * Return rotating state of NavX device.
	 * @return True if rotating.
	 */
	public boolean isRotating()
	{
		return ahrs.isRotating();
	}
	
	/**
	 * Reset yaw zero reference to current direction the robot
	 * is pointing. getYaw may not return zero immediately after
	 * calling this function as zeroYaw takes a varying amount of time
	 * to be reflected in getYaw return. Manufacturer suggests it could
	 * be up to 150ms but we have observed 300ms in some cases and might
	 * be longer still if robot is moving when this call is made.
	 */
	public void resetYaw()
	{
		if (simGyro == null)
		{
			totalAngle += ahrs.getAngle();
			
			ahrs.zeroYaw();
		}
		else
		{
			// We track the current angle as representing zero yaw because even though
			// we reset the simGyro here, the AnalogGyroSim class in the robot code
			// will stuff its idea of the accumulated gyro angle right back into the
			// simGryo. This is a quirk of the simulation design. 
			simGyroResetAngle = simGyro.getAngle();			
			
			simGyro.reset();
		}

		Util.consoleLog("yaw=%.2f hdg=%.2f", getYaw(), getHeading());
	}	
	
	/**
	 * Reset yaw zero reference to current direction the robot
	 * is pointing and wait for reset to complete. Set wait time
	 * with setYawResetWait(). Defaults to 175ms.
	 */
	public void resetYawWait()
	{
		Util.consoleLog();
		
		resetYaw();
		
        Timer.delay(yawResetDelay);
	}	
	
	/**
	 * Reset yaw zero reference to current direction the robot
	 * is pointing and wait the specified time for reset to complete.
	 * @param wait Wait time in milliseconds (0-2000).
	 */
	public void resetYawWait(double wait)
	{
		Util.consoleLog();
		
		Util.checkRange(wait, 0, 2000, "Yaw wait");

		resetYaw();
		
        Timer.delay(wait);
	}
	
	/**
	 * Set yaw reset wait time.
	 * @param wait Wait time in milliseconds (0-2000).
	 */
	public void setYawResetWait(double wait)
	{
		Util.consoleLog();
		
		Util.checkRange(wait, 0, 2000, "Yaw wait");
		
		yawResetDelay = wait;
	}
	
	/**
	 * Reset yaw zero reference to current direction the robot
	 * is pointing and wait for reset to complete. Wait is determined
	 * by watching getYaw to return a value 0 to tolerance (degrees).
	 * Checked every 10ms or until wait milliseconds is reached.
	 * @param tolerance Tolerance (degrees) to determine reset complete (0-10).
	 * @param wait Number of milliseconds to wait (10-5000ms resolution). Should be evenly
	 * divisible by 10.
	 */
	public void resetYawWait(double tolerance, int wait)
	{
		Util.consoleLog("t=%.1f w=%d", tolerance, wait);
		
		int		waits = 0, waitCount;
		
		Util.checkRange(tolerance, 0, 10, "Tolerance");
		Util.checkRange(wait, 10, 5000, "wait ms");
		
		waitCount = wait / 10;
		
		resetYaw();
		
		while (!Util.checkRange(Math.abs(ahrs.getYaw()), 0, tolerance) && waitCount > 0)
		{
			Timer.delay(.010);
			waits++;
			waitCount--;
		}
		
		Util.consoleLog("wait=%dms  yaw=%.2f", waits * 10, ahrs.getYaw());
	}
	
	/**
	 * Return RoboRio channel number for a NavX pin. Not for NavX-Mini.
	 * @param type PinType of pin to get channel for.
	 * @param io_pin_number Pin number to get channel for.
	 * @return RoboRio channel number.
	 * @throws IllegalArgumentException
	 */
	public int getChannelFromPin( PinType type, int io_pin_number ) throws IllegalArgumentException 
	{
        int roborio_channel = 0;
    
        if ( io_pin_number < 0 ) throw new IllegalArgumentException("Error:  navX-MXP I/O Pin #");

        switch ( type ) 
        {
            case DigitalIO:
                if ( io_pin_number > MAX_NAVX_MXP_DIGIO_PIN_NUMBER ) 
                    throw new IllegalArgumentException("Error:  Invalid navX-MXP Digital I/O Pin #");

                roborio_channel = io_pin_number + NUM_ROBORIO_ONBOARD_DIGIO_PINS + (io_pin_number > 3 ? 4 : 0);
                
                break;
                
            case PWM:
                if ( io_pin_number > MAX_NAVX_MXP_DIGIO_PIN_NUMBER ) 
                    throw new IllegalArgumentException("Error:  Invalid navX-MXP Digital I/O Pin #");
                
                roborio_channel = io_pin_number + NUM_ROBORIO_ONBOARD_PWM_PINS;
                
                break;
                
            case AnalogIn:
                if ( io_pin_number > MAX_NAVX_MXP_ANALOGIN_PIN_NUMBER ) 
                    throw new IllegalArgumentException("Error:  Invalid navX-MXP Analog Input Pin #");
                
                roborio_channel = io_pin_number + NUM_ROBORIO_ONBOARD_ANALOGIN_PINS;
                
                break;
                
            case AnalogOut:
                if ( io_pin_number > MAX_NAVX_MXP_ANALOGOUT_PIN_NUMBER ) 
                    throw new IllegalArgumentException("Error:  Invalid navX-MXP Analog Output Pin #");
                
                roborio_channel = io_pin_number;            

                break;
        }
       
        return roborio_channel;
    }
	   
	/**
	 * Dump all NavX AHRS properties current values to Network Tables
	 * for debugging.
	 */
	public void dumpValuesToNetworkTables()
	{
		NetworkTableInstance instance = NetworkTableInstance.getDefault();
		NetworkTable table = instance.getTable("NavX-Dump");
		
		table.getEntry(    "IMU_Connected")       .setBoolean(ahrs.isConnected());
		if (!ahrs.isConnected()) return;

        /* Sensor Board Information                                                 */
        table.getEntry(    "IMU_FirmwareVersion") .setString( ahrs.getFirmwareVersion());
        table.getEntry(    "IMU_UpdateRate(hz)")  .setNumber( ahrs.getActualUpdateRate());
        table.getEntry(    "IMU_BoardLevelYawReset").setBoolean( ahrs.isBoardlevelYawResetEnabled());

        table.getEntry(    "IMU_IsCalibrating")   .setBoolean(ahrs.isCalibrating());
        table.getEntry(    "IMU_Yaw")             .setNumber( ahrs.getYaw());
        table.getEntry(    "IMU_Pitch")           .setNumber( ahrs.getPitch());
        table.getEntry(    "IMU_Roll")            .setNumber( ahrs.getRoll());
                
        /* Display tilt-corrected, Magnetometer-based heading (requires             */
        /* magnetometer calibration to be useful)                                   */
                
        table.getEntry(    "IMU_CompassHeading")  .setNumber( ahrs.getCompassHeading());
             
        /* Display 9-axis Heading (requires magnetometer calibration to be useful)  */
        table.getEntry(    "IMU_FusedHeading")    .setNumber( ahrs.getFusedHeading());
 
        /* These functions are compatible w/the WPI Gyro Class, providing a simple  */
        /* path for upgrading from the Kit-of-Parts gyro to the navx MXP            */
        
        table.getEntry(    "IMU_TotalYaw")        .setNumber( ahrs.getAngle());
        table.getEntry(    "IMU_AngleAdjustment") .setNumber( ahrs.getAngleAdjustment());
        table.getEntry(    "IMU_YawRateDPS")      .setNumber( ahrs.getRate());

        /* Display Processed Acceleration Data (Linear Acceleration, Motion Detect) */
        
        table.getEntry(    "IMU_Accel_X")         .setNumber( ahrs.getWorldLinearAccelX());
        table.getEntry(    "IMU_Accel_Y")         .setNumber( ahrs.getWorldLinearAccelY());
        table.getEntry(    "IMU_IsMoving")        .setBoolean(ahrs.isMoving());
        table.getEntry(    "IMU_IsRotating")      .setBoolean(ahrs.isRotating());

        /* Display estimates of velocity/displacement.  Note that these values are  */
        /* not expected to be accurate enough for estimating robot position on a    */
        /* FIRST FRC Robotics Field, due to accelerometer noise and the compounding */
        /* of these errors due to single (velocity) integration and especially      */
        /* double (displacement) integration.                                       */
        
        table.getEntry(    "IMU_Velocity_X")      .setNumber( ahrs.getVelocityX());
        table.getEntry(    "IMU_Velocity_Y")      .setNumber( ahrs.getVelocityY());
        table.getEntry(    "IMU_Displacement_X")  .setNumber( ahrs.getDisplacementX());
        table.getEntry(    "IMU_Displacement_Y")  .setNumber( ahrs.getDisplacementY());
        
        /* Display Raw Gyro/Accelerometer/Magnetometer Values                       */
        /* NOTE:  These values are not normally necessary, but are made available   */
        /* for advanced users.  Before using this data, please consider whether     */
        /* the processed data (see above) will suit your needs.                     */
        
        table.getEntry(    "IMU_RawGyro_X")       .setNumber( ahrs.getRawGyroX());
        table.getEntry(    "IMU_RawGyro_Y")       .setNumber( ahrs.getRawGyroY());
        table.getEntry(    "IMU_RawGyro_Z")       .setNumber( ahrs.getRawGyroZ());
        table.getEntry(    "IMU_RawAccel_X")      .setNumber( ahrs.getRawAccelX());
        table.getEntry(    "IMU_RawAccel_Y")      .setNumber( ahrs.getRawAccelY());
        table.getEntry(    "IMU_RawAccel_Z")      .setNumber( ahrs.getRawAccelZ());
        table.getEntry(    "IMU_RawMag_X")        .setNumber( ahrs.getRawMagX());
        table.getEntry(    "IMU_RawMag_Y")        .setNumber( ahrs.getRawMagY());
        table.getEntry(    "IMU_RawMag_Z")        .setNumber( ahrs.getRawMagZ());
        table.getEntry(    "IMU_Temp_C")          .setNumber( ahrs.getTempC());
        table.getEntry(    "IMU_Timestamp")       .setNumber( ahrs.getLastSensorTimestamp());
        
        /* Omnimount Yaw Axis Information                                           */
        /* For more info, see http://navx-mxp.kauailabs.com/installation/omnimount  */
        AHRS.BoardYawAxis yaw_axis = ahrs.getBoardYawAxis();
        table.getEntry(    "IMU_YawAxisDirection").setString( yaw_axis.up ? "Up" : "Down" );
        table.getEntry(    "IMU_YawAxis")         .setNumber( yaw_axis.board_axis.getValue() );
        
        /* Quaternion Data                                                          */
        /* Quaternions are fascinating, and are the most compact representation of  */
        /* orientation data.  All of the Yaw, Pitch and Roll Values can be derived  */
        /* from the Quaternions.  If interested in motion processing, knowledge of  */
        /* Quaternions is highly recommended.                                       */
        table.getEntry(    "IMU_QuaternionW")     .setNumber( ahrs.getQuaternionW());
        table.getEntry(    "IMU_QuaternionX")     .setNumber( ahrs.getQuaternionX());
        table.getEntry(    "IMU_QuaternionY")     .setNumber( ahrs.getQuaternionY());
        table.getEntry(    "IMU_QuaternionZ")     .setNumber( ahrs.getQuaternionZ());
         
        /* Connectivity Debugging Support                                           */
        table.getEntry(    "IMU_Byte_Count")      .setNumber( ahrs.getByteCount());
        table.getEntry(    "IMU_Update_Count")    .setNumber( ahrs.getUpdateCount());
	}

	@Override
	public void initSendable( SendableBuilder builder )
	{
		builder.setSmartDashboardType("Gyro");
	    builder.addDoubleProperty("Value", this::getHeadingInt, null);
	}

	/**
	 * When using PID Source Type of displacement, selects which displacement 
	 * measurement function to use.
	 */
	public enum PIDDispType
	{
		getYaw,
		getTotalYaw,
		getHeadingYaw,
		getHeading,
		getHeadingR,
		getHeadingInt,
		getHeadingIntR
	}

	/**
	 * Selects gyro axis to use in various axis based methods.
	 */
	public enum GyroAxis
	{
		X,
		Y,
		Z
	}
	
	/**
	 * Set the PID displacement function to use when doing displacement PID.
	 * @param dispType The function used to send data to PID controllers. Defaults
	 * to getYaw.
	 */
	public void setPIDDispType( PIDDispType dispType )
	{
		pidDispType = dispType;
	}

	/**
	 * Return the current PIDDispType function.
	 * @return The PIDDispType.
	 */
	public PIDDispType getPIDDispType()
	{
		return pidDispType;
	}

	/**
	 * Sets the pid source type to be used for pidGet() calls by PID controllers. When
	 * selecting displacement, be sure to set the PIDDispType to use.
	 * @param pidSource The pid source type (displacement(default)/rate of yaw change deg/sec).
	 */
	@Override
	public void setPIDSourceType( PIDSourceType pidSource )
	{
		this.pidSourceType = pidSource;
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
	 * Returns the current displacement (yaw or heading) or rate of change to PID
	 * controllers per the PIDSourceType setting.
	 * @return The current displacement(yaw/heading) or rate of yaw change.
	 */
	@Override
	public double pidGet()
	{
		if (pidSourceType == PIDSourceType.kDisplacement)
			return getYawRate();
		else
			switch (pidDispType)
			{
				case getYaw:
					return getYaw();

				case getTotalYaw:
					return getTotalYaw();
					
				case getHeadingYaw:
					return getHeadingYaw();
				
				case getHeading:
					return getHeading();
					
				case getHeadingR:
					return getHeadingR();
					
				case getHeadingInt:
					return getHeadingInt();
					
				case getHeadingIntR:
					getHeadingIntR();
					
				default:
					return getYaw();
			}
	}
	
	/**
	 * Turn event monitoring on/off.
	 * @param enabled True to monitor events, false to stop.
	 */
	public void enableEventMonitoring(boolean enabled)
	{
		eventMonitoringEnabled = enabled;
		
		if  (enabled)
			eventNotifier.startPeriodic(eventMonitoringInterval);
		else
			eventNotifier.stop();
	}
	
	/**
	 * Return status of event monitoring function.
	 * @return True if event monitoring is enabled.
	 */
	public boolean isEventMonitoringEnabled()
	{
		return eventMonitoringEnabled;
	}
	
	/**
	 * Set the time interval at which the event monitoring loop will run.
	 * @param seconds Number of seconds between each run of the event monitoring
	 * loop.
	 */
	public void setEventMonitoringInterval(double seconds)
	{
		eventMonitoringInterval = seconds;
	}
	
	// Runs each event monitoring time interval triggered by the notifier. Checks
	// for events.
	
	private class eventMonitor implements Runnable
	{
		public final void run()
		{
			if (collisionThreshold != 0) detectCollision(collisionThreshold);
		}
	}
	
	/**
	 * Set g force threshold to detect a collision. Set to zero to turn off
	 * collision detection. Collision detect raises a NavXEvent if listener
	 * defined and event monitoring is enabled. Monitoring interval should be
	 * 50ms or less for detection to work reliably.
	 * @param g The collision acceleration threshold in g (gravity). Zero to
	 * turn off collision detection.
	 */
	public void setCollisionThreshold(double g)
	{
		collisionThreshold = g;
	}
	
	/**
	 * Perform collision detection, raise event if jerk acceleration exceeds the
	 * threshold.
	 * @param g Detection threshold in G (gravity).
	 */
	private void detectCollision(double g)
	{
        double currLinearAccelX = ahrs.getWorldLinearAccelX();
        double currentJerkX = currLinearAccelX - lastLinearAccelX;
        lastLinearAccelX = currLinearAccelX;
        
        double currLinearAccelY = ahrs.getWorldLinearAccelY();
        double currentJerkY = currLinearAccelY - lastLinearAccelY;
        lastLinearAccelY = currLinearAccelY;
        
        if ( Math.abs(currentJerkX) > g ) 
        	notifyEventListener(NavXEventType.collisionDetected, currentJerkX);
        else if ( Math.abs(currentJerkY) > g ) 
        	notifyEventListener(NavXEventType.collisionDetected, currentJerkY);
	}
	
	// Event Handling classes.
	
	/**
	 * NavX event type enumeration
	 */
	public enum NavXEventType
	{
		collisionDetected;
	}
	
	/**
	 *  Event description class returned to event handler.
	 */
    public class NavXEvent extends EventObject 
    {
		private static final long serialVersionUID = 1L;

    	public NavXEventType	eventType;
		public Object			eventData;
		
		public NavXEvent(Object source, NavXEventType eventType, Object eventData) 
		{
            super(source);
            this.eventType = eventType;
            this.eventData = eventData;
        }
    }
    
    /**
     *  Java Interface definition for event listener.
     */
    public interface NavXEventListener extends EventListener 
    {
        public void event(NavXEvent navXEvent);
    }
    
    /**
     * Register a NavXEventListener object instance to receive events.
     * @param listener NavXEventListener object instance to receive events.
     */
    public void setNavXEventListener(NavXEventListener listener) 
    {
        this.eventListener = listener;
    }
    
    private void notifyEventListener(NavXEventType eventType, Object eventData) 
    {
        if (eventListener != null) eventListener.event(new NavXEvent(this, eventType, eventData));
    }

	/**
	 * Returns a double when this class is used as a DoubleSupplier.
	 * @return The current yaw value, same as getYaw() method.
	 */
	@Override
	public double getAsDouble()
	{
		return getYaw();
	}
	
	public void setSimGyro(AnalogGyro gyro)
	{
		simGyro = gyro;
	}
}
