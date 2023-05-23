package Team4450.Lib;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.util.sendable.SendableRegistry;
import edu.wpi.first.hal.util.BoundaryException;

/**
 * This class implements a PID Control Loop.
 * 
 * Does all computation synchronously (i.e. the calculate() function must be called by the user from his own thread)
 * 
 * This class courtesy of Team 254 with modifications.
 */

public class SynchronousPID implements Sendable, AutoCloseable
{
	private static int	instances;
	
	private String m_name;
    private double m_P; 	// factor for "proportional" control.
    private double m_I; 	// factor for "integral" control.
    private double m_D; 	// factor for "derivative" control.
    private double m_F; 	// factor for feed forward gain.
    private double m_maximumOutput = 1.0; 	// maximum output.
    private double m_minimumOutput = -1.0; 	// minimum output.
    private double m_maximumInput = 0.0; 	// maximum input - limit setpoint to this.
    private double m_minimumInput = 0.0; 	// minimum input - limit setpoint to this.
    private boolean m_continuous = false; 	// do the end points wrap around? eg. Absolute encoder.
    private double m_prevError = 0.0; 	// the prior sensor input (used to compute velocity).
    private double m_totalError = 0.0; 	// the sum of the errors for use in the integral calc.
    private double m_setpoint = 0.0;
    private double m_error = 0.0;
    private double m_result = 0.0;
    private double m_last_input = Double.NaN;
    private double m_last_time_called = Double.NaN;
    private double m_deadband = 0.0; // If the absolute error is less than dead band.
                                     // then treat error for the proportional term as 0.

    @SuppressWarnings("unused")
	private SynchronousPID() 
    {
    }

    /**
     * Allocate a PID object with the given constants for P, I, D
     *
     * @param p
     *            the proportional coefficient
     * @param i
     *            the integral coefficient
     * @param d
     *            the derivative coefficient
     */
    public SynchronousPID(double p, double i, double d) 
    {
    	this("", p, i, d, 0);
    }

    /**
     * Allocate a PID object with the given constants for P, I, D
     * @param name 
     * 			  Title in Live Window, will have instance number and -PID appended.
     * @param p
     *            the proportional coefficient
     * @param i
     *            the integral coefficient
     * @param d
     *            the derivative coefficient
     */
    public SynchronousPID(String name, double p, double i, double d) 
    {
    	this(name, p, i, d, 0);
    }

    /**
     * Allocate a PID object with the given constants for P, I, D, F
     *
     * @param p
     *            the proportional coefficient
     * @param i
     *            the integral coefficient
     * @param d
     *            the derivative coefficient
     * @param f
     *            the feed forward gain coefficient
     */
    public SynchronousPID(double p, double i, double d, double f) 
    {
    	this("", p, i, d, f);
    }

    /**
     * Allocate a PID object with the given constants for P, I, D, F
     *
     * @param name 
     * 			  Title in Live Window, will have instance number and -PID appended.
     * @param p
     *            the proportional coefficient
     * @param i
     *            the integral coefficient
     * @param d
     *            the derivative coefficient
     * @param f
     *            the feed forward gain coefficient
     */
    public SynchronousPID(String name, double p, double i, double d, double f) 
    {
        m_P = p;
        m_I = i;
        m_D = d;
        m_F = f;

        instances++;
        
        m_name = String.format("%s(%d)-PID", name, instances);
        
        if (name.isEmpty())
        	SendableRegistry.addLW(this, "SynchronousPID", instances);
        else
        	SendableRegistry.addLW(this, m_name);

        Util.consoleLog("%s", m_name);
    }
    
    /**
     * Returns the name of the object instance.
     * @return Name of this object instance.
     */
    public String getName()
    {
    	return m_name;
    }
    
    /**
     * Sets the name of the object instance.
     * @param name Name of this object instance.
     */
    public void setName(String name)
    {
    	Util.consoleLog("%s", name);
    	
    	m_name = name + "-PID";
    	
    	SendableRegistry.setName(this, m_name);
    }
    
    /**
     * Release resources in preparation to destroy this object.
     */
    public void close()
    {
    	Util.consoleLog("%s", m_name);

    	SendableRegistry.remove(this);
    }

