package Team4450.Lib.Swerve.ctre;

import com.ctre.phoenix.ErrorCode;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;

public final class CtreUtils 
{
    private CtreUtils() 
    {
    }

    public static void checkCtreError(ErrorCode errorCode, String message) 
    {
        if (errorCode != ErrorCode.OK) 
        {
            if (RobotBase.isReal()) 
                DriverStation.reportError(String.format("%s: %s", message, errorCode.toString()), false);
            
            Util.consoleLog(String.format("%s: %s", message, errorCode.toString()));
        }
    }
}
