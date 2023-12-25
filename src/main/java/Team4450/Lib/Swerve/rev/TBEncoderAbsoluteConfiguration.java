package Team4450.Lib.Swerve.rev;

import com.revrobotics.CANSparkMax;

public class TBEncoderAbsoluteConfiguration 
{
    private final int       	id;
    private final double    	offset;
    private double				positionConversionFactor = (2 * Math.PI);			// Radians
    private double				velocityConversionFactor = (2 * Math.PI) / 60.0;	// Radians / second
    private CANSparkMax			motor;
    private boolean				inverted = true;	// Required for MaxSwerve.

    public TBEncoderAbsoluteConfiguration withPositionConvertionFactor(double factor) 
    {
        positionConversionFactor = factor;
        return this;
    }

    public TBEncoderAbsoluteConfiguration withVelocityConvertionFactor(double factor) 
    {
        velocityConversionFactor = factor;
        return this;
    }
    
    public TBEncoderAbsoluteConfiguration(int id, double offset, CANSparkMax motor) 
    {
        this.id = id;
        this.offset = offset;
        this.motor = motor;
    }
    
    public TBEncoderAbsoluteConfiguration(int id, double offset) 
    {
        this(id, offset, null);
    }
    
    public TBEncoderAbsoluteConfiguration(int id, double offset, boolean inverted) 
    {
        this(id, offset, null);
        
        this.inverted = inverted;
    }

    public int getId() 
    {
        return id;
    }

    public double getOffset() 
    {
        return offset;
    }

    public boolean getInverted() 
    {
        return inverted;
    }

    public void setInverted(boolean inverted) 
    {
        this.inverted = inverted;
    }
    
    public double getPositionConversionFactor()
    {
    	return positionConversionFactor;
    }
    
    public double getVelocityConversionFactor()
    {
    	return velocityConversionFactor;
    }
    
    public void setMotor(CANSparkMax motor)
    {
    	this.motor = motor;
    }
    
    public CANSparkMax getMotor() 
    {
        return motor;
    }
}
