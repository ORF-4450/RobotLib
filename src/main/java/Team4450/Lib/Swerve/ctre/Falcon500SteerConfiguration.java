package Team4450.Lib.Swerve.ctre;

import java.util.Objects;

import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

public class Falcon500SteerConfiguration<EncoderConfiguration> 
{
    private final int 					motorPort;
    private final ModulePosition		position;
    private final EncoderConfiguration  encoderConfiguration;

    public Falcon500SteerConfiguration(int motorPort, ModulePosition position, EncoderConfiguration encoderConfiguration) 
    {
        this.motorPort = motorPort;
        this.position = position;
        this.encoderConfiguration = encoderConfiguration;
    }

    public int getMotorPort() 
    {
        return motorPort;
    }
    
    public ModulePosition getPosition()
    {
    	return position;
    }

    public EncoderConfiguration getEncoderConfiguration() 
    {
        return encoderConfiguration;
    }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Falcon500SteerConfiguration<?> that = (Falcon500SteerConfiguration<?>) o;
        return getMotorPort() == that.getMotorPort() && getEncoderConfiguration().equals(that.getEncoderConfiguration());
    }

    @Override
    public int hashCode() 
    {
        return Objects.hash(getMotorPort(), getEncoderConfiguration());
    }

    @Override
    public String toString() 
    {
        return "Falcon500SteerConfiguration{" +
                "motorPort=" + motorPort +
                ", encoderConfiguration=" + encoderConfiguration +
                '}';
    }
}
