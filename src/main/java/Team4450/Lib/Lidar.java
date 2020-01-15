package Team4450.Lib;

// Thanks to Barlow Robotics and GirlsOfSteel Robotics for this code

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.DigitalSource;

/**
 * Wrapper class for LIDAR V3 distance measuring unit. Connected to RoboRio
 * DIO port wired for PWM.
 */
public class Lidar
{
	private Counter	counter;
	private int 	printedWarningCount = 5;
	private int 	offset = 0;
	
	/**
     * Create an object for a LIDAR V3 attached to some digital input on the roboRIO using PWM wiring.
     * @param source The DigitalInput or DigitalSource where the LIDAR V3 is attached (ex: new DigitalInput(9))
     */
	public Lidar(DigitalSource source)
	{
		counter = new Counter(source);
        counter.setMaxPeriod(1.0);
        // Configure for measuring rising to falling pulses
        counter.setSemiPeriodMode(true);
        counter.reset();
	}

    /**
     * Get distance in centimeters.
     * @param rounded True to round the result.
     * @return Distance in centimeters.
     */
    public double getDistanceCm(boolean rounded) 
    {
    	double cm;

    	/* If we haven't seen the first rising to falling pulse, then we have no measurement.
    	 * This happens when there is no LIDAR plugged in.
    	 */
    	
    	if (counter.get() < 1) 
    	{
   			Util.consoleLog("Lidar: waiting for distance measurement");
    		
    		return 0;
    	}
    	
    	/* getPeriod returns time in seconds. The hardware resolution is microseconds.
    	 * The LIDAR unit sends a high signal for 10 microseconds per cm of distance.
    	 */
    	
    	cm = (counter.getPeriod() * 1000000.0 / 10.0) + offset;
    	
    	if(!rounded) 
    	{
    		return cm;
    	}else {
    		return Math.floor( cm * 10 ) / 10;
    	}
    }
    
    /**
     * Get distance in inches.
     * @param rounded True to round the result.
     * @return Distance in inches.
     */
    public double getDistanceIn(boolean rounded) 
    {
    	double in = getDistanceCm(true) * 0.393700787;
    	
    	if(!rounded) 
    	{
    		return in;
    	} else {
    		return Math.floor(in*10)/10;
    	}
    }
   
    /**
     * Set offset added to Lidar measurement to correct for error in
     * Lidar unit.
     * @param offset Correction value in centimeters
     */
    public void setOffset(int offset)
    {
    	this.offset = offset;
    }
}
