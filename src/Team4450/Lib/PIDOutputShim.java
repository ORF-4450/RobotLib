package Team4450.Lib;

import edu.wpi.first.wpilibj.PIDOutput;

public class PIDOutputShim implements PIDOutput
{
	PIDOutput	pidOutput;
	
	public PIDOutputShim(PIDOutput pidOutput)
	{
		this.pidOutput = pidOutput;
	}
	
	@Override
	public void pidWrite(double output)
	{
		Util.consoleLog("%f", output);
		pidOutput.pidWrite(output);
	}
}
