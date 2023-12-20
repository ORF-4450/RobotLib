package Team4450.Lib.Swerve.rev;

import com.revrobotics.*;
import com.revrobotics.CANSparkMax.IdleMode;

import Team4450.Lib.Util;
import Team4450.Lib.Swerve.*;
import Team4450.Lib.Swerve.AbsoluteEncoder;
import Team4450.Lib.Swerve.ModuleConfiguration.ModulePosition;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardContainer;

import static Team4450.Lib.Swerve.rev.RevUtils.checkNeoError;

public final class NeoSteerControllerFactoryBuilder
{
    // PID configuration
    private double pidProportional  = Double.NaN;
    private double pidIntegral      = Double.NaN;
    private double pidDerivative    = Double.NaN;

    private double nominalVoltage   = Double.NaN;
    private double currentLimit     = Double.NaN;
    private double rampRate         = Double.NaN;

    public NeoSteerControllerFactoryBuilder withPidConstants(double proportional, double integral, double derivative) 
    {
        //Util.consoleLog();
    
        this.pidProportional = proportional;
        this.pidIntegral = integral;
        this.pidDerivative = derivative;
        
        return this;
    }

    public boolean hasPidConstants() 
    {
        return Double.isFinite(pidProportional) && Double.isFinite(pidIntegral) && Double.isFinite(pidDerivative);
    }

    public NeoSteerControllerFactoryBuilder withVoltageCompensation(double nominalVoltage) 
    {
        this.nominalVoltage = nominalVoltage;
        return this;
    }

    public boolean hasVoltageCompensation() 
    {
        return Double.isFinite(nominalVoltage);
    }

    public NeoSteerControllerFactoryBuilder withCurrentLimit(double currentLimit) 
    {
        this.currentLimit = currentLimit;
        return this;
    }

    public boolean hasCurrentLimit() 
    {
        return Double.isFinite(currentLimit);
    }

    public NeoSteerControllerFactoryBuilder withRampRate(double rampRate) 
    {
        this.rampRate = rampRate;
        return this;
    }

    public boolean hasRampRate() 
    {
        return Double.isFinite(rampRate);
    }

    public <T> SteerControllerFactory<ControllerImplementation, NeoSteerConfiguration<T>> build(AbsoluteEncoderFactory<T> encoderFactory) 
    {
        //Util.consoleLog();
    
        return new FactoryImplementation<>(encoderFactory);
    }

    public class FactoryImplementation<T> implements SteerControllerFactory<ControllerImplementation, NeoSteerConfiguration<T>> 
    {
        private final AbsoluteEncoderFactory<T> encoderFactory;

        public FactoryImplementation(AbsoluteEncoderFactory<T> encoderFactory) 
        {
            //Util.consoleLog();
    
            this.encoderFactory = encoderFactory;
        }

        @Override
        public void addDashboardEntries(ShuffleboardContainer container, ControllerImplementation controller) 
        {
            //Util.consoleLog();
    
            container.addString("Absolute Angle", 
                    () -> String.format("%.3f", Math.toDegrees(controller.absoluteEncoder.getAbsoluteAngle())))
            		.withPosition(0, 0);
            
            //container.addNumber("Steer Encoder position", () -> controller.getMotorEncoder().getPosition()); 

            SteerControllerFactory.super.addDashboardEntries(container, controller);
        }

