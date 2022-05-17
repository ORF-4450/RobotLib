package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.util.Color;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.ColorSensorV3.RawColor;

/**
 * Wrapper class for REV Color Sensor V3.
 */
public class RevColorSensor implements Sendable
{
	/**
	 * Static reference to the internal RevColorSensor instance created by
	 * getInstance() calls on this class. Must call a getInstance() before using.
	 */ 
	public static RevColorSensor INSTANCE;

    /**
	 * Change the I2C port below to match the connection of your color sensor
	 */
	private I2C.Port	i2cPort = I2C.Port.kOnboard;
	
	/**
	 * A Rev Color Sensor V3 object is constructed with an I2C port as a 
	 * parameter. The device will be automatically initialized with default 
	 * parameters.
	 */
	private final ColorSensorV3	colorSensor = new ColorSensorV3(i2cPort);	
	
	private ColorMatch 			colorMatcher = new ColorMatch();
	
	// Private constructor prevents instantiation.

	private RevColorSensor()
	{
	}
    
	/**
	 * Returns reference to global single instance of this class. You can also
	 * reference the global instance with: RevColorSensor.INSTANCE
	 * @return Reference to global instance of RevColorSensor.
	*/
    public static RevColorSensor getInstance()
    {
    	if (INSTANCE == null) INSTANCE = new RevColorSensor();
        
  	  	SendableRegistry.addLW(INSTANCE, "RevColorSensor");
    	
        return INSTANCE;
    }
    
    /**
     * Set which I2C port the sensor is plugged into. Defaults to 
     * RoboRio I2C port.
     * @param port I2C.port value for desired port.
     */
    public void setPort(I2C.Port port)
    {
    	i2cPort = port;
    }
    
    /**
     * The method getColor() returns a normalized color value from the sensor and can be
     * useful if outputting the color to an RGB LED or similar. To
     * read the raw color, use GetRawColor().
     * 
     * The color sensor works best when within a few inches from an object in
     * well lit conditions (the built in LED is a big help here!). The farther
     * an object is the more light from the surroundings will bleed into the 
     * measurements and make it difficult to accurately determine its color.
     * @return Color object representing the color read by the sensor.
     */
    public Color getColor()
    {
    	return colorSensor.getColor();
    }
    
    /**
     * Return raw value from the IR sensor.
     * @return Raw color value.
     */
    public int getIR()
    {
    	return colorSensor.getIR();
    }
    
    /**
     * Get raw proximity value from sensor. Largest value when object is close,
     * smallest value when far away
     * @return Proximity value 0 to 2047.
     */
    public int getProximity()
    {
    	return colorSensor.getProximity();
    }
    
    /**
     * Get raw color values from the sensor.
     * @return RawColor object.
     */
    public RawColor getRawColor()
    {
    	return colorSensor.getRawColor();
    }
    
    /**
     * Get raw value of red sensor.
     * @return Red value.
     */
    public int getRed()
    {
    	return colorSensor.getRed();
    }
        
    /**
     * Get raw value of blue sensor.
     * @return Blue value.
     */
    public int getBlue()
    {
    	return colorSensor.getBlue();
    }
    
    /**
     * Get raw value of green sensor.
     * @return Green value.
     */
    public int getGreen()
    {
    	return colorSensor.getGreen();
    }
    
    /**
     * Add a color to match to the color matcher function.
     * @param targetColor Target RGB color to match.
     */
    public void addColorMatch(Color targetColor)
    {
    	colorMatcher.addColorMatch(targetColor);
    }
    
    /**
     * Match sample color to a specific target color previously 
     * added to the color matcher function. Returns the TARGET color
     * that matches the sample color above a confidence level. Returns
     * null if no target color matches the sample color to or above
     * confidence level.
     * @param sampleColor Color to check.
     * @return ColorMatch result object or null if no match.
     */
    public ColorMatchResult matchColor(Color sampleColor)
    {
    	return colorMatcher.matchColor(sampleColor);
    }
    
    /**
     * Match sample color to closest target color previously 
     * added to the color matcher function. Returns the TARGET color
     * closest to the Sample color and a confidence level. Confidence
     * level returned is important as this function always returns a target color
     * as some target is "closest" to the sample though maybe not very
     * close. High confidence means the two colors are very close to matching.
     * @param sampleColor Color to check.
     * @return ColorMatch result object.
     */
    public ColorMatchResult matchClosestColor(Color sampleColor)
    {
    	return colorMatcher.matchClosestColor(sampleColor);
    }
    
    /**
     * Get a Color object to add to the color matcher function. Here the value
     * of the RGB values are the standard RGB values range 0-255 normalized to
     * a range of 0-1. So full red (rgb=255,0,0) would be 1,0,0.
     * @param red	Red value of target color.
     * @param green	Green value of target color.
     * @param blue	Blue value of target color.
     * @return A Color object.
     */
    public static Color getMatchColor(double red, double green, double blue)
    {
    	return new Color(red, green, blue);
    }
    
    /**
     * Set confidence level applied to exact color matching. Defaults to .95.
     * @param confidenceLevel Percent / 100. 95% would be .95.
     */
    public void setConfidenceThreshold(double confidenceLevel)
    {
    	colorMatcher.setConfidenceThreshold(confidenceLevel);
    }
    
    /**
     * Clears all colors from the color matcher function.
     */
    public void resetColorMatcher()
    {
    	colorMatcher = new ColorMatch();
    }
    
    /**
     * Return a string with the RGB values for the specified Color.
     * @param color The Color to format.
     * @return The R,G,B values as a string.
     */
    public String getRGB(Color color)
    {
    	return String.format("%.0f,%.0f,%.0f", color.red * 255.0, color.green * 255.0, color.blue * 255.0);
    }
    
    /**
     * Return a string with the specified RGB values.
     * @param r Red value 0-255.
     * @param g Green value 0-255.
     * @param b Blue value 0-255.
     * @return RGB values as a string.
     */
    public String getRGB(int r, int g, int b)
    {
    	return String.format("%d,%d,%d", r, g, b);
    }
        
    /**
     * Same as getRGB() using the current sensor Color.
     */
    public String getRGB()
    {
    	Color color = getColor();
    	return getRGB(color);
    }
    
    /**
     * Get the name of the color specified by the Color object.
     * @param color Color object to name.
     * @return The name of the color.
     */
    public String getColorName(Color color)
    {
    	return ColorUtil.getColorNameFromRgb((int)(color.red * 255.0), (int)(color.green * 255.0), (int)(color.blue * 255.0));
    }
    
    /**
     * Get the name of the color specified by the RGB values.
     * @param r Red value 0-255.
     * @param g Green value 0-255.
     * @param b Blue value 0-255.
     * @return The name of the color.
     */
    public String getColorName(int r, int g, int b)
    {
    	return ColorUtil.getColorNameFromRgb(r, g, b);    	
    }
    
    /**
     * Get the name of the current sensor color. 
     * @return The name of the color.
     */
    public String getColorName()
    {
    	Color color = getColor();
    	return getColorName(color);
    	//return getColorName(255, 255, 5);
    }
	
    @Override
    public void initSendable( SendableBuilder builder )
    {
    	builder.setSmartDashboardType("RevColorSensor");
    	builder.addBooleanProperty(".controllable", () -> false, null);
    	builder.addBooleanProperty("Connected", () -> colorSensor.isConnected(), null);
    	builder.addStringProperty("RGB", () -> getRGB(), null);
    	builder.addStringProperty("Color", () -> getColorName(), null);
    	builder.addDoubleProperty("Proximity", () -> getProximity(), null);
    	builder.addDoubleProperty("R", this::getRed, null);
    	builder.addDoubleProperty("G", this::getGreen, null);
    	builder.addDoubleProperty("B", this::getBlue, null);
    }
}
