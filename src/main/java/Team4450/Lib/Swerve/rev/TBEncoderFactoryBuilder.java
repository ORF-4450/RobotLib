package Team4450.Lib.Swerve.rev;

import static Team4450.Lib.Swerve.rev.RevUtils.checkNeoError;

import com.revrobotics.SparkMaxAbsoluteEncoder;
import com.revrobotics.SparkMaxAbsoluteEncoder.Type;

import Team4450.Lib.Util;
import Team4450.Lib.Swerve.AbsoluteEncoder;
import Team4450.Lib.Swerve.AbsoluteEncoderFactory;

public class TBEncoderFactoryBuilder 
{

    public AbsoluteEncoderFactory<TBEncoderAbsoluteConfiguration> build() 
    {
        Util.consoleLog();
    
        return configuration -> {
        	
        	SparkMaxAbsoluteEncoder encoder = configuration.getMotor().getAbsoluteEncoder(Type.kDutyCycle);
        	
        	checkNeoError(encoder.setInverted(configuration.getInverted()), "Failed to set encoder inverted");
        	
        	checkNeoError(encoder.setPositionConversionFactor(configuration.getPositionConversionFactor()), 
        			      "Failed to set encoder position conv factor");
        	
        	checkNeoError(encoder.setVelocityConversionFactor(configuration.getVelocityConversionFactor()),
        				  "Failed to set encoder velocity conv factor");
        	
        	if (configuration.getOffset() != 0) 
        	{
        		checkNeoError(encoder.setZeroOffset(configuration.getOffset()), "Failed to set encoder zero offset");
        	}
            
            //SendableRegistry.addLW(encoder, "DriveBase", "TBEncoder[" + configuration.getId() + "]");
            
            return new EncoderImplementation(encoder);
        };
    }

    private static class EncoderImplementation implements AbsoluteEncoder 
    {
        private final SparkMaxAbsoluteEncoder	encoder;

        private EncoderImplementation(SparkMaxAbsoluteEncoder encoder) 
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
            double angle = encoder.getPosition();

            angle %= 2.0 * Math.PI;

            if (angle < 0.0) angle += 2.0 * Math.PI;

            return angle;
        }
    }
}