        @Override
        public ControllerImplementation create(NeoSteerConfiguration<T> steerConfiguration, ModuleConfiguration moduleConfiguration) 
        {
            //Util.consoleLog();
    
            AbsoluteEncoder absoluteEncoder = encoderFactory.create(steerConfiguration.getEncoderConfiguration());

            CANSparkMax motor = new CANSparkMax(steerConfiguration.getMotorPort(), CANSparkMaxLowLevel.MotorType.kBrushless);

            motor.restoreFactoryDefaults(); // 4450
            //motor.getAbsoluteEncoder(Type.kDutyCycle)

            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, 100), "Failed to set periodic status frame 0 rate");
            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, 20), "Failed to set periodic status frame 1 rate");
            checkNeoError(motor.setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, 20), "Failed to set periodic status frame 2 rate");
            checkNeoError(motor.setIdleMode(CANSparkMax.IdleMode.kBrake), "Failed to set NEO idle mode");
            
            motor.setInverted(!moduleConfiguration.isSteerInverted());
            
            if (hasVoltageCompensation()) 
                checkNeoError(motor.enableVoltageCompensation(nominalVoltage), "Failed to enable voltage compensation");
            
            if (hasCurrentLimit())
                checkNeoError(motor.setSmartCurrentLimit((int) Math.round(currentLimit)), "Failed to set NEO current limits");
            
            if (hasRampRate())
                checkNeoError(motor.setOpenLoopRampRate(rampRate), "Failed to set NEO ramp rate");
            
            RelativeEncoder integratedEncoder = motor.getEncoder();

            checkNeoError(integratedEncoder.setPositionConversionFactor(2.0 * Math.PI * moduleConfiguration.getSteerReduction()), "");
            checkNeoError(integratedEncoder.setVelocityConversionFactor(2.0 * Math.PI * moduleConfiguration.getSteerReduction() / 60.0), "Failed to set steer NEO encoder vel conversion factor");
            checkNeoError(integratedEncoder.setPosition(absoluteEncoder.getAbsoluteAngle()), "Failed to set NEO encoder position");

            SparkMaxPIDController controller = motor.getPIDController();

            if (hasPidConstants()) 
            {
                checkNeoError(controller.setP(pidProportional), "Failed to set NEO PID proportional constant");
                checkNeoError(controller.setI(pidIntegral), "Failed to set NEO PID integral constant");
                checkNeoError(controller.setD(pidDerivative), "Failed to set NEO PID derivative constant");
            }

            checkNeoError(controller.setFeedbackDevice(integratedEncoder), "Failed to set NEO PID feedback device");

            // Save all above settings to flash memory. If sparkmax power fails, it will restart with
            // the saved settings. It will still need the start position button pressed to completely
            // recover.
            
            checkNeoError(motor.burnFlash(), "Failed to burn steering NEO config");
            
            return new ControllerImplementation(motor, steerConfiguration.getPosition(), absoluteEncoder);
        }
    }

    public static class ControllerImplementation implements SteerController 
    {
        private static final int ENCODER_RESET_ITERATIONS = 500;
        private static final double ENCODER_RESET_MAX_ANGULAR_VELOCITY = Math.toRadians(0.5);

        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final CANSparkMax           motor;
        private final ModulePosition		position;
        private final SparkMaxPIDController controller;
        private final RelativeEncoder       motorEncoder;
        private final AbsoluteEncoder       absoluteEncoder;

        private double referenceAngleRadians = 0;
        private double resetIteration = 0;

        public ControllerImplementation(CANSparkMax motor, ModulePosition position, AbsoluteEncoder absoluteEncoder) 
        {
            //Util.consoleLog();
    
            this.motor = motor;
            this.position = position;
            this.controller = motor.getPIDController();
            this.motorEncoder = motor.getEncoder();
            this.absoluteEncoder = absoluteEncoder;
                                    
            if (RobotBase.isSimulation()) 
            {
                // Note that the REV simulation does not work correctly. We have hacked
                // a solution where we drive the sim through our code, not by reading the
                // REV simulated encoder position and velocity, which are incorrect. However, 
                // registering the motor controller with the REV sim is still needed.

                // Add Neo to sim.
                REVPhysicsSim.getInstance().addSparkMax(motor, DCMotor.getNEO(1));

                //controller.setP(1, 3);
            }
        }

        @Override
        public void stop()
        {
            motor.stopMotor();
        }

        @Override
        public void setPidConstants(double proportional, double integral, double derivative)
        {
            //Util.consoleLog();
    
            checkNeoError(controller.setP(proportional), "Failed to set NEO PID proportional constant");
            checkNeoError(controller.setI(integral), "Failed to set NEO PID integral constant");
            checkNeoError(controller.setD(derivative), "Failed to set NEO PID derivative constant");
        }

        @Override
        public double getReferenceAngle() 
        {
            return referenceAngleRadians;
        }

        @Override
        public void setReferenceAngle(double referenceAngleRadians) 
        {
            double currentAngleRadians = motorEncoder.getPosition();

            // Reset the NEO's encoder periodically when the module is not rotating.
            // Sometimes (~5% of the time) when we initialize, the absolute encoder isn't fully set up, and we don't
            // end up getting a good reading. If we reset periodically this won't matter anymore.
            
            if (motorEncoder.getVelocity() < ENCODER_RESET_MAX_ANGULAR_VELOCITY) 
            {
                if (++resetIteration >= ENCODER_RESET_ITERATIONS) 
                {
                    resetIteration = 0;
                    double absoluteAngle = absoluteEncoder.getAbsoluteAngle();
                    motorEncoder.setPosition(absoluteAngle);
                    currentAngleRadians = absoluteAngle;
                }
            } else {
                resetIteration = 0;
            }

            double currentAngleRadiansMod = currentAngleRadians % (2.0 * Math.PI);
            
            if (currentAngleRadiansMod < 0.0) currentAngleRadiansMod += 2.0 * Math.PI;

            // The reference angle has the range [0, 2pi) but the Neo's encoder can go above that
            double adjustedReferenceAngleRadians = referenceAngleRadians + currentAngleRadians - currentAngleRadiansMod;
            
            if (referenceAngleRadians - currentAngleRadiansMod > Math.PI) 
                adjustedReferenceAngleRadians -= 2.0 * Math.PI;
            else if (referenceAngleRadians - currentAngleRadiansMod < -Math.PI) 
                adjustedReferenceAngleRadians += 2.0 * Math.PI;

            this.referenceAngleRadians = referenceAngleRadians;

            controller.setReference(adjustedReferenceAngleRadians, CANSparkMax.ControlType.kPosition);
        }

        @Override
        public double getAngle() 
        {
            if (RobotBase.isReal())
            {
                double motorAngleRadians = motorEncoder.getPosition();

                motorAngleRadians %= 2.0 * Math.PI;

                if (motorAngleRadians < 0.0) motorAngleRadians += 2.0 * Math.PI;

                return motorAngleRadians;
            }
            else
                return getReferenceAngle();
        }

        @Override
        public RelativeEncoder getMotorEncoder()
        {
            return motorEncoder;
        }

        @Override
        public AbsoluteEncoder getAbsoluteEncoder()
        {
            return absoluteEncoder;
        }

        @Override
        public void setBrakeMode(boolean on) 
        {
            if (on)
                motor.setIdleMode(IdleMode.kBrake);
            else
                motor.setIdleMode(IdleMode.kCoast);
        }

        @Override        
        public boolean getBrakeMode() 
        {
            if (motor.getIdleMode() == IdleMode.kBrake)
                return true;
            else    
                return false;
        }

        /**
         * This method launches a thread that uses a PID control to turn the
         * wheel to the "start" position, which represents wheel aligned straight
         * ahead and wheel gear facing the left side of the chassis. The start position
         * is CanCoder absolute angle = 360 or 0 with steering offset applied. Note that
         * the steering offset is configured in the encoder and is automatically applied.
         * Turns out steerOffset parameter is not needed here.
         */
        @Override
        public void setStartingPosition(double steerOffset) 
        {
            new Thread(() -> 
            {
                PIDController pid = new PIDController(.01, 0, 0);
                
                pid.setTolerance(0.25);
                pid.enableContinuousInput(0, 360);

                double  power, angle, startTime = Util.timeStamp();
                String  result = "";

                try {
                    while (true)
                    {
                        angle = Math.toDegrees(absoluteEncoder.getAbsoluteAngle());

                        power = pid.calculate(angle, 360);

                        power = Util.clampValue(power, .20);

                        motor.set(power);

                        if (pid.atSetpoint())
                        {
                            motor.stopMotor();
                            motorEncoder.setPosition(absoluteEncoder.getAbsoluteAngle());
                            result = "on target";
                            break;
                        }

                        if (Util.getElaspedTime(startTime) > 2.0) 
                        {
                            motor.stopMotor();
                            result = "timeout";
                            break;
                        }

                        // Util.consoleLog("angle=%.3f  target=360  error=%.3f  pwr=%.3f",
                        //     angle, 
                        //     pid.getPositionError(), power);

                        Thread.sleep(20);
                    }
                } catch (Exception e) { Util.logException(e); }

                motor.stopMotor();

                pid.close();

                Util.consoleLog("%s %s", position.toString(), result);
            }).start();
        }
    }
}
