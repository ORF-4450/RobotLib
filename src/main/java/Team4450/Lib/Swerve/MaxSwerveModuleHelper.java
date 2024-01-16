package Team4450.Lib.Swerve;

import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;
import Team4450.Lib.Swerve.ctre.*;
import Team4450.Lib.Swerve.rev.NeoDriveControllerFactoryBuilder;
import Team4450.Lib.Swerve.rev.NeoSteerConfiguration;
import Team4450.Lib.Swerve.rev.NeoSteerControllerFactoryBuilder;
import Team4450.Lib.Swerve.rev.TBEncoderAbsoluteConfiguration;
import Team4450.Lib.Swerve.rev.TBEncoderFactoryBuilder;
import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;

public final class MaxSwerveModuleHelper 
{
    private MaxSwerveModuleHelper() 
    {
    }

    private static DriveControllerFactory<?, Integer> getNeoDriveFactory(Mk4ModuleConfiguration configuration) 
    {
        //Util.consoleLog();
    
        return new NeoDriveControllerFactoryBuilder()
                .withVoltageCompensation(configuration.getNominalDriveVoltage())
                .withCurrentLimit(configuration.getDriveCurrentLimit())
                .withRampRate(configuration.getDriveRampRate())
                .build();
    }

    private static SteerControllerFactory<?, NeoSteerConfiguration<TBEncoderAbsoluteConfiguration>> getNeo550SteerFactory(Mk4ModuleConfiguration configuration) 
    {
        Util.consoleLog();
    
        return new NeoSteerControllerFactoryBuilder()
                .withVoltageCompensation(configuration.getNominalSteerVoltage())
                .withPidConstants(configuration.getSteerP(), configuration.getSteerI(), configuration.getSteerD())
                .withCurrentLimit(configuration.getSteerCurrentLimit())
                .withRampRate(configuration.getSteerRampRate())
                .build(new TBEncoderFactoryBuilder().build());
    }

     
    /**
     * Creates a Max swerve module that uses NEOs for driving and ENO550s for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param position		   The module location on the robot drive base.
     * @param container        The container to display module information in.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer TBEncoder SparkMax.
     * @param steerOffset      The offset of the TBEncoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo550(
            ModulePosition position,
            ShuffleboardLayout container,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset)
    {
        Util.consoleLog();
    
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getNeo550SteerFactory(configuration)
        ).create(
                container,
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        position,
                        new TBEncoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Max swerve module that uses NEOs for driving and NEO550s for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param position		   The module location on the robot drive base.
     * @param container        The container to display module information in.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer TBEncoder SparkMax.
     * @param steerOffset      The offset of the TBEncoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo550(
            ModulePosition position,
            ShuffleboardLayout container,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        Util.consoleLog();
    
        return createNeo550(
            position, 
            container, 
            Mk4ModuleConfiguration.getDefaultNeo550Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Max swerve module that uses NEOs for driving and NEO550s for steering.
     *
     * @param position		   The module location on the robot drive base.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer TBEncoder SparkMax.
     * @param steerOffset      The offset of the TBEncoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo550(
            ModulePosition position,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        Util.consoleLog();
    
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getNeo550SteerFactory(configuration)
        ).create(
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        position,
                        new TBEncoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Max swerve module that uses NEOs for driving and NEO550s for steering.
     *
     * @param position		   The module location on the robot drive base.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer TBEncoder SparkMax.
     * @param steerOffset      The offset of the TBEncoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo550(
            ModulePosition position,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        Util.consoleLog();
    
        return createNeo550(
            position, 
            Mk4ModuleConfiguration.getDefaultNeo550Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    public enum GearRatio 
    {
    	T12(RevModuleConfigurations.MAXSWERVE_T12),
    	T13(RevModuleConfigurations.MAXSWERVE_T13),
    	T14(RevModuleConfigurations.MAXSWERVE_T14);

        private final ModuleConfiguration configuration;

        GearRatio(ModuleConfiguration configuration) 
        {
            this.configuration = configuration;
        }

        public ModuleConfiguration getConfiguration() 
        {
            return configuration;
        }
    }
}

