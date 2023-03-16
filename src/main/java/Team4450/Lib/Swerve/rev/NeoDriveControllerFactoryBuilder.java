package Team4450.Lib.Swerve.rev;

import com.ctre.phoenix.motorcontrol.can.TalonFX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.REVPhysicsSim;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax.IdleMode;

import Team4450.Lib.Util;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotBase;
import Team4450.Lib.Swerve.DriveController;
import Team4450.Lib.Swerve.DriveControllerFactory;
import Team4450.Lib.Swerve.ModuleConfiguration;
import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

import static Team4450.Lib.Swerve.rev.RevUtils.checkNeoError;

public final class NeoDriveControllerFactoryBuilder 
{
    private double nominalVoltage   = Double.NaN;
    private double currentLimit     = Double.NaN;
    private double rampRate         = Double.NaN;

    public NeoDriveControllerFactoryBuilder withVoltageCompensation(double nominalVoltage) 
    {
        //Util.consoleLog();
    
        this.nominalVoltage = nominalVoltage;

        return this;
    }

    public boolean hasVoltageCompensation() 
    {
        return Double.isFinite(nominalVoltage);
    }

    public NeoDriveControllerFactoryBuilder withCurrentLimit(double currentLimit) 
    {
        this.currentLimit = currentLimit;
        return this;
    }

    public boolean hasCurrentLimit() 
    {
        return Double.isFinite(currentLimit);
    }

    public NeoDriveControllerFactoryBuilder withRampRate(double rampRate) 
    {
        this.rampRate = rampRate;
        return this;
    }

    public boolean hasRampRate() 
    {
        return Double.isFinite(rampRate);
    }

    public DriveControllerFactory<ControllerImplementation, Integer> build() 
    {
        //Util.consoleLog();
    
        return new FactoryImplementation();
    }

    private class FactoryImplementation implements DriveControllerFactory<ControllerImplementation, Integer> 
    {
        @Override
        public ControllerImplementation create(Integer id, ModuleConfiguration moduleConfiguration) 
        {
            //Util.consoleLog();
    
            CANSparkMax motor = new CANSparkMax(id, CANSparkMaxLowLevel.MotorType.kBrushless);

            motor.restoreFactoryDefaults(); // 4450

            motor.setInverted(moduleConfiguration.isDriveInverted());

            if (hasVoltageCompensation())
                checkNeoError(motor.enableVoltageCompensation(nominalVoltage), "Failed to enable voltage compensation");

            if (hasCurrentLimit()) 
                checkNeoError(motor.setSmartCurrentLimit((int) currentLimit), "Failed to set current limit for NEO");
            
            if (hasRampRate())
                checkNeoError(motor.setOpenLoopRampRate(rampRate), "Failed to set NEO ramp rate");

            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 100), "Failed to set periodic status frame 0 rate");
            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, 20), "Failed to set periodic status frame 1 rate");
            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, 20), "Failed to set periodic status frame 2 rate");
            
            // Set neutral mode to brake
            motor.setIdleMode(CANSparkMax.IdleMode.kBrake);

            // Setup encoder
            RelativeEncoder encoder = motor.getEncoder();

            double positionConversionFactor = Math.PI * moduleConfiguration.getWheelDiameter() * moduleConfiguration.getDriveReduction();
            
            checkNeoError(encoder.setPositionConversionFactor(positionConversionFactor), "Failed to set drive NEO encoder pos conversion factor");
            checkNeoError(encoder.setVelocityConversionFactor(positionConversionFactor / 60.0), "Failed to set drive NEO encoder vel conversion factor");

            // Save all above settings to flash memory. If sparkmax power fails, it will restart with
            // the saved settings.
            
            checkNeoError(motor.burnFlash(), "Failed to burn drive NEO config");

            return new ControllerImplementation(motor, encoder);
        }
    }

    private static class ControllerImplementation implements DriveController 
    {
        private final CANSparkMax       motor;
        private final RelativeEncoder   encoder;
        private double                  currentSimVelocity, currentSimPosition;
        private ModulePosition			position;

        private ControllerImplementation(CANSparkMax motor, RelativeEncoder encoder) 
        {
            //Util.consoleLog();
    
            this.motor = motor;
            this.encoder = encoder;
                        
            if (RobotBase.isSimulation()) 
            {
                // Note that the REV simulation does not work correctly. We have hacked
                // a solution where we drive the sim through our code, not by reading the
                // REV simulated encoder position and velocity, which are incorrect. However, 
                // registering the motor controller with the REV sim is still needed.

                // Add Neo to sim.
                REVPhysicsSim.getInstance().addSparkMax(motor, DCMotor.getNEO(1));
            }
        }

        @Override
        public void setReferenceVoltage(double voltage, double velocity) 
        {
            // If voltage is tiny, zero it out so no power to motor when
            // we are not actually moving. This prevents constantly applying
            // a voltage that is not enough to move the robot possibly damaging
            // the motor.
            if (Math.abs(voltage) > .05)
                motor.setVoltage(voltage);
            else
                motor.setVoltage(0);

            // We track the "requested" velocity to use as a substitute for measured
            // velocity due to problems with Neo simulation not calculating a correct
            // velocity. We also compute the distance travelled for the same reason.
            // TODO Fix Rev simulation to work correctly. The code below appears to
            // work as a workaround but hard to say how well it models reality.
            currentSimVelocity = velocity;
            
            double distancePer20Ms = currentSimVelocity / 50.0;

            if (voltage > 0)
                currentSimPosition += distancePer20Ms;
            else
                currentSimPosition -= distancePer20Ms;
        }

        public double getVoltage()
        {
            return motor.getAppliedOutput();
        }

        @Override
        public double getVelocity() 
        {
            if (RobotBase.isReal())
                return encoder.getVelocity();
            else
                return currentSimVelocity;
        }

        @Override
        public void stop()
        {
            motor.stopMotor();
        }

        @Override
        public RelativeEncoder getEncoder() 
        {
            return encoder;
        }

        @Override
        public CANSparkMax getMotorNeo() 
        {
            return motor;
        }

        @Override
        public TalonFX getMotor500() 
        {
            return null;
        }

        @Override
        public void setBrakeMode(boolean on) 
        {
            Util.consoleLog("%b", on);
    
            if (on)
                motor.setIdleMode(IdleMode.kBrake);
            else
                motor.setIdleMode(IdleMode.kCoast);
        }

        @Override
        public double getDistance() 
        {
            if (RobotBase.isReal())
                return encoder.getPosition();
            else
                return currentSimPosition;
        }

		@Override
		public void setPosition(ModulePosition position) 
		{
			this.position = position;
		}
    }
}
