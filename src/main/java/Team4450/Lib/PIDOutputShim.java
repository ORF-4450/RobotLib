package Team4450.Lib;

import Team4450.Lib.Wpilib.PIDController;
import Team4450.Lib.Wpilib.PIDOutput;

//import edu.wpi.first.wpilibj.PIDController;
//import edu.wpi.first.wpilibj.PIDOutput;

/**
 * Debugging shim for PID controller output.
 * <pre>
 * This is how you use the shims to trace the input and output of a pid controller:
 * 
 * sourceShim = new PIDSourceShim(Devices.winchEncoder);
 * outputShim = new PIDOutputShim(Devices.winchDrive);

 * //liftPidController = new PIDController(0.0, 0.0, 0.0, Devices.winchEncoder, Devices.winchDrive);
 * liftPidController = new PIDController(0.0, 0.0, 0.0, sourceShim, outputShim);

 * sourceShim.setPidController(liftPidController);
 * outputShim.setPidController(liftPidController);
 * </pre>
 */

public class PIDOutputShim implements PIDOutput
{
	public PIDOutput		pidOutput;
	private boolean			disableOutput, logOutput;
	private PIDController	pid;
	
	public PIDOutputShim(PIDOutput pidOutput)
	{
		this.pidOutput = pidOutput;
	}
	
	@Override
	public void pidWrite(double output)
	{
		if (logOutput)
		{
			if (pid != null) 
				Util.consoleLog("%.3f  ont=%b", output, pid.onTarget());
			else
				Util.consoleLog("%.3f", output);
		}
		
		if (pidOutput != null)
		{
			if (disableOutput)
				pidOutput.pidWrite(0);
			else
				pidOutput.pidWrite(output);
		}
	}
	
	public void enableLogging(boolean enabled)
	{
		logOutput = enabled;
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
