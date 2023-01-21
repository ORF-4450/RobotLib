package Team4450.Lib.Swerve;

import com.revrobotics.RelativeEncoder;

public interface SteerController 
{
    double getReferenceAngle();

    void setReferenceAngle(double referenceAngleRadians);

    void setStartingPosition(double steerOffset);

    double getAngle();

    void stop();

    void setPidConstants(double proportional, double integral, double derivative);

    RelativeEncoder getMotorEncoder();

    AbsoluteEncoder getAbsoluteEncoder();

    void setBrakeMode(boolean on);

    boolean getBrakeMode();
}
