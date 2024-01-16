package Team4450.Lib.Swerve;

public class RevModuleConfigurations 
{
    // The MAXSwerve module can be configured with one of three pinion gears: 12T, 13T, or 14T.
    // This changes the drive speed of the module (a pinion gear with more teeth will result in a
    // robot that drives faster). We invert the gear reduction so it will work with the calculation
    // of max speed in the drivebase class which also works with the SDS ratios.
    
    public static final ModuleConfiguration MAXSWERVE_T12 = new ModuleConfiguration(
            0.0762,											// wheel diameter (m)
            1 / ((45.0 * 22) / (12 * 15)),					// drive reduction
            false,											// drive inverted
            12,												// steer reduction
            false											// steer inverted
    );

    
    public static final ModuleConfiguration MAXSWERVE_T13 = new ModuleConfiguration(
            0.0762,											// wheel diameter (m)
            1 / ((45.0 * 22) / (13 * 15)),					// drive reduction
            false,											// drive inverted
            12,												// steer reduction
            false											// steer inverted
    );

    public static final ModuleConfiguration MAXSWERVE_T14 = new ModuleConfiguration(
            0.0762,											// wheel diameter (m)
            1 / ((45.0 * 22) / (14 * 15)),					// drive reduction
            false,											// drive inverted
            12,												// steer reduction
            false											// steer inverted
    );

    private RevModuleConfigurations() 
    {
    }
}
