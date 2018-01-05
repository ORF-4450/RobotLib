package Team4450.Lib;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

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
	 * @return Yaw angle in degrees 0->180, - is left of zero, + is right.
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
	 * @throws IllegalArgumentException.
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
		NetworkTable table = NetworkTable.getTable("NavX");
		
        table.putBoolean(  "IMU_Connected",        ahrs.isConnected());
        if (!ahrs.isConnected()) return;

        /* Sensor Board Information                                                 */
        table.putString(   "IMU_FirmwareVersion",  ahrs.getFirmwareVersion());
        
        table.putBoolean(  "IMU_IsCalibrating",    ahrs.isCalibrating());
        table.putNumber(   "IMU_Yaw",              ahrs.getYaw());
        table.putNumber(   "IMU_Pitch",            ahrs.getPitch());
        table.putNumber(   "IMU_Roll",             ahrs.getRoll());
                
        /* Display tilt-corrected, Magnetometer-based heading (requires             */
        /* magnetometer calibration to be useful)                                   */
                
        table.putNumber(   "IMU_CompassHeading",   ahrs.getCompassHeading());
             
        /* Display 9-axis Heading (requires magnetometer calibration to be useful)  */
        table.putNumber(   "IMU_FusedHeading",     ahrs.getFusedHeading());

        /* These functions are compatible w/the WPI Gyro Class, providing a simple  */
        /* path for upgrading from the Kit-of-Parts gyro to the navx MXP            */
        
        table.putNumber(   "IMU_TotalYaw",         ahrs.getAngle());
        table.putNumber(   "IMU_YawRateDPS",       ahrs.getRate());

        /* Display Processed Acceleration Data (Linear Acceleration, Motion Detect) */
        
        table.putNumber(   "IMU_Accel_X",          ahrs.getWorldLinearAccelX());
        table.putNumber(   "IMU_Accel_Y",          ahrs.getWorldLinearAccelY());
        table.putBoolean(  "IMU_IsMoving",         ahrs.isMoving());
        table.putBoolean(  "IMU_IsRotating",       ahrs.isRotating());

        /* Display estimates of velocity/displacement.  Note that these values are  */
        /* not expected to be accurate enough for estimating robot position on a    */
        /* FIRST FRC Robotics Field, due to accelerometer noise and the compounding */
        /* of these errors due to single (velocity) integration and especially      */
        /* double (displacement) integration.                                       */
        
        table.putNumber(   "IMU_Velocity_X",           ahrs.getVelocityX());
        table.putNumber(   "IMU_Velocity_Y",           ahrs.getVelocityY());
        table.putNumber(   "IMU_Displacement_X",       ahrs.getDisplacementX());
        table.putNumber(   "IMU_Displacement_Y",       ahrs.getDisplacementY());
        
        /* Display Raw Gyro/Accelerometer/Magnetometer Values                       */
        /* NOTE:  These values are not normally necessary, but are made available   */
        /* for advanced users.  Before using this data, please consider whether     */
        /* the processed data (see above) will suit your needs.                     */
        
        table.putNumber(   "IMU_RawGyro_X",        ahrs.getRawGyroX());
        table.putNumber(   "IMU_RawGyro_Y",        ahrs.getRawGyroY());
        table.putNumber(   "IMU_RawGyro_Z",        ahrs.getRawGyroZ());
        table.putNumber(   "IMU_RawAccel_X",       ahrs.getRawAccelX());
        table.putNumber(   "IMU_RawAccel_Y",       ahrs.getRawAccelY());
        table.putNumber(   "IMU_RawAccel_Z",       ahrs.getRawAccelZ());
        table.putNumber(   "IMU_RawMag_X",         ahrs.getRawMagX());
        table.putNumber(   "IMU_RawMag_Y",         ahrs.getRawMagY());
        table.putNumber(   "IMU_RawMag_Z",         ahrs.getRawMagZ());
        table.putNumber(   "IMU_Temp_C",           ahrs.getTempC());
        table.putNumber(   "IMU_Timestamp",        ahrs.getLastSensorTimestamp());
        
        /* Omnimount Yaw Axis Information                                           */
        /* For more info, see http://navx-mxp.kauailabs.com/installation/omnimount  */
        AHRS.BoardYawAxis yaw_axis = ahrs.getBoardYawAxis();
        table.putString(   "IMU_YawAxisDirection", yaw_axis.up ? "Up" : "Down" );
        table.putNumber(   "IMU_YawAxis",          yaw_axis.board_axis.getValue() );
        
        /* Quaternion Data                                                          */
        /* Quaternions are fascinating, and are the most compact representation of  */
        /* orientation data.  All of the Yaw, Pitch and Roll Values can be derived  */
        /* from the Quaternions.  If interested in motion processing, knowledge of  */
        /* Quaternions is highly recommended.                                       */
        table.putNumber(   "IMU_QuaternionW",      ahrs.getQuaternionW());
        table.putNumber(   "IMU_QuaternionX",      ahrs.getQuaternionX());
        table.putNumber(   "IMU_QuaternionY",      ahrs.getQuaternionY());
        table.putNumber(   "IMU_QuaternionZ",      ahrs.getQuaternionZ());
        
        /* Connectivity Debugging Support                                           */
        table.putNumber(   "IMU_Byte_Count",       ahrs.getByteCount());
        table.putNumber(   "IMU_Update_Count",     ahrs.getUpdateCount());
	}
}
