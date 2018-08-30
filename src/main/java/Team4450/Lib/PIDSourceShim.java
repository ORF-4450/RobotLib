package Team4450.Lib;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;

public class PIDSourceShim implements PIDSource
{
	public PIDSource		pidSource;
	private PIDController	pid;
	
	public PIDSourceShim(PIDSource pidSource)
	{
		this.pidSource = pidSource;
	}
	
	@Override
	public double pidGet()
	{
		Util.consoleLog("%f sp=%f", pidSource.pidGet(), pid.getSetpoint());
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
	
	public void setPidController(PIDController pid)
	{
		this.pid = pid;
	}
}

