package Team4450.Lib.Swerve;

import Team4450.Lib.Util;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardContainer;

@FunctionalInterface
public interface SteerControllerFactory<Controller extends SteerController, SteerConfiguration> 
{	
    default void addDashboardEntries(ShuffleboardContainer container, Controller controller) 
    {
        Util.consoleLog();
    
        container.addNumber("Current Angle", () -> Math.toDegrees(controller.getAngle()))
        					.withPosition(1, 0); // 1
        container.addNumber("Target Angle", () -> Math.toDegrees(controller.getReferenceAngle()))
        					.withPosition(0, 1); // 0, 2
    }

    default Controller create(
            ShuffleboardContainer	dashboardContainer,
            SteerConfiguration 		steerConfiguration,
            ModuleConfiguration 	moduleConfiguration) 
    {
        Util.consoleLog();
    
        var controller = create(steerConfiguration, moduleConfiguration);

        addDashboardEntries(dashboardContainer, controller);

        return controller;
    }

    Controller create(SteerConfiguration steerConfiguration, ModuleConfiguration moduleConfiguration);
}