    /**
     * Read the input, calculate the output accordingly, and return the result. This should be called at a constant
     * rate by the user (ex. in a timed thread or execute/periodic function in a command or subsystem)
     *
     * @param input
     *            the input.
     * @param dt
     *            time passed since previous call to calculate in seconds.
     * @return
     *            the output.           
     */
    public double calculate(double input, double dt) 
    {
    	//Util.consoleLog();
    	
        if (dt < 1E-6) dt = 1E-6;
        
        m_last_input = input;
        
        m_error = m_setpoint - input;
        
        if (m_continuous) 
        {
            if (Math.abs(m_error) > (m_maximumInput - m_minimumInput) / 2) 
            {
                if (m_error > 0) 
                    m_error = m_error - m_maximumInput + m_minimumInput;
                else 
                    m_error = m_error + m_maximumInput - m_minimumInput;
            }
        }

        if ((m_error * m_P < m_maximumOutput) && (m_error * m_P > m_minimumOutput)) 
            m_totalError += m_error * dt;
        else 
            m_totalError = 0;

        // Don't blow away m_error so as to not break derivative but apply deadband.
        double proportionalError = Math.abs(m_error) < m_deadband ? 0 : m_error;

        m_result = (m_P * proportionalError) + (m_I * m_totalError) + (m_D * (m_error - m_prevError) / dt)
                	+ (m_F * m_setpoint);
        
        m_prevError = m_error;
        
        if (m_result > m_maximumOutput) 
            m_result = m_maximumOutput;
        else if (m_result < m_minimumOutput) 
            m_result = m_minimumOutput;
        
        return m_result;
    }

    /**
     * Read the input, calculate the output accordingly, and return the result. This should be called at a constant
     * rate by the user (ex. in a timed thread or execute/periodic function in a command or subsystem). The time
     * passed since previous call is tracked internally.
     *
     * @param input
     *            the input.
     * @return
     *            the output.           
     */
    public double calculate(double input) 
    {
    	if (m_last_time_called == Double.NaN) m_last_time_called = Util.timeStamp();
    		
    	double elapsedTime = Util.getElaspedTime(m_last_time_called);
    	 
    	m_last_time_called = Util.timeStamp();
    	 
    	return calculate(input, elapsedTime);
    }
    
    /**
     * Set the PID controller gain parameters. Set the proportional, integral, and differential coefficients.
     *
     * @param p
     *            Proportional coefficient
     * @param i
     *            Integral coefficient
     * @param d
     *            Differential coefficient
     */
    public void setPID(double p, double i, double d) 
    {
        m_P = p;
        m_I = i;
        m_D = d;
    }

    /**
     * Set the PID controller gain parameters. Set the proportional, integral, and differential coefficients.
     *
     * @param p
     *            Proportional coefficient
     * @param i
     *            Integral coefficient
     * @param d
     *            Differential coefficient
     * @param f
     *            Feed forward coefficient
     */
    public void setPID(double p, double i, double d, double f) 
    {
        m_P = p;
        m_I = i;
        m_D = d;
        m_F = f;
    }

    /**
     * Get the Proportional coefficient
     *
     * @return proportional coefficient
     */
    public double getP() 
    {
    	//Util.consoleLog("%s: p=%.4f", m_name, m_P);
    	
        return m_P;
    }
    
    /**
     * Set the Proportional coefficient
     * 
     * @param p The proportional coefficient
     */
    public void setP(double p)
    {
    	//Exception e = new Exception(m_name + ": setP");
    	//Util.logException(e);
    	//Util.consoleLog("%s: p=%.4f", m_name, p);
    	
    	m_P = p;
    }

    /**
     * Get the Integral coefficient
     *
     * @return integral coefficient
     */
    public double getI() 
    {
        return m_I;
    }
    
    /**
     * Set the Integral coefficient
     * 
     * @param i The integral coefficient
     */
    public void setI(double i)
    {
    	m_I = i;
    }

    /**
     * Get the Differential coefficient
     *
     * @return differential coefficient
     */
    public double getD() 
    {
        return m_D;
    }
    
    /**
     * Set the Differential coefficient
     * 
     * @param d The differential coefficient
     */
    public void setD(double d)
    {
    	m_D = d;
    }

    /**
     * Get the Feed forward coefficient
     *
     * @return feed forward coefficient
     */
    public double getF() 
    {
        return m_F;
    }
    
    /**
     * Set the Feed Forward coefficient
     * 
     * @param f The fee forward coefficient
     */
    public void setF(double f)
    {
    	m_F = f;
    }
    
    /**
     * Return the current PID result This is always centered on zero and constrained the the max and min outputs.
     *
     * @return the latest calculated output
     */
    public double get()
    {
    	//Util.consoleLog();
    	
        return m_result;
    }

