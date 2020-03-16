package Team4450.Lib.Wpilib;

/*----------------------------------------------------------------------------*/
/* Copyright (c) 2008-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

/**
 * This interface allows PIDController to write it's results to its output.
 *
 * Copied from Wpilib so we can keep using it if WPI removes it.
 */
@FunctionalInterface
public interface PIDOutput 
{
  /**
   * Set the output to the value calculated by PIDController.
   *
   * @param output the value calculated by PIDController
   */
  void pidWrite(double output);
}