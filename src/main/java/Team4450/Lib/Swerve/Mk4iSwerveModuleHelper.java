package Team4450.Lib.Swerve;

import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;
import Team4450.Lib.Swerve.ctre.*;
import Team4450.Lib.Swerve.rev.NeoDriveControllerFactoryBuilder;
import Team4450.Lib.Swerve.rev.NeoSteerConfiguration;
import Team4450.Lib.Swerve.rev.NeoSteerControllerFactoryBuilder;
import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;

public final class Mk4iSwerveModuleHelper 
{
    private Mk4iSwerveModuleHelper() {
    }

    private static DriveControllerFactory<?, Integer> getFalcon500DriveFactory(Mk4ModuleConfiguration configuration) 
    {
        return new Falcon500DriveControllerFactoryBuilder()
                .withVoltageCompensation(configuration.getNominalDriveVoltage())
                .withCurrentLimit(configuration.getDriveCurrentLimit())
                .withRampRate(configuration.getDriveRampRate())
                .build();
    }

    private static SteerControllerFactory<?, Falcon500SteerConfiguration<CanCoderAbsoluteConfiguration>> getFalcon500SteerFactory(Mk4ModuleConfiguration configuration) 
    {
        return new Falcon500SteerControllerFactoryBuilder()
                .withVoltageCompensation(configuration.getNominalSteerVoltage())
                .withPidConstants(configuration.getSteerP(), configuration.getSteerI(), configuration.getSteerD())
                .withCurrentLimit(configuration.getSteerCurrentLimit())
                .withRampRate(configuration.getSteerRampRate())
                .build(new CanCoderFactoryBuilder()
                        .withReadingUpdatePeriod(15)
                        .build());
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

    private static SteerControllerFactory<?, NeoSteerConfiguration<CanCoderAbsoluteConfiguration>> getNeoSteerFactory(Mk4ModuleConfiguration configuration) 
    {
        //Util.consoleLog();
    
        return new NeoSteerControllerFactoryBuilder()
                .withVoltageCompensation(configuration.getNominalSteerVoltage())
                .withPidConstants(configuration.getSteerP(), configuration.getSteerI(), configuration.getSteerD())
                .withCurrentLimit(configuration.getSteerCurrentLimit())
                .withRampRate(configuration.getSteerRampRate())
                .build(new CanCoderFactoryBuilder()
                        .withReadingUpdatePeriod(15)
                        .build());
    }

    /**
     * Creates a Mk4i swerve module that uses Falcon 500s for driving and steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500(
            ModulePosition position,
            ShuffleboardLayout container,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
   {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getFalcon500DriveFactory(configuration),
                getFalcon500SteerFactory(configuration)
        ).create(
                container,
                driveMotorPort,
                new Falcon500SteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses Falcon 500s for driving and steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500(
            ModulePosition position,
            ShuffleboardLayout container,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset)
    {
        return createFalcon500(
            position,
            container, 
            Mk4ModuleConfiguration.getDefault500Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses Falcon 500s for driving and steering.
     *
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500(
            ModulePosition position,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getFalcon500DriveFactory(configuration),
                getFalcon500SteerFactory(configuration)
        ).create(
                driveMotorPort,
                new Falcon500SteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses Falcon 500s for driving and steering.
     *
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500(
            ModulePosition position,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return createFalcon500(
            position,
            Mk4ModuleConfiguration.getDefault500Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses NEOs for driving and steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo(
            ModulePosition position,
            ShuffleboardLayout container,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset)
    {
        //Util.consoleLog();
    
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getNeoSteerFactory(configuration)
        ).create(
                container,
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses NEOs for driving and steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo(
            ModulePosition position,
            ShuffleboardLayout container,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        //Util.consoleLog();
    
        return createNeo(
            position, 
            container, 
            Mk4ModuleConfiguration.getDefaultNeoConfig(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses NEOs for driving and steering.
     *
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo(
            ModulePosition position,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        //Util.consoleLog();
    
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getNeoSteerFactory(configuration)
        ).create(
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses NEOs for driving and steering.
     *
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeo(
            ModulePosition position,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        //Util.consoleLog();
    
        return createNeo(
            position, 
            Mk4ModuleConfiguration.getDefaultNeoConfig(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses a Falcon 500 for driving and a NEO for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500Neo(
            ModulePosition position,
            ShuffleboardLayout container,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getFalcon500DriveFactory(configuration),
                getNeoSteerFactory(configuration)
        ).create(
                container,
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses a Falcon 500 for driving and a NEO for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500Neo(
            ModulePosition position,
            ShuffleboardLayout container,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return createFalcon500Neo(
            position,
            container, 
            Mk4ModuleConfiguration.getDefaultNeoConfig(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses a Falcon 500 for driving and a NEO for steering.
     *
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500Neo(
            ModulePosition position,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getFalcon500DriveFactory(configuration),
                getNeoSteerFactory(configuration)
        ).create(
                driveMotorPort,
                new NeoSteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses a Falcon 500 for driving and a NEO for steering.
     *
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive Falcon 500.
     * @param steerMotorPort   The CAN ID of the steer NEO.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createFalcon500Neo(
            ModulePosition position,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return createFalcon500Neo(
            position,
            Mk4ModuleConfiguration.getDefaultNeoConfig(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses a NEO for driving and a Falcon 500 for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeoFalcon500(
            ModulePosition position,
            ShuffleboardLayout container,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getFalcon500SteerFactory(configuration)
        ).create(
                container,
                driveMotorPort,
                new Falcon500SteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses a NEO for driving and a Falcon 500 for steering.
     * Module information is displayed in the specified ShuffleBoard container.
     *
     * @param container        The container to display module information in.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeoFalcon500(
         ModulePosition position,
            ShuffleboardLayout container,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return createNeoFalcon500(
            position,
            container, 
            Mk4ModuleConfiguration.getDefault500Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    /**
     * Creates a Mk4i swerve module that uses a NEO for driving and a Falcon 500 for steering.
     *
     * @param configuration    Module configuration parameters to use.
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeoFalcon500(
            ModulePosition position,
            Mk4ModuleConfiguration configuration,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return new SwerveModuleFactory<>(
                gearRatio.getConfiguration(),
                getNeoDriveFactory(configuration),
                getFalcon500SteerFactory(configuration)
        ).create(
                driveMotorPort,
                new Falcon500SteerConfiguration<>(
                        steerMotorPort,
                        new CanCoderAbsoluteConfiguration(steerEncoderPort, steerOffset)
                ),
                steerOffset,
                position
        );
    }

    /**
     * Creates a Mk4i swerve module that uses a NEO for driving and a Falcon 500 for steering.
     *
     * @param gearRatio        The gearing configuration the module is in.
     * @param driveMotorPort   The CAN ID of the drive NEO.
     * @param steerMotorPort   The CAN ID of the steer Falcon 500.
     * @param steerEncoderPort The CAN ID of the steer CANCoder.
     * @param steerOffset      The offset of the CANCoder in radians.
     * @return The configured swerve module.
     */
    public static SwerveModule createNeoFalcon500(
            ModulePosition position,
            GearRatio gearRatio,
            int driveMotorPort,
            int steerMotorPort,
            int steerEncoderPort,
            double steerOffset) 
    {
        return createNeoFalcon500(
            position,
            Mk4ModuleConfiguration.getDefault500Config(), 
            gearRatio, 
            driveMotorPort, 
            steerMotorPort, 
            steerEncoderPort, 
            steerOffset);
    }

    public enum GearRatio 
    {
        L1(SdsModuleConfigurations.MK4I_L1),
        L2(SdsModuleConfigurations.MK4I_L2),
        L3(SdsModuleConfigurations.MK4I_L3);

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
