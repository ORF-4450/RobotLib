package Team4450.Lib;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Wrapper class for NavX MXP navigation sensor board.
 */

public class NavX
{
	private static NavX		navx;
	private AHRS			ahrs;
	private static double 	totalAngle = 0;

	/**
	 * Identifies port the NavX is plugged into
	 *
	 */

	public enum PortType {SPI, I2C, I2C_MXP, USB};
	
	/**
	 * Specifies pin type when accessing NavX pins.
	 *
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
	 * Return yaw angle from zero point.
	 * @return Yaw angle in degrees 0 to 180, - is left of zero, + is right.
	 */
	public float getYaw()
	{
		return ahrs.getYaw();
	}
	
	/**
	 * Return total yaw angle accumulated since last reset.
	 * @return Total yaw angle in degrees.
	 */
	public double getTotalYaw()
	{
		return ahrs.getAngle();
	}
	
	/**
	 * Return yaw rate.
	 * @return Yaw rate in degrees/second.
	 */
	public double getYawRate()
	{
		return ahrs.getRate();
	}
	

	/**
	 * Return current robot heading (0-360) relative to direction robot was
	 * pointed at last reset (setHeading).
	 * @return Robot heading.
	 */
	public double getHeading()
	{
		double heading;
		
		heading = ahrs.getAngle() + totalAngle;
		
		heading = heading - ((int) (heading / 360) * 360);
		
		if (heading < 0) heading += 360;
		
		return heading;
	}
	
	/**
	 * Set heading tracking angle to offset value.
	 * @param offset Offset from 0-360 that is used to adjust the
	 * heading to the direction the robot is pointing relative to
	 * the direction the driver is looking.
	 */
	public void setHeading(double offset)
	{
		totalAngle = offset;
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
	 * is pointing.
	 */
	public void resetYaw()
	{
		totalAngle += ahrs.getAngle();
		
        ahrs.zeroYaw();
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
		NetworkTable table = instance.getTable("NavX");
		
		table.getEntry(    "IMU_Connected")       .setBoolean(ahrs.isConnected());
		if (!ahrs.isConnected()) return;

        /* Sensor Board Information                                                 */
        table.getEntry(    "IMU_FirmwareVersion") .setString( ahrs.getFirmwareVersion());
        
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
}