    /**
     * Set the PID controller to consider the input to be continuous, Rather then using the max and min in as
     * constraints, it considers them to be the same point and automatically calculates the shortest route to the
     * setpoint.
     *
     * @param continuous 
     *            Set to true turns on continuous, false turns off continuous
     */
    public void setContinuous(boolean continuous) 
    {
        m_continuous = continuous;
    }

    public void setDeadband(double deadband) 
    {
        m_deadband = deadband;
    }

    /**
     * Set the PID controller to consider the input to be continuous, Rather then using the max and min in as
     * constraints, it considers them to be the same point and automatically calculates the shortest route to the
     * setpoint.
     */
    public void setContinuous() 
    {
        this.setContinuous(true);
    }

    /**
     * Sets the maximum and minimum values expected from the input.
     *
     * @param minimumInput
     *            the minimum value expected from the input
     * @param maximumInput
     *            the maximum value expected from the output
     */
    public void setInputRange(double minimumInput, double maximumInput) 
    {
        if (minimumInput > maximumInput) 
            throw new BoundaryException("Lower bound is greater than upper bound");
        
        m_minimumInput = minimumInput;
        m_maximumInput = maximumInput;
        
        setSetpoint(m_setpoint);
    }

    /**
     * Sets the minimum and maximum values to write.
     *
     * @param minimumOutput
     *            the minimum value to write to the output
     * @param maximumOutput
     *            the maximum value to write to the output
     */
    public void setOutputRange(double minimumOutput, double maximumOutput) 
    {
        if (minimumOutput > maximumOutput) 
            throw new BoundaryException("Lower bound is greater than upper bound");
        
        m_minimumOutput = minimumOutput;
        m_maximumOutput = maximumOutput;
    }

    /**
     * Set the setpoint for the PID controller
     *
     * @param setpoint
     *            the desired setpoint
     */
    public void setSetpoint(double setpoint) 
    {
        if (m_maximumInput > m_minimumInput) 
        {
            if (setpoint > m_maximumInput) 
                m_setpoint = m_maximumInput;
            else if (setpoint < m_minimumInput) 
                m_setpoint = m_minimumInput;
            else 
                m_setpoint = setpoint;
        } 
        else 
            m_setpoint = setpoint;
    }

    /**
     * Returns the current setpoint of the PID controller
     *
     * @return the current setpoint
     */
    public double getSetpoint() 
    {
        return m_setpoint;
    }

    /**
     * Returns the current difference of the input from the setpoint
     *
     * @return the current error
     */
    public double getError() 
    {
        return m_error;
    }

    /**
     * Returns the current input to the calculate() function.
     *
     * @return the current input.
     */
    public double getInput() 
    {
        return m_last_input;
    }
 
    /**
     * Return true if the error is within the tolerance
     * @param tolerance Tolerance value 0..t
     * @return true if the error is less than the tolerance
     */
    public boolean onTarget(double tolerance) 
    {
        return m_last_input != Double.NaN && Math.abs(m_last_input - m_setpoint) < tolerance;
    }

    /**
     * Reset all internal terms.
     */
    public void reset() 
    {
        m_last_input = Double.NaN;
        m_prevError = 0;
        m_totalError = 0;
        m_result = 0;
        m_setpoint = 0;
    }

    /**
     * Reset the error accumulated for integration (i term).
     */
    public void resetIntegrator() 
    {
        m_totalError = 0;
    }

    public String getState() 
    {
        String lState = "";

        lState += "Kp: " + m_P + "\n";
        lState += "Ki: " + m_I + "\n";
        lState += "Kd: " + m_D + "\n";
        lState += "Kf: " + m_F + "\n";

        return lState;
    }

    public String getType() 
    {
        return "PIDController";
    }
    
	/**
	 * Initialize the Sendable. Called by SmartDashboard.putData().
	 * @param builder SendableBuilder object.
	 */
	@Override
	public void initSendable( SendableBuilder builder )
	{
		builder.setSmartDashboardType("SynchronousPID");
    	builder.addBooleanProperty(".controllable", () -> true, null);
	    builder.addDoubleProperty("1 p", this::getP, this::setP);
	    builder.addDoubleProperty("2 i", this::getI, this::setI);
	    builder.addDoubleProperty("3 d", this::getD, this::setD);
	    builder.addDoubleProperty("4 f", this::getF, this::setF);
	    builder.addDoubleProperty("5 setpoint", this::getSetpoint, this::setSetpoint);
	    builder.addDoubleProperty("6 input", this::getInput, null);
	    builder.addDoubleProperty("7 error", this::getError, null);
	    builder.addDoubleProperty("8 result", this::get, null);
	}
    
}