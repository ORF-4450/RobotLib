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
		double sp, src;
		
		Util.consoleLog();
		
		sp = pid.getSetpoint();
		Util.consoleLog("after sp=%.3f", sp);
		src = pidSource.pidGet();
		Util.consoleLog("after src=%.3f", src);

		Util.consoleLog("sp=%.3f src=%.3f err=%.3f", sp, src, sp - src);	

		return src;
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

