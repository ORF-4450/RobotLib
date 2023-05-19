package Team4450.Lib.Swerve.ctre;

import com.ctre.phoenix.sensors.AbsoluteSensorRange;
import com.ctre.phoenix.sensors.WPI_CANCoder;
import com.ctre.phoenix.sensors.CANCoderConfiguration;
import com.ctre.phoenix.sensors.CANCoderStatusFrame;

import Team4450.Lib.Util;
import Team4450.Lib.Swerve.AbsoluteEncoder;
import Team4450.Lib.Swerve.AbsoluteEncoderFactory;
import edu.wpi.first.util.sendable.SendableRegistry;

public class CanCoderFactoryBuilder 
{
    private Direction   direction = Direction.COUNTER_CLOCKWISE;
    private int         periodMilliseconds = 10;

    public CanCoderFactoryBuilder withReadingUpdatePeriod(int periodMilliseconds) 
    {
        this.periodMilliseconds = periodMilliseconds;
        return this;
    }

    public CanCoderFactoryBuilder withDirection(Direction direction) 
    {
        this.direction = direction;
        return this;
    }

    public AbsoluteEncoderFactory<CanCoderAbsoluteConfiguration> build() 
    {
        //Util.consoleLog();
    
        return configuration -> {
            CANCoderConfiguration config = new CANCoderConfiguration();

            config.absoluteSensorRange = AbsoluteSensorRange.Unsigned_0_to_360;
            config.magnetOffsetDegrees = Math.toDegrees(configuration.getOffset());
            config.sensorDirection = direction == Direction.CLOCKWISE;
            config.initializationStrategy = configuration.getInitStrategy();

            WPI_CANCoder encoder = new WPI_CANCoder(configuration.getId());
            
            CtreUtils.checkCtreError(encoder.configAllSettings(config, 250), "Failed to configure CANCoder");

            CtreUtils.checkCtreError(encoder.setStatusFramePeriod(CANCoderStatusFrame.SensorData, periodMilliseconds, 
                                     250), "Failed to configure CANCoder update rate");

            SendableRegistry.addLW(encoder, "DriveBase", "CanCoder[" + encoder.getDeviceID() + "]");
            
            return new EncoderImplementation(encoder);
        };
    }

    private static class EncoderImplementation implements AbsoluteEncoder 
    {
        private final WPI_CANCoder  encoder;

        private EncoderImplementation(WPI_CANCoder encoder) 
        {
            //Util.consoleLog();
    
            this.encoder = encoder;
        }

        /**
         * Returns encoder absolute angle (position).
         * @return The absolute angle in radians.
         */
        @Override
        public double getAbsoluteAngle() 
        {
            double angle = Math.toRadians(encoder.getAbsolutePosition());

            // TODO: This call is supposed to be equivalent to above due to configuration settings
            // and faster to return. Need to test.
            //double angle = Math.toRadians(encoder.getPosition());

            CtreUtils.checkCtreError(encoder.getLastError(), "Failed to retrieve CANcoder "
                                    + encoder.getDeviceID() + " absolute position");

            angle %= 2.0 * Math.PI;

            if (angle < 0.0) angle += 2.0 * Math.PI;

            return angle;
        }
    }

    public enum Direction 
    {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }
}
