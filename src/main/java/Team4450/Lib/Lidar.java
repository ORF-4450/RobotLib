package Team4450.Lib;

import edu.wpi.first.math.filter.LinearFilter;
import edu.wpi.first.math.filter.MedianFilter;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;

// Thanks to Barlow Robotics and GirlsOfSteel Robotics for this code

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalSource;

/**
 * Wrapper class for LIDAR V3 distance measuring unit. Connected to RoboRio
 * DIO port wired for PWM.
 */
public class Lidar implements Sendable
{
	private Counter	counter;
	private int 	printedWarningCount = 5;
	private double 	offset = -10;	// Based on testing.
	
	private MedianFilter	mf = new MedianFilter(20);
	private double			medianCm;
	
	/**
     * Create an object for a LIDAR V3 attached to some digital input on the roboRIO using PWM wiring.
     * See doc for details on the wiring.
     * @param source The DigitalInput or DigitalSource where the LIDAR V3 is attached (ex: new DigitalInput(9))
     */
	public Lidar(DigitalSource source)
	{
		counter = new Counter(source);
        counter.setMaxPeriod(1.0);
        // Configure for measuring rising to falling pulses
        counter.setSemiPeriodMode(true);
        counter.reset();
        
        SendableRegistry.addLW(this, "Lidar", source.getChannel());
        SendableRegistry.setName((Sendable) counter, "LidarCounter");
        SendableRegistry.disableLiveWindow((Sendable) source);
        SendableRegistry.disableLiveWindow(counter);
    }

    /**
     * Get distance in centimeters. This is the median of the last 20 distance
     * samples from the Lidar.
     * @param rounded True to round the result.
     * @return Distance in centimeters.
     */
    public double getDistanceCm(boolean rounded) 
    {
    	double cm;

    	/* If we haven't seen the first rising to falling pulse, then we have no measurement.
    	 * This happens when there is no LIDAR plugged in.
    	 */
    	
    	if (counter.get() < 1 & printedWarningCount > 0) 
    	{
   			Util.consoleLog("Lidar: waiting for distance measurement");
    		
   			printedWarningCount--;
   			
    		return 0;
    	}
    	
    	/* getPeriod returns time in seconds. The hardware resolution is microseconds.
    	 * The LIDAR unit sends a high signal for 10 microseconds per cm of distance.
    	 */
    	
    	cm = (counter.getPeriod() * 1000000.0 / 10.0) + offset;
    	
    	if (rounded) cm = Math.floor( cm * 10 ) / 10;
    	
    	medianCm = mf.calculate(cm);
    	
    	return medianCm;
    }

    /**
     * Get distance in inches. This is the median of the last 20 samples 
     * from the Lidar.
     * @param rounded True to round the result.
     * @return Distance in inches.
     */
    public double getDistanceIn(boolean rounded) 
    {
    	double in = getDistanceCm(rounded) * 0.393700787;
    	
    	if (rounded) in = Math.floor(in * 10) / 10;
    	
    	return in;
    }

    /**
     * Set offset added to Lidar measurement to correct for error in
     * Lidar unit. Defaults to -10. Use Test mode to check and adjust
     * and the set value via this function.
     * @param offset Correction value in centimeters
     */
    public void setOffset(double offset)
    {
    	this.offset = offset;
    }
	
    @Override
    public void initSendable( SendableBuilder builder )
    {
    	builder.setSmartDashboardType("Lidar");
    	builder.addBooleanProperty(".controllable", () -> false, null);
    	builder.addDoubleProperty("Distance(cm)", () -> medianCm, this::setOffset);
    	builder.addDoubleProperty("Distance(in)", () -> getDistanceIn(false), null);
    	builder.addDoubleProperty("Offset(cm)", () -> offset, this::setOffset);
    }
}
