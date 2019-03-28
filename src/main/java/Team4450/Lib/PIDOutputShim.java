package Team4450.Lib;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;

public class PIDOutputShim implements PIDOutput
{
	public PIDOutput		pidOutput;
	private boolean			disableOutput;
	private PIDController	pid;
	
	public PIDOutputShim(PIDOutput pidOutput)
	{
		this.pidOutput = pidOutput;
	}
	
	@Override
	public void pidWrite(double output)
	{
		Util.consoleLog("%.3f  ont=%b", output, pid.onTarget());
		
		if (disableOutput)
			pidOutput.pidWrite(0);
		else
			pidOutput.pidWrite(output);
	}
	
	public void disableOutput(boolean disabled)
	{
		disableOutput = disabled;
	}
	
	public void setPidController(PIDController pid)
	{
		this.pid = pid;
	}
}
