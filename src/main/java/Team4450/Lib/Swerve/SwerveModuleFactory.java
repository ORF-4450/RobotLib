package Team4450.Lib.Swerve;

import com.revrobotics.REVPhysicsSim;

import Team4450.Lib.Util;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

public class SwerveModuleFactory<DriveConfiguration, SteerConfiguration> 
{
    private final ModuleConfiguration moduleConfiguration;
    private final DriveControllerFactory<?, DriveConfiguration> driveControllerFactory;
    private final SteerControllerFactory<?, SteerConfiguration> steerControllerFactory;

    public SwerveModuleFactory(ModuleConfiguration moduleConfiguration,
                               DriveControllerFactory<?, DriveConfiguration> driveControllerFactory,
                               SteerControllerFactory<?, SteerConfiguration> steerControllerFactory) 
    {
        //Util.consoleLog();
    
        this.moduleConfiguration = moduleConfiguration;
        this.driveControllerFactory = driveControllerFactory;
        this.steerControllerFactory = steerControllerFactory;
    }

    public SwerveModule create(DriveConfiguration driveConfiguration, SteerConfiguration steerConfiguration, 
                               double steerOffset, ModulePosition position) 
    {
        Util.consoleLog("%s", position);
    
        var driveController = driveControllerFactory.create(driveConfiguration, moduleConfiguration);
        var steerController = steerControllerFactory.create(steerConfiguration, moduleConfiguration);

        return new ModuleImplementation(driveController, steerController, steerOffset, position, null);
    }

    public SwerveModule create(ShuffleboardLayout container, DriveConfiguration driveConfiguration, 
                               SteerConfiguration steerConfiguration, double steerOffset,
                               ModulePosition position)
    {
        Util.consoleLog("%s", position);
    
        var driveController = driveControllerFactory.create(
                container,
                driveConfiguration,
                moduleConfiguration
        );

        var steerContainer = steerControllerFactory.create(
                container,
                steerConfiguration,
                moduleConfiguration
        );

        return new ModuleImplementation(driveController, steerContainer, steerOffset, position,
                                        container);
    }

    private static class ModuleImplementation implements SwerveModule 
    {
        private final DriveController driveController;
        private final SteerController steerController;

        private Translation2d         translation2d;
        private double                actualAngleDegrees, steerOffset;
        private Pose2d                pose;
        private ModulePosition        position;
        private ShuffleboardLayout    container;

        private ModuleImplementation(DriveController driveController, SteerController steerController,
                                     double steerOffset, ModulePosition position, ShuffleboardLayout container) 
        {
            //Util.consoleLog("%s", position);
    
            this.driveController = driveController;
            this.steerController = steerController;
            this.steerOffset = steerOffset;
            this.position = position;
            this.container = container;
            
            this.driveController.setPosition(position);
            
            if (RobotBase.isSimulation()) 
            {
            	// Moved to drive controller class.
                // Only Neo sim implemented.
                //REVPhysicsSim.getInstance().addSparkMax(driveController.getMotorNeo(), DCMotor.getNEO(1));

                //driveController.getMotorNeo().getPIDController().setP(1, 3);
            }

            resetSteerAngleToAbsolute();
        }

        @Override
        public double getDriveVelocity() 
        {
            return driveController.getVelocity();
        }

        @Override
        public double getSteerAngle() 
        {
            return steerController.getAngle(); // Radians.
        }

        @Override
        public void set(double driveVoltage, double steerAngle, double velocity) 
        {
            steerAngle %= (2.0 * Math.PI);

            if (steerAngle < 0.0) steerAngle += 2.0 * Math.PI;

            double difference = steerAngle - getSteerAngle();

            // Change the target angle so the difference is in the range [-pi, pi) instead of [0, 2pi)

            if (difference >= Math.PI) 
                steerAngle -= 2.0 * Math.PI;
            else if (difference < -Math.PI) 
                steerAngle += 2.0 * Math.PI;
           
            difference = steerAngle - getSteerAngle(); // Recalculate difference

            // If the difference is greater than 90 deg or less than -90 deg the drive can be inverted so the total
            // movement of the module is less than 90 deg

            if (difference > Math.PI / 2.0 || difference < -Math.PI / 2.0) 
            {
                // Only need to add 180 deg here because the target angle will be put back into the range [0, 2pi)
                steerAngle += Math.PI;
                driveVoltage *= -1.0;
            }

            // Put the target angle back into the range [0, 2pi)

            steerAngle %= (2.0 * Math.PI);

            if (steerAngle < 0.0) steerAngle += 2.0 * Math.PI;

            driveController.setReferenceVoltage(driveVoltage, velocity);
            steerController.setReferenceAngle(steerAngle);

            actualAngleDegrees = Math.toDegrees(steerAngle);
        }

        @Override
        public void stop()
        {
            driveController.stop();
            steerController.stop();
        }

        @Override
        public void setSteerPidConstants(double proportional, double integral, double derivative)
        {
            steerController.setPidConstants(proportional, integral, derivative);
        }

        @Override
        public void setTranslation2d(Translation2d translation) 
        {
            translation2d = translation;            
        }

        @Override
        public Translation2d getTranslation2d() 
        {
            return translation2d;
        }
        
        @Override
        public double getHeadingDegrees() 
        {
            if (RobotBase.isReal())
                return steerController.getMotorEncoder().getPosition();
            else
                return actualAngleDegrees;
        }

        @Override
        public Rotation2d getHeadingRotation2d() 
        {
            return Rotation2d.fromDegrees(getHeadingDegrees());
        }

        @Override
        public void setModulePose(Pose2d pose) 
        {
            this.pose = pose;
        }

        @Override
        public Pose2d getPose()
        {
            return pose;
        }

        @Override     
        public void resetSteerAngleToAbsolute() 
        {
        	// The original SDS code had the steer offset subtracted below. But the steer offset is
        	// configured into the SparkMax for Neos and so subtracting it here is an error. This 
        	// caused this method to set an incorrect angle. So when we tried to use it to reset
        	// after align to start position, it caused the steering to fail. With the offset
        	// removed, things seem to be working much better.
        	// NOTE: It may be that the offset needs to be subtracted here for 500s.
        	
            double angleRad = steerController.getAbsoluteEncoder().getAbsoluteAngle(); // - steerOffset;
            
            Util.consoleLog("%s aa=%.3f  off=%.3f  result=%.3f rad=%.3f", position,
                Math.toDegrees(steerController.getAbsoluteEncoder().getAbsoluteAngle()), 
                Math.toDegrees(steerOffset), Math.toDegrees(angleRad), angleRad);

            steerController.getMotorEncoder().setPosition(angleRad);
        }

        @Override
        public void resetMotorEncoders() 
        {
            //Util.consoleLog("%s", position);
    
            driveController.getEncoder().setPosition(0);
            steerController.getMotorEncoder().setPosition(0);
        }

        @Override
        public ModulePosition getModulePosition() 
        {
            return position;
        }

        @Override
        public double getAbsoluteOffset() 
        {
            return steerOffset;
        }

        @Override
        public void setStartingPosition() 
        {
            Util.consoleLog("offset=%.3fr", steerOffset);
            
            steerController.setStartingPosition(steerOffset);
            
            resetSteerAngleToAbsolute();
        }

        @Override
        public SwerveModulePosition getFieldPosition() 
        {
            return new SwerveModulePosition(driveController.getDistance(), 
                                            new Rotation2d(getSteerAngle()));

            // TODO            return new SwerveModulePosition(driveController.getEncoder().getPosition(), 
            //                                            new Rotation2d(getSteerAngle()));
            //new Rotation2d(steerController.getMotorEncoder().getPosition()));
        }

		@Override
		public void setBrakeMode(boolean on) 
		{
			driveController.setBrakeMode(on);
		}
    }
} 
