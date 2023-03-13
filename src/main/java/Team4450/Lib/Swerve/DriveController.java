package Team4450.Lib.Swerve;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

public interface DriveController 
{
    void setReferenceVoltage(double voltage, double velocity);

    double getVelocity();

    double getDistance();

    double getVoltage(); // TODO remove this?

    void stop();
    
    public RelativeEncoder getEncoder();

    public CANSparkMax getMotorNeo();

    public TalonFX getMotor500();

    public void setBrakeMode(boolean on);
    
    public void setPosition(ModulePosition position);
}
