package Team4450.Lib.Wpilib;

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

import edu.wpi.first.util.sendable.SendableBuilder;
//import edu.wpi.first.wpilibj.Controller;
import edu.wpi.first.wpilibj.Notifier;
//import edu.wpi.first.wpilibj.PIDBase;

/**
 * Class implements a PID Control Loop.
 *
 * <p>Creates a separate thread which reads the given PIDSource and takes care of the integral
 * calculations, as well as writing the given PIDOutput.
 *
 * <p>This feedback controller runs in discrete time, so time deltas are not used in the integral
 * and derivative calculations. Therefore, the sample rate affects the controller's behavior for a
 * given set of PID constants.
 *
 * Copied from Wpilib so we can still use it if WPI removes it.
 */

public class PIDController extends PIDBase implements Controller, AutoCloseable 
{
  Notifier m_controlLoop = new Notifier(this::calculate);

  /**
   * Allocate a PID object with the given constants for P, I, D, and F.
   *
   * @param Kp     the proportional coefficient
   * @param Ki     the integral coefficient
   * @param Kd     the derivative coefficient
   * @param Kf     the feed forward term
   * @param source The PIDSource object that is used to get values
   * @param output The PIDOutput object that is set to the output percentage
   * @param period the loop time for doing calculations in seconds.
   *               This particularly affects calculations of
   *               the integral and differential terms.
   *               The default is 0.05 (50ms).
   */
  public PIDController(double Kp, double Ki, double Kd, double Kf, PIDSource source,
                       PIDOutput output, double period) 
  {
    super(Kp, Ki, Kd, Kf, source, output);
    m_controlLoop.startPeriodic(period);
  }

  /**
   * Allocate a PID object with the given constants for P, I, D and period.
   *
   * @param Kp     the proportional coefficient
   * @param Ki     the integral coefficient
   * @param Kd     the derivative coefficient
   * @param source the PIDSource object that is used to get values
   * @param output the PIDOutput object that is set to the output percentage
   * @param period the loop time for doing calculations in seconds.
   *               This particularly affects calculations of
   *               the integral and differential terms.
   *               The default is 0.05 (50ms).
   */
  public PIDController(double Kp, double Ki, double Kd, PIDSource source, PIDOutput output,
                       double period) 
  {
    this(Kp, Ki, Kd, 0.0, source, output, period);
  }

  /**
   * Allocate a PID object with the given constants for P, I, D, using a 50ms period.
   *
   * @param Kp     the proportional coefficient
   * @param Ki     the integral coefficient
   * @param Kd     the derivative coefficient
   * @param source The PIDSource object that is used to get values
   * @param output The PIDOutput object that is set to the output percentage
   */
  public PIDController(double Kp, double Ki, double Kd, PIDSource source, PIDOutput output) 
  {
    this(Kp, Ki, Kd, source, output, kDefaultPeriod);
  }

  /**
   * Allocate a PID object with the given constants for P, I, D, using a 50ms period.
   *
   * @param Kp     the proportional coefficient
   * @param Ki     the integral coefficient
   * @param Kd     the derivative coefficient
   * @param Kf     the feed forward term
   * @param source The PIDSource object that is used to get values
   * @param output The PIDOutput object that is set to the output percentage
   */
  public PIDController(double Kp, double Ki, double Kd, double Kf, PIDSource source,
                       PIDOutput output) 
  {
    this(Kp, Ki, Kd, Kf, source, output, kDefaultPeriod);
  }

  @Override
  public void close() {
    m_controlLoop.close();
    m_thisMutex.lock();
    try {
      m_pidOutput = null;
      m_pidInput = null;
      m_controlLoop = null;
    } finally {
      m_thisMutex.unlock();
    }
  }

  /**
   * Begin running the PIDController.
   */
  @Override
  public void enable() 
  {
    m_thisMutex.lock();
    
    try 
    {
      m_enabled = true;
    } finally {
      m_thisMutex.unlock();
    }
  }

  /**
   * Stop running the PIDController, this sets the output to zero before stopping.
   */
  @Override
  public void disable() 
  {
    // Ensures m_enabled check and pidWrite() call occur atomically
    m_pidWriteMutex.lock();
    
    try {
      m_thisMutex.lock();
      
      try 
      {
        m_enabled = false;
      } finally {
        m_thisMutex.unlock();
      }

      m_pidOutput.pidWrite(0);
    } finally {
      m_pidWriteMutex.unlock();
    }
  }

  /**
   * Set the enabled state of the PIDController.
   * @param enable True to enable, false to disable.
   */
  public void setEnabled(boolean enable) 
  {
    if (enable) 
    {
      enable();
    } else {
      disable();
    }
  }

  /**
   * Return PIDController is enable status.
   * @return True if enabled.
   */
  public boolean isEnabled() 
  {
    m_thisMutex.lock();
    
    try {
      return m_enabled;
    } finally {
      m_thisMutex.unlock();
    }
  }

  /**
   * Reset the previous error, the integral term, and disable the controller.
   */
  @Override
  public void reset() 
  {
    disable();

    super.reset();
  }

  @Override
  public void initSendable(SendableBuilder builder) 
  {
    super.initSendable(builder);
    builder.addBooleanProperty("enabled", this::isEnabled, this::setEnabled);
  }
  
  /**
   * Set logging of setpoint, input, error and output values on each
   * controller loop.
   * @param enable True to log values.
   */
  public void enableLogging(boolean enable) 
  {
    m_thisMutex.lock();
    try {
      m_logging = enable;
    } finally {
      m_thisMutex.unlock();
    }
 
  }
  
  /**
   * Turn off output, that is sending the result of pid calculation to the
   * object receiving the controller output. This is used for testing without
   * actually driving the controlled object.
   * @param disable True to disable output.
   */
  public void disableOuptut(boolean disable) 
  {
    m_thisMutex.lock();
    try {
      m_disableOutput = disable;
    } finally {
      m_thisMutex.unlock();
    }
  }
}
