package Team4450.Lib.Swerve;

public final class SdsModuleConfigurations 
{
    public static final ModuleConfiguration MK3_STANDARD = new ModuleConfiguration(
            0.1016,
            (14.0 / 50.0) * (28.0 / 16.0) * (15.0 / 60.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK3_FAST = new ModuleConfiguration(
            0.1016,
            (16.0 / 48.0) * (28.0 / 16.0) * (15.0 / 60.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK4_L1 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (25.0 / 19.0) * (15.0 / 45.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK4_L2 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (27.0 / 17.0) * (15.0 / 45.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK4_L3 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (28.0 / 16.0) * (15.0 / 45.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK4_L4 = new ModuleConfiguration(
            0.10033,
            (16.0 / 48.0) * (28.0 / 16.0) * (15.0 / 45.0),
            true,
            (15.0 / 32.0) * (10.0 / 60.0),
            true
    );

    public static final ModuleConfiguration MK4I_L1 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (25.0 / 19.0) * (15.0 / 45.0),
            true,
            (14.0 / 50.0) * (10.0 / 60.0),
            false
    );

    public static final ModuleConfiguration MK4I_L2 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (27.0 / 17.0) * (15.0 / 45.0),
            true,
            (14.0 / 50.0) * (10.0 / 60.0),
            false
    );

    public static final ModuleConfiguration MK4I_L3 = new ModuleConfiguration(
            0.10033,
            (14.0 / 50.0) * (28.0 / 16.0) * (15.0 / 45.0),
            true,
            (14.0 / 50.0) * (10.0 / 60.0),
            false
    );

    // The MAXSwerve module can be configured with one of three pinion gears: 12T, 13T, or 14T.
    // This changes the drive speed of the module (a pinion gear with more teeth will result in a
    // robot that drives faster).
    
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

    private SdsModuleConfigurations() 
    {
    }
}
