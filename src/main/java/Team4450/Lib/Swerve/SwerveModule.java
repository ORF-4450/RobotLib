package Team4450.Lib.Swerve;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

public interface SwerveModule 
{
	/**
	 * Gets the current velocity of the wheel.
	 * @return Velocity in m/s.
	 */
    double getDriveVelocity();

    /**
     * Gets the last set steering angle.
     * @return The angle in radians.
     */
    double getSteerAngle();

    /**
     * Sets the voltage of the drive motor, angle of the drive wheel and
     * passes the desired velocity of the drive wheel (for sim) to the
     * driving algorithm. This causes the module wheel to move.
     * @param driveVoltage Drive voltage 0-max voltage.
     * @param steerAngle Target angle to rotate the wheel to.
     * @param velocity Target velocity m/s.
     */
    void set(double driveVoltage, double steerAngle, double velocity);

    /**
     * Stops both motors on the module.
     */
    void stop();
        
    /**
     * Set the PID constants of the steering PID controller.
     * @param proportional Proportional factor.
     * @param integral Integral factor.
     * @param derivative Derivative factor.
     */
    void setSteerPidConstants(double proportional, double integral, double derivative);

    /**
     * Set the module translation from the center of robot.
     * @param translation Desired translation.
     */
    void setTranslation2d(Translation2d translation);

    /**
     * Get the module translation from center of robot.
     * @return The translation.
     */
    Translation2d getTranslation2d();
           
    /**
     * Get the module wheel heading.
     * @return Wheel heading in degrees,
     */
    double getHeadingDegrees();

    /**
     * Get the module wheel heading.
     * @return Module wheel heading in a rotation2d.
     */
    Rotation2d getHeadingRotation2d() ;

    /**
     * Set the module pose to a new location.
     * @param pose The desired X,Y location and angle.
     */
    void setModulePose(Pose2d pose) ;

    /**
     * Returns the module pose (location on field).
     * @return Module pose.
     */
    Pose2d getPose();

    /**
     * Set the module steer angle to the angle of the absolute encoder.
     */
    void resetSteerAngleToAbsolute();

    /**
     * Reset the drive and steer encoders of the module. Do not
     * use after driving starts as that will disrupt the swerve code.
     */
    void resetMotorEncoders();

    /**
     * Returns the module mounted position on the robot.
     * @return ModulePosition The module's position.
     */
    ModulePosition getModulePosition();

    /**
     * Get the offset that when added to the absolute encoder zero value
     * yields the straight ahead absolute value.
     * @return The offset in radians.
     */
    double getAbsoluteOffset();

    /**
     * Runs a thread to align the wheel to the initial position, which is
     * straight ahead with bevel drive gear facing left. Takes 2 seconds.
     */
    void setStartingPosition();

    /**
     * Gets the module's position on the field. This is related to the
     * robot's position on the field. Robot and modules move together.
     * Note that this position is not the same as a pose. This position
     * is expressed as distance traveled by the module wheel and current
     * angle the wheel is pointing.
     * @return SwerveModulePosition The module's field position.
     */
    SwerveModulePosition getFieldPosition();
}
