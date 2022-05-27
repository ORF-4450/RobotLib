package Team4450.Lib;

import java.util.ArrayList;

/**
 * Computes a rolling average over a fixed set of double values.
 */
public class RollingAverage
{
	private int		numValues, size;
	private double	sum;
	
	private ArrayList<Double>	buffer;
	
	/**
	 * Create Rolling Average calculator instance with specified max number of values.
	 * Also known as a moving average.
	 * @param size Max number of values in the rolling average.
	 */
	public RollingAverage(int size)
	{
		buffer = new ArrayList<Double>(size);
		
		this.size = size;
	}

	/**
	 * Gets the average of the values in the Rolling Average buffer.
	 * @return The average of the available values.
	 */
	public double get()
	{
		if (numValues == 0) return 0;
		
		return sum / numValues;
	}
	
	/**
	 * Adds a new item to the Rolling Average buffer. If buffer is full,
	 * the oldest item is deleted and the new item added. The new average
	 * is returned.
	 * @param value The value to be added to the rolling average.
	 * @return The rolling average after adding the new value.
	 */
	public double calculate(double value)
	{
		if (numValues > size - 1)
		{
			sum -= buffer.get(size - 1);
			buffer.remove(size - 1);
			numValues--;
		}
		
		sum += value;
		
		buffer.add(0, value);
		
		numValues++;
		
		return sum / numValues;
	}
}
