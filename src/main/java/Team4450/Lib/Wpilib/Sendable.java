package Team4450.Lib.Wpilib;

import edu.wpi.first.util.sendable.SendableRegistry;

/**
 * Wraps Wpilib Sendable interface to get rid of the deprecation warnings
 * if you used the default methods of Sendable. Uses SendableRegistry as
 * directed by Wpilib authors to implement the default methods of Sendable.
 */
@Deprecated(since = "3.14.0", forRemoval = true)
public interface Sendable extends edu.wpi.first.util.sendable.Sendable
{
	/**
	 * Register this Sendable with the SendableRegistry.
	 * @param name Name of this Sendable.
	 */
	default void registerSendable(String name)
	{
		SendableRegistry.add(this, name);
	}
	
	/**
	 * Register this Sendable with the SendableRegistry. 
	 * @param subSystem Subsystem this sendable is associated with.
	 * @param name Name of this Sendable.
	 */
	default void registerSendable(String subSystem, String name)
	{
		SendableRegistry.add(this, subSystem, name);
	}
	
	/**
	 * Register this Sendable with the SendableRegistry. 
	 * @param moduleType Module type name this Sendable is associated with.
	 * @param channel Channel number the device is plugged into.
	 */
	default void registerSendable(String moduleType, int channel)
	{
		SendableRegistry.add(this, moduleType, channel);
	}
	
	/**
	 * Register this Sendable with the SendableRegistry. 
	 * @param moduleType Module type name this Sendable is associated with.
	 * @param moduleNumber The number of the particular module.
	 * @param channel Channel number the device is plugged into.
	 */
	default void registerSendable(String moduleType, int moduleNumber, int channel)
	{
		SendableRegistry.add(this, moduleType, moduleNumber, channel);
	}

	/**
	 * Remove this Sendable from the SendableRegistry.
	 */
	default void removeSendable()
	{
		SendableRegistry.remove(this);
	}
	
	/**
	 * Update the network tables with this Sendable's values.
	 */
	default void updateSendable()
	{
		SendableRegistry.update(this);
	}
	
	/**
	 * Gets the name of this {@link Sendable} object.
	 *
	 * @return Name
	 */
	default String getName() 
	{
		return SendableRegistry.getName(this);
	}

	/**
	 * Sets the name of this {@link Sendable} object.
	 *
	 * @param name name
	 */
	default void setName(String name) 
	{
		SendableRegistry.setName(this, name);
	}

	/**
	 * Sets both the subsystem name and device name of this {@link Sendable} object.
	 *
	 * @param subsystem subsystem name
	 * @param name device name
	 */
	default void setName(String subsystem, String name) 
	{
		SendableRegistry.setName(this, subsystem, name);
	}

	/**
	 * Sets the name of the sensor with a channel number.
	 *
	 * @param moduleType A string that defines the module name in the label for the value
	 * @param channel    The channel number the device is plugged into
	 */
	default void setName(String moduleType, int channel) 
	{
	    SendableRegistry.setName(this, moduleType, channel);
	}

	/**
	 * Sets the name of the sensor with a module and channel number.
	 *
	 * @param moduleType   A string that defines the module name in the label for the value
	 * @param moduleNumber The number of the particular module type
	 * @param channel      The channel number the device is plugged into (usually PWM)
	 */
	default void setName(String moduleType, int moduleNumber, int channel) 
	{
	    SendableRegistry.setName(this, moduleType, moduleNumber, channel);
	}

	/**
	 * Gets the subsystem name of this {@link Sendable} object.
	 *
	 * @return Subsystem name
	 */
	default String getSubsystem() 
	{
	    return SendableRegistry.getSubsystem(this);
	}

	/**
	 * Sets the subsystem name of this {@link Sendable} object.
	 *
	 * @param subsystem subsystem name
	 */
	default void setSubsystem(String subsystem) 
	{
	    SendableRegistry.setSubsystem(this, subsystem);
	}

	/**
	 * Add a child component.
	 *
	 * @param child child component
	 */
	default void addChild(Object child) 
	{
	    SendableRegistry.addChild(this, child);
	}
}
