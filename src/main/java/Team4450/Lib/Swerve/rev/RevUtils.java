package Team4450.Lib.Swerve.rev;

import com.revrobotics.REVLibError;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;

public final class RevUtils 
{
    private RevUtils() {}

    public static void checkNeoError(REVLibError error, String message) 
    {
        if (error != REVLibError.kOk) 
        {
            Util.consoleLog("%s: %s", message, error.toString());

            if (RobotBase.isReal()) 
                DriverStation.reportError(String.format("%s: %s", message, error.toString()), false);
        }
    }
}
