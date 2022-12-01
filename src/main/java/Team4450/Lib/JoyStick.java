
package Team4450.Lib;

import java.util.EventObject;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import edu.wpi.first.wpilibj.Joystick;

/** 
 * This class provides an interface to the Joystick buttons. It monitors
 * the state of the JS buttons and raises events when button state changes.
 * Also provides stick axis information with dead zone applied.
 * 
 * This class should not be used with Command based robot programs. Use
 * WpiJoyStick instead.
 */

public class JoyStick
{
	private final Joystick	joyStick;
	private 				Set<JoyStickEventListener> listeners = new HashSet<JoyStickEventListener>();
	private					Set<JoyStickButton> buttons = new HashSet<JoyStickButton>();
	private Thread			monitorJoyStickThread;
	private String			joyStickName = "";
	private double			deadZone = 0.1, deadZoneX = 0, deadZoneY = 0, invertX = 1.0, invertY = 1.0;
	
	/**
	 * Constructor which adds all JoyStick buttons to be monitored.
	 * @param joystick JoyStick object.
	 * @param name Identifying name for the JoyStick object.
	 */
	
	public JoyStick(Joystick joystick, String name)
	{
		JoyStickButton	button;
		
		Util.consoleLog(name);

		joyStick = joystick;
		joyStickName = name;
		
		// Build set of all the joystick buttons which will be monitored.
		
		button = new JoyStickButton(JoyStickButtonIDs.TRIGGER);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.BACK_LEFT);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.BACK_RIGHT);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.LEFT_FRONT);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.LEFT_REAR);
		buttons.add(button);

		button = new JoyStickButton(JoyStickButtonIDs.RIGHT_FRONT);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.RIGHT_REAR);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.TOP_BACK);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.TOP_LEFT);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.TOP_MIDDLE);
		buttons.add(button);
		
		button = new JoyStickButton(JoyStickButtonIDs.TOP_RIGHT);
		buttons.add(button);

		Start();
	}
	
	/**
	 * Constructor which adds single JoyStick button to be monitored.
	 * @param joystick JoyStick object representing the GamePad.
	 * @param name Identifying name for the JoyStick object.
	 * @param button Enum value identifying button to add.
	 */
	public JoyStick(Joystick joystick, String name, JoyStickButtonIDs button)
	{
		Util.consoleLog(name);

		this.joyStick = joystick;
		joyStickName = name;
		
		if (button != null) AddButton(button);
	}
	
	/**
	 * Add additional JoystickButton button to be monitored.
	 * @param button Id value identifying button to add.
	 * @return New button added or existing button.
	 */
	public JoyStickButton AddButton(JoyStickButtonIDs button)
	{
		Util.consoleLog("%s (%s)", joyStickName, button.name());
	
		JoyStickButton jsButton = FindButton(button);
		
		if (jsButton == null)
		{
			jsButton = new JoyStickButton(button);
			buttons.add(jsButton);
		}

		return jsButton;
	}
	
	/**
	 * Find JoyStick button by id in the list of registered buttons.
	 * @param button Id value identifying button to find.
	 * @return Button reference or null if not found.
	 */
	public JoyStickButton FindButton(JoyStickButtonIDs button)
	{
		Util.consoleLog("%s (%s)", joyStickName, button.name());

        for (JoyStickButton jsButton: buttons) 
        	if (jsButton.id.value == button.value) return jsButton;

        return null;
	}
	
	/**
	 * Return the Wpilib Joystick object underlying this RobotLib JoyStick object.
	 * @return Reference to Joystick.
	 */
	public Joystick getJoyStick()
	{
		return joyStick;
	}
	
	/**
	 * Returns the name of this joystick object.
	 * @return Joystick name.
	 */
	public String getName()
	{
		return joyStickName;
	}

	/**
	 *  Call to start JoyStick button monitoring once all buttons are added.
	 */
	public void Start()
	{
		Util.consoleLog(joyStickName);

		if (monitorJoyStickThread != null) monitorJoyStickThread.interrupt();

		monitorJoyStickThread = new MonitorJoyStick();
		
		monitorJoyStickThread.start();
	}

	/**
	 * Stop button monitoring.
	 */
	public void Stop()
	{
		Util.consoleLog(joyStickName);

		if (monitorJoyStickThread != null) monitorJoyStickThread.interrupt();
		
		monitorJoyStickThread = null;
	}
	
	/**
	 * Release any JoyStick resources.
	 */
	public void dispose()
	{
		Util.consoleLog(joyStickName);
		
		if (monitorJoyStickThread != null) monitorJoyStickThread.interrupt();
	}
	
	/**
	 * Set global axis dead zone. Applied if no axis specific dead zone is set.
	 * @param dz Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZone(double dz) throws Exception
	{
		Util.checkRange(dz, 0.0, 1.0, "Dead Zone.");
		
		deadZone = dz;
	}
	
	/**
	 * Set X axis dead zone.
	 * @param dzX Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZoneX(double dzX) throws Exception
	{
		Util.checkRange(dzX, 0.0, 1.0, "Dead Zone.");
		
		deadZoneX = dzX;
	}
	
	/**
	 * Set Y axis dead zone.
	 * @param dzY Dead zone value, 0.0 to 1.0.
	 * @throws Exception
	 */
	public void deadZoneY(double dzY) throws Exception
	{
		Util.checkRange(dzY, 0.0, 1.0, "Dead Zone.");
		
		deadZoneY = dzY;
	}
	
	/**
	 * Invert the X axis output.
	 * @param invert True is invert, false is normal.
	 */
	public void invertX(boolean invert)
	{
		if (invert)
			invertX = -1.0;
		else
			invertX = 1.0;
	}
	
	/**
	 * Invert the Y axis output.
	 * @param invert True is invert, false is normal.
	 */
	public void invertY(boolean invert)
	{
		if (invert)
			invertY = -1.0;
		else
			invertY = 1.0;
	}

	/**
	 * Get JoyStick X axis deflection value.
	 * @return X axis deflection.
	 */
	public double GetX()
	{
		double x;
		
		x = joyStick.getX();
		
		if (deadZoneX > 0 && Math.abs(x) < deadZoneX)
			x = 0;
		else if (deadZone > 0 && Math.abs(x) < deadZone)
			x = 0;
		
		return x * invertX;
	}
	
	/**
	 * Get JoyStick Y axis deflection value.
	 * @return Y axis deflection.
	 */
	public double GetY()
	{
		double y;
		
		y = joyStick.getY();
		
		if (deadZoneY > 0 && Math.abs(y) < deadZoneY)
			y = 0;
		else if (deadZone > 0 && Math.abs(y) < deadZone)
			y = 0;
		
		return y * invertY;
	}
	
	// JoyStick Button Monitor thread.
	
	private class MonitorJoyStick extends Thread
	{
		boolean previousState;
  	
		MonitorJoyStick()
		{
			Util.consoleLog(joyStickName);
			
			this.setName("Monitor " + joyStickName);
	    }
		
//		public void interrupt()
//		{
//	    	Util.consoleLog("Joystick.%s.MonitorJoyStick.interrupt", joyStickName);
//	    	super.interrupt();
//		}
		
	    public void run()
	    {
	    	Util.consoleLog(joyStickName);
	    	
	    	try
	    	{
    	    	while (!isInterrupted())
    	    	{
    	    		// Loop through the set of joystick buttons and read the value of each
    	    		// saving the button state and raising events for change in button state.
    	    		
    	            for (JoyStickButton button: buttons) 
    	            {
          	    		if (joyStick.getRawButton(button.id.value))
          				{
          					previousState = button.currentState;
          					button.currentState = true;
          						
          					if (!previousState) //TODO Double check this
          					{
          						button.latchedState = !button.latchedState;
          						
          						notifyButtonDown(button);
          					}
          				}
          	    		else
          				{
          					previousState = button.currentState;
          					button.currentState = false;
          					
          					if (previousState) notifyButtonUp(button);
          				}
    	            }
    	            
    	            // We sleep since JS updates come from DS every 20ms or so. We wait 30ms so this thread
    	            // does not run at the same time as the teleop thread.
    	            sleep(30);
    	    	}
	    	}
	    	catch (InterruptedException e) {}
	    	catch (Throwable e) {Util.logException(e);}
	    }
	}	// end of MonitorJoystick thread class.
	
	/**
	 * Get the current state of a registered button.
	 * @param requestedbutton Button id to check.
	 * @return True if pressed, false if not.
	 */
	public boolean GetCurrentState(JoyStickButtonIDs requestedbutton)
	{
      for (JoyStickButton button: buttons) 
      	if (button.id.equals(requestedbutton)) return button.currentState;
      
      return false;
	}
	
	/**
	 * Gets the latched state of a registered button. When buttons
	 * are pressed, the latch state is toggled and retained. Latched is in effect
	 * a persistent button press. Press and it latches, press again and it unlatches.
	 * @param requestedbutton Button id to check.
	 * @return True if button latched, false if not.
	 */
	public boolean GetLatchedState(JoyStickButtonIDs requestedbutton)
	{
      for (JoyStickButton button: buttons) 
      	if (button.id.equals(requestedbutton)) return button.latchedState;
      
      return false;
	}
	
	// Event Handling classes.
	
    /**
     *  Event description class returned to event handlers.
     */
	public class JoyStickEvent extends EventObject 
	{
	  private static final long serialVersionUID = 1L;

	  public JoyStickButton	button;
	
	  public JoyStickEvent(Object source, JoyStickButton button) 
	  {
		  super(source);
		  this.button = button;
	  }
	}
  
    /**
     *  Java Interface definition for event listener. Actual listener implements
     *  the actions associated with button up and down events.
     */
	public interface JoyStickEventListener extends EventListener 
	{	
		public void ButtonDown(JoyStickEvent JoyStickEvent);
      
		public void ButtonUp(JoyStickEvent JoyStickEvent);
	}
  
    /**
     * Register a JoyStickEventListener object to receive events.
     * @param listener JoyStickEventListener object to receive events.
     */
	public void addJoyStickEventListener(JoyStickEventListener listener) 
	{
		this.listeners.add(listener);
	}
   
    /**
     * Remove the specified JoyStickEventListener object from event notification.
     * @param listener JoyStickEventListener object to remove.
     */
	public void removeJoyStickEventListener(JoyStickEventListener listener) 
	{
		this.listeners.remove(listener);
	}  
    
    /**
     * Remove all JoyStickEventListener objects from event notification.
     */
    public void removeAllJoyStickEventListeners()
    {
    	this.listeners.clear();
    }
  
	// Notify all registered handlers of button up event.
  
	private void notifyButtonUp(JoyStickButton button) 
	{
		for (JoyStickEventListener JoyStickEventListener: listeners) 
		{
			JoyStickEventListener.ButtonUp(new JoyStickEvent(this, button));
		}
	}
  
	// Notify all registered handlers of button down event.
  
	private void notifyButtonDown(JoyStickButton button) 
	{
		for (JoyStickEventListener JoyStickEventListener: listeners) 
		{
			JoyStickEventListener.ButtonDown(new JoyStickEvent(this, button));
		}
	}

    /**
    *  Button object which contains button id and current and latched state values of the button
    *  when contained in an event and if you directly request button state.
    */
	public class JoyStickButton
	{
		public JoyStickButtonIDs	id;
		public boolean				currentState, latchedState;
  
		public JoyStickButton(JoyStickButtonIDs buttonID)
		{
			id = buttonID;
		}
	}
  
    /**
    *  JoyStick button id enumeration. 
    */
	public enum JoyStickButtonIDs
	{
        TOP_MIDDLE (3),
        TOP_LEFT (4),
        TOP_RIGHT (5),
        TRIGGER (1),
        TOP_BACK (2),
        LEFT_FRONT (6),
        LEFT_REAR (7),
        RIGHT_FRONT (11),
        RIGHT_REAR (10),
        BACK_LEFT (8),
        BACK_RIGHT (9);
          
        public int value;
          
        private JoyStickButtonIDs(int value) 
        {
        	this.value = value;
        }
	};
}
