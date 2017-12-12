package Team4450.Lib;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

/**
 * Provides a wrapper for the Absolute Encoder model MA3-A10-250N. That encoder is attached
 * as an AnalogInput device and reads 0-5v for 0-360 degrees. Implements PIDSource
 * interface so it can be a data source to the PID controller class.
 */

public class AbsoluteEncoder implements PIDSource
{
	private AnalogInput		encoder;
	private PIDSourceType	pidSourceType = PIDSourceType.kDisplacement;
	private int				zeroAngleOffset = 0;
	private boolean			pidOffsetMode = false;
	
	public AbsoluteEncoder(AnalogInput encoder)
	{
		this.encoder = encoder;
	}
	
	public AbsoluteEncoder(int port)
	{
		this.encoder = new AnalogInput(port);
	}
	
	public AbsoluteEncoder(AnalogInput encoder, int zeroAngleOffset)
	{
		this.encoder = encoder;
		this.zeroAngleOffset = zeroAngleOffset;
	}
	
	public AbsoluteEncoder(int port, int zeroAngleOffset)
	{
		this.encoder = new AnalogInput(port);
		this.zeroAngleOffset = zeroAngleOffset;
	}
	
	/**
	 * Set source data type to be returned by pidGet().
	 * @param pidSource Source data type. Only kDisplacement (angle) supported at this time.
	 */
	@Override
	public void setPIDSourceType(PIDSourceType pidSource)
	{
		pidSourceType = PIDSourceType.kDisplacement; //pidSource;
	}

	/**
	 * Return current data source type.
	 * @return PID source data type.
	 */
	@Override
	public PIDSourceType getPIDSourceType()
	{
		return pidSourceType;
	}
	
	/**
	 * Sets which angle value is returned to the pid controller, the angle from getAngle()
	 * or getOffsetFromZero().
	 * @param enabled True = use getOffsetFromZero(), false = use getAngle().
	 */
	public void setPidOffsetMode(boolean enabled)
	{
		pidOffsetMode = enabled;
	}
	
	/**
	 * Set the offset angle that represents zero. This value is added to
	 * the angle returned by the encoder.
	 * @param offset Offset angle 0-360.
	 */
	public void setZeroAngleOffset(int offset)
	{
		this.zeroAngleOffset = offset;
	}
	
	/**
	 * Return current zero angle offset.
	 * @return Zero angle offset 0-360.
	 */
	public int getZeroAngleOffset()
	{
		return zeroAngleOffset;
	}
	
	/**
	 * Return the current unadjusted angle of the encoder.
	 * @return Current angle 0-360.
	 */
	public int getRawAngle()
	{
		return (int) (encoder.getVoltage() * 72);
	}
	
	/**
	 * Return the current angle of the encoder with zero offset applied.
	 * @return Current angle 0-360.
	 */
	public int getAngle()
	{
		int	angle = (int) (encoder.getVoltage() * 72);
		
		angle = angle - zeroAngleOffset;
		
		if (angle < 0) angle += 360;
		
		return angle;
	}
	
	/**
	 * Return the current offset angle left(-) or right(+) of the encoder with reference
	 * to the zero position (zero offset applied).
	 * @return Current angle -180 to +180.
	 */
	public int getOffsetFromZero()
	{
		int	angle = (int) (encoder.getVoltage() * 72);
		
		angle = angle - zeroAngleOffset;
		
		return angle;
	}
	
	/**
	 * Return the current voltage of the underlying AnalogInput object.
	 * @return Voltage 0-5v.
	 */
	public double getVoltage()
	{
		return encoder.getVoltage();
	}
	
	public double getRate()
	{
		// TODO: Implement a rate of change function.
		
		return 0;
	}
	
	/**
	 * Return the current rotational rate of the encoder or current value (offset or angle) to PID controller.
	 * Only offset and angle implemented at this time. The use of the offset from zero point or angle from
	 * zero point controlled by setPidOffsetMode().
	 * @return Encoder Current offset/angle or rate of change.
	 */
	@Override
	public double pidGet()
	{
		if (pidSourceType == PIDSourceType.kRate)
			return getRate();
		else if (pidOffsetMode)
			return getOffsetFromZero();
		else
			return getAngle();
	}
}
