package Team4450.Lib;

import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

public class PIDSourceShim implements PIDSource
{
	PIDSource	pidSource;
	
	public PIDSourceShim(PIDSource pidSource)
	{
		this.pidSource = pidSource;
	}
	
	@Override
	public double pidGet()
	{
		Util.consoleLog("%f", pidSource.pidGet());
		return pidSource.pidGet();
	}

	@Override
	public void setPIDSourceType( PIDSourceType pidSourceType )
	{
		this.pidSource.setPIDSourceType(pidSourceType);
	}

	@Override
	public PIDSourceType getPIDSourceType()
	{
		return pidSource.getPIDSourceType();
	}
}

