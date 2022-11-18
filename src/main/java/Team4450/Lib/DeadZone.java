package Team4450.Lib;

import java.util.function.DoubleSupplier;

/**
 * Applies dead zone and inversion to any double input. Returns a double
 * or double supplier (if input is a double supplier).
 */
public class DeadZone implements DoubleSupplier
{
    DoubleSupplier      input;
    double              deadZone;
    boolean             invert;

    /**
     * Create DeadZone instance. Return the input value if it's absolute value is greater
     * or equal to the dead zone else zero.
     * @param input Input value.
     * @param deadZone Dead zone value to be applied (0-1).
     * @param invert True to multiply the result by -1.
     */
    public DeadZone(DoubleSupplier input, double deadZone, boolean invert)
    {
        this.input = input;
        Util.checkRange(deadZone, 0.0, 1.0, "Dead Zone.");
        this.deadZone = deadZone;
        this.invert = invert;
    }

    /**
     * Create DeadZone instance. Return the input value if it's absolute value is greater
     * or equal to the dead zone else zero.
     * @param input Input value.
     * @param deadZone Dead zone value to be applied (0-1).
     * @param invert True to multiply the result by -1.
     */
    public DeadZone(double input, double deadZone, boolean invert)
    {
        this(() -> input, deadZone, invert);
    }
    
    /**
     * Create DeadZone instance. Return the input value if it's absolute value is greater
     * or equal to the dead zone else zero. Can only be used by calling get(inputValue) function.
     * @param deadZone Dead zone value to be applied (0-1).
     * @param invert True to multiply the result by -1.
     */
    public DeadZone(double deadZone, boolean invert)
    {
        this(null, deadZone, invert);
    }

    /**
     * Return the input value read from double supplier after applying dead zone and invert.
     * @return The resulting value.
     */
    @Override
    public double getAsDouble() 
    {
        double inputValue = input.getAsDouble();

        if (Math.abs(inputValue) < deadZone) inputValue = 0.0;

        if (invert) inputValue *= -1;

        return inputValue;
    }

    /**
     * Return the specified input value after applying dead zone and invert. Use only
     * with the DeadZone(double deadZone, boolean invert) constructor.
     * @param inputValue The value to which the dead zone and invert will be applied.
     * @return The resulting value.
     */
    public double get(double inputValue)
    {
        this.input = () -> inputValue;

        return getAsDouble();
    }

    /**
     * Static function to apply a dead zone and or invert to an input value.
     * @param inputValue Value to be processed.
     * @param deadZone The dead zone (0-1).
     * @param invert True to multiply the result by -1.
     * @return The resulting value.
     */
    public static double get(double inputValue, double deadZone, boolean invert)
    {
        Util.checkRange(deadZone, 0.0, 1.0, "Dead Zone.");

        if (Math.abs(inputValue) < deadZone) inputValue = 0.0;

        if (invert) inputValue *= -1;

        return inputValue;        
    }
}

