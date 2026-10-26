package Team4450.Lib;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;

import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
//import edu.wpi.first.math.system.plant.LinearSystemId;
//import edu.wpi.first.util.sendable.SendableBuilder;
//import edu.wpi.first.util.sendable.SendableRegistry;
import org.wpilib.framework.RobotBase;
import org.wpilib.system.RobotController;
import org.wpilib.tunable.ComplexTunable;
import org.wpilib.tunable.TunableTable;
import org.wpilib.simulation.DCMotorSim;

/**
 * Wrapper class for TalonFX that adds simulation support.
 */
public class Talon_FX extends TalonFX implements ComplexTunable
{
   private double          gearRatio = 1.0;
   private DCMotorSim      motorSimModel;
   private String		   name;

   /**
    * Create Talon_fX instance.
	* @param id CAN id of TalonFX.
    * @param dcMotor Motor type the TalonFX is controlling.
    * @param gearRatio Gear ratio (number of driven shaft turns to motor shaft turns).
    */
   public Talon_FX(CANBus canBus, int id, DCMotor dcMotor, double gearRatio)
   {
       this("Talon_FX", canBus, id, dcMotor, gearRatio);
   }
   
   /**
    * Create Talon_fX instance.
	* @param name Name assigned in Network Tables viewer.
	* @param id CAN id of TalonFX.
    * @param dcMotor Motor type the TalonFX is controlling.
    * @param gearRatio Gear ratio (number of driven shaft turns to motor shaft turns).
    */
   public Talon_FX(String name, CANBus canBus, int id, DCMotor dcMotor, double gearRatio)
   {
       super(id, canBus);
       
       this.gearRatio = gearRatio;

       // Reset TalonFX to default configuration.
       getConfigurator().apply(new TalonFXConfiguration());

       if (RobotBase.isSimulation()) initializeSim(dcMotor);
       
       name = String.format("%s[%s-%d]", name, getNetwork().getName(), getDeviceID());

       LiveWindow.registerComplexTunable(this, name);

       Util.consoleLog("%s", name);                       
   }
   
   /**
    * Returns the name of the object instance.
    * @return Name of this object instance.
    */
   public String getName()
   {
   	return name;
   }
   
   /**
    * Sets the name of the object instance.
    * @param name Name of this object instance.
    */
   public void setName(String name)
   {
   	Util.consoleLog("%s", name);
   	
   	name = String.format("%s[%s-%d]", name, getNetwork().getName(), getDeviceID());
   	
   	LiveWindow.registerComplexTunable(this, name);
   }
   
   /**
    * Release resources in preparation to destroy this object.
    */
   public void close()
   {
   	Util.consoleLog("%s", name);

   	//SendableRegistry.remove(this);
   }
	
   // Set up simulation.
   private void initializeSim(DCMotor dcMotor)
   {
       motorSimModel = new DCMotorSim
                       (
                           Models.singleJointedArmFromPhysicalConstants(
                               dcMotor, 
                               0.001, // A guess.
                               gearRatio
                           ),
                           dcMotor    
                       );
   }

   /**
    * This function should be called in the simulationPeriodic function of the
    * subsystem containing this Talon_FX instance to drive simulation.
    */
   public void simulationPeriodic() 
   {
       var talonFXSim = getSimState();
       
       talonFXSim.Orientation = ChassisReference.Clockwise_Positive;
       
       // set the supply voltage of the TalonFX
       talonFXSim.setSupplyVoltage(RobotController.getBatteryVoltage());

       // get the motor voltage of the TalonFX
       var motorVoltage = talonFXSim.getMotorVoltage();

       // use the motor voltage to calculate new position and velocity
       // using WPILib's DCMotorSim class for physics simulation
       motorSimModel.setInputVoltage(motorVoltage);
       motorSimModel.update(0.020); // assume 20 ms loop time

       // apply the new rotor position and velocity to the TalonFX;
       // note that this is rotor position/velocity (before gear ratio), but
       // DCMotorSim returns mechanism position/velocity (after gear ratio)
       //talonFXSim.setRawRotorPosition(motorSimModel.getAngularPositionRotations() * gearRatio);
       //talonFXSim.setRotorVelocity(motorSimModel.getAngularVelocity().times(gearRatio));
       talonFXSim.setRawRotorPosition(motorSimModel.getAngularPosition() * gearRatio);
       talonFXSim.setRotorVelocity(motorSimModel.getAngularVelocity() * gearRatio);

   }

   /**
    * Reset the motors shaft position to zero.
    */
   public void resetPosition()
   {
   	setPosition(0);
   }
   
   /**
    * Returns TalonFX shaft position in revolutions as a DoubleSupplier.
    * @return The motor's shaft revolutions.
    */
   public DoubleSupplier getPositionDS()
   {
       return () -> getPosition().getValueAsDouble();
   }

   /**
    * Returns TalonFX shaft velocity as a DoubleSupplier.
    * @return The motors shaft velocity in revolutions per second.
    */
   public DoubleSupplier getVelocityDS()
   {
       return () -> getVelocity().getValueAsDouble();
   }

	@Override
	public void publishTunable(TunableTable table) {
		table.publishDouble("speed", ()-> getThrottle(), null);
	    table.publishDouble("position (rot)", getPositionDS(), null);
	    table.publishDouble("velocity (rps)", getVelocityDS(), null);
	}
		
	@Override
	public String getTunableType() {
		return "Talon_FX";
	}
}

