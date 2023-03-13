package Team4450.Lib.Swerve.ctre;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;

import Team4450.Lib.Swerve.DriveController;
import Team4450.Lib.Swerve.DriveControllerFactory;
import Team4450.Lib.Swerve.ModuleConfiguration;
import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;

public final class Falcon500DriveControllerFactoryBuilder 
{
    private static final double TICKS_PER_ROTATION = 2048.0;

    private static final int CAN_TIMEOUT_MS = 250;
    private static final int STATUS_FRAME_GENERAL_PERIOD_MS = 250;

    private double nominalVoltage   = Double.NaN;
    private double currentLimit     = Double.NaN;
    private double rampRate         = Double.NaN;

    public Falcon500DriveControllerFactoryBuilder withVoltageCompensation(double nominalVoltage) 
    {
        this.nominalVoltage = nominalVoltage;
        return this;
    }

    public boolean hasVoltageCompensation() 
    {
        return Double.isFinite(nominalVoltage);
    }

    public DriveControllerFactory<ControllerImplementation, Integer> build() 
    {
        return new FactoryImplementation();
    }

    public Falcon500DriveControllerFactoryBuilder withCurrentLimit(double currentLimit) 
    {
        this.currentLimit = currentLimit;
        return this;
    }

    public boolean hasCurrentLimit() 
    {
        return Double.isFinite(currentLimit);
    }

    public Falcon500DriveControllerFactoryBuilder withRampRate(double rampRate) 
    {
        this.rampRate = rampRate;
        return this;
    }

    public boolean hasRampRate() 
    {
        return Double.isFinite(rampRate);
    }

    private class FactoryImplementation implements DriveControllerFactory<ControllerImplementation, Integer> 
    {
        @Override
        public ControllerImplementation create(Integer driveConfiguration, ModuleConfiguration moduleConfiguration) 
        {
            TalonFXConfiguration motorConfiguration = new TalonFXConfiguration();

            double sensorPositionCoefficient = Math.PI * moduleConfiguration.getWheelDiameter() * moduleConfiguration.getDriveReduction() / TICKS_PER_ROTATION;
            double sensorVelocityCoefficient = sensorPositionCoefficient * 10.0;

            if (hasVoltageCompensation()) motorConfiguration.voltageCompSaturation = nominalVoltage;

            if (hasRampRate()) motorConfiguration.closedloopRamp = rampRate;

            if (hasCurrentLimit()) {
                motorConfiguration.supplyCurrLimit.currentLimit = currentLimit;
                motorConfiguration.supplyCurrLimit.enable = true;
            }

            WPI_TalonFX motor = new WPI_TalonFX(driveConfiguration);
            CtreUtils.checkCtreError(motor.configAllSettings(motorConfiguration), "Failed to configure Falcon 500");

            if (hasVoltageCompensation()) motor.enableVoltageCompensation(true);

            motor.setNeutralMode(NeutralMode.Brake);

            motor.setInverted(moduleConfiguration.isDriveInverted() ? TalonFXInvertType.Clockwise : TalonFXInvertType.CounterClockwise);
            motor.setSensorPhase(true);

            // Reduce CAN status frame rates
            CtreUtils.checkCtreError(
                    motor.setStatusFramePeriod(
                            StatusFrameEnhanced.Status_1_General,
                            STATUS_FRAME_GENERAL_PERIOD_MS,
                            CAN_TIMEOUT_MS
                    ),
                    "Failed to configure Falcon status frame period"
            );

            return new ControllerImplementation(motor, sensorVelocityCoefficient, sensorPositionCoefficient);
        }
    }

    private class ControllerImplementation implements DriveController 
    {
        private final WPI_TalonFX 		motor;
        private ModulePosition			position;
        private final double 			sensorVelocityCoefficient, sensorPositionCoefficient;
        private final double 			nominalVoltage = hasVoltageCompensation() ? 
        								Falcon500DriveControllerFactoryBuilder.this.nominalVoltage : 12.0;

        private ControllerImplementation(WPI_TalonFX motor, double sensorVelocityCoefficient,
                                         double sensorPositionCoefficient) 
        {
            this.motor = motor;
            this.sensorVelocityCoefficient = sensorVelocityCoefficient;
            this.sensorPositionCoefficient = sensorPositionCoefficient;
        }

        @Override
        public void setReferenceVoltage(double voltage, double velocity) 
        {
            // If voltage is small, zero it out so no power to motor when
            // we are not actually moving. This prevents constantly applying
            // a voltage that is not enough to move the robot possibly damaging
            // the motor.
            if (Math.abs(voltage) > .25)
                motor.setVoltage(voltage);
            else
                motor.setVoltage(0);
                
            motor.set(TalonFXControlMode.PercentOutput, voltage / nominalVoltage);
        }

        @Override
        public double getVelocity() 
        {
            return motor.getSelectedSensorVelocity() * sensorVelocityCoefficient;
        }

        @Override
        public void stop()
        {
            motor.set(TalonFXControlMode.PercentOutput, 0);
        }

        @Override
        public RelativeEncoder getEncoder() 
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public CANSparkMax getMotorNeo() 
        {
            // TODO
            return null;
        }

        @Override
        public WPI_TalonFX getMotor500() 
        {
            return motor;
        }

        @Override
        public void setBrakeMode(boolean on) 
        {
            if (on)
                motor.setNeutralMode(NeutralMode.Brake);
            else
                motor.setNeutralMode(NeutralMode.Coast);
        }

        @Override
        public double getDistance() 
        {
            return motor.getSelectedSensorPosition() * sensorPositionCoefficient;
        }

        @Override
        public double getVoltage() 
        {
            return motor.getMotorOutputVoltage();
        }

		@Override
		public void setPosition(ModulePosition position) 
		{
			this.position = position;
		}
    }
}
