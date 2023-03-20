# RobotLib
[![Build CI](https://github.com/MoSadie/RobotLib/actions/workflows/gradleCI.yml/badge.svg?branch=actions)](https://github.com/MoSadie/RobotLib/actions/workflows/gradleCI.yml)
[![](https://jitpack.io/v/ORF-4450/robotlib.svg)](https://jitpack.io/#ORF-4450/robotlib)

FRC Team 4450 Robot Control program library classes.

These are the library classes created by the Olympia Robotics Federation (FRC Team 4450).
This library is delivered via jar file for use in our robot control programs.

## How to download

### GradleRIO (2019 full release and later)

Add the file RobotLib.json from this project to the vendordeps directory of your robot project. You can use the
file url from the list above to add to VSCode with the WPILib: Manage Vendor Libraries command. Check the RobotLib version inside that file and set the version you wish to pull from Jitpack. Normally the version in RobotLib.json 
will point to the current release. Recompile while connected to the internet.

Import of Javadocs, source attachment, and jars will be done automatically when building the robot project.

### WARNING ###

This library no longer contains its dependencies. You have to import the dependent libraries in the robot project
consuming this library. As of 3.0 these libraries are needed: Navx, CTRE_Phoenix. You can copy the json files for
these libraries from RobotLib vendordeps folder to your robot project vendordeps folder. Don't forget to do a 
Gradle refresh after changing any vendordeps file. As of 3.4.0 you also need REVColorSensorV3.

### NOTICE

v2.x is not compatible with 2019 (full release) or later FRC robotics platform. Only use this library for pre-2019 projects that will run on a RoboRIO with a pre-2019 image.

### NOTICE TO DEVELOPERS
Read the documentation in build.gradle for the procedure to do development on this
library and then generate a release on Github and Jitpack.
***************************************************************************************************************
Version 4.4.0

*	Reduce swerve not moving threshold voltage from .25 to .05 in the drive controllers. .25 too high causing
	some problems. 
*	Add code to swerve steering controller to save (burn) SparkMax configuration set in our code to flash memory
	so SM will retain that config if SM power is interrupted.

R. Corn, March 20, 2023

Version 4.3.1

*	Remove unneeded logging from swerve code.
*	Fix bug in SwerveModuleFactory.resetSteerAngleToAbsolute() that was causing random steering failures when
	called from setStartPosition(). Without it working, setStartPosition would fail when used after a reboot.

R. Corn, February 13, 2023

Version 4.3.0

*	Move swerve drive code into this library from Swerve-Demo.

R. Corn, February 1, 2023

Version 4.2.0

*	Update for 2023 WPILib.
*	Add Util.saveProperties method to save the Properties object back to disk.
*	Add user specified package path removal marker to strip package path from method names in the log file.
	This allows logging to be used with any program.

R. Corn, January 11, 2023

Version 4.1.0

*	Added DeadZone wrapper class for use with WPILib Joystick and XboxController X and Y axis get functions
	to provide dead zone and inversion support. This will allow us to more easily use the WPILib classes 
	instead of our own. We had have been using our own classes just for dz and inversion.
*	New WpiJoyStick and XboxController classes. Decided it was cleaner to extend WPILib Joystick and XboxController 
	classes to add functions to set and apply dead zone and inversion and any other customization we might want to 
	add, without all the code for our original interrupt based joystick handler. Will retain DeadZone class.
	
R. Corn, November 22, 2022

Version 4.0.0

*	Updated the logging system to queue log records in memory and write them to disk on a separate thread.
*	Updated the logging system to include output to the RoboRio console.
*	Added absolute position support to SRX/FX encoder classes.
*	Added new wrapper class CANCoder to support the CTRE CANCoder encoder.
*	Fixed problems with encoder sim support when rerunning auto programs without code reboot.
*	Add Sendable support to all appropriate classes.
*	Remove use of our wrapper for WPILib Sendable.
*	Clean up how the singleton classes are implemented adding the INSTANCE reference variable.
*	Add ColorUtil class.
*	Finally got the Lidar sensor hooked up and tested the wrapper class. It is working.
*	Added the RollingAverage utility class.
*	Added the FunctionTracer class to help find long running functions that trigger the WPILib watchdog.
*	Re-tested solenoid slide time used in ValveDA and determined 20ms is enough, down from 50.

R. Corn, June 23, 2022

Version 3.13.0

*	Built-in sim support in SRXMagneticEncoderRelative now working. Dropped our home brew sim support
	based on dummy encoders.
	
R. Corn, April 8, 2022

Version 3.12.0

*	Built-in sim support in NavX now working. Dropped our home brew sim support based on dummy gyros.
*	Fixed issue with NavX sim support not working when start heading not zero.

R. Corn, April 2, 2022

Version 3.11.0

*	Fix design error in NavX class sim support. Sim was not tracking non-zero starting heading. Still working the
	problem with rerunning auto without code download. Sim support works after download but not correctly on rerun.

R. Corn, February 25, 2022

Version 3.10.0

*	Updated for 2022 WPILib:
	Update Gradle to 7.1.1.
	Update build.gradle for 2022 following VSCode project build.gradle.
	Update to 2022 REVLib.

R. Corn, January 22, 2022

Version 3.9.0

*	Added support for built-in simulation in SRXMagneticEncoder and NavX. This was hoped to replace our more
	complex solution using dummy encoders and analog gyro. However, the built-in support for both did not
	work reliably. Could not determine if caused by our code or theirs, but it appear theirs. Will continue
	with our original solution for simulation on these devices via our wrapper classes. Will keep the changes
	for built-in sim support and revisit in the future. Need to stop now and prepare for 2022 WPILib changes.
	
R. Corn, October 20, 2021

Version 3.8.2

*	Due to problems with Travis-ci, replaced the travis deployment steps with github Actions.
*	Fixed a bug in how the Robotlib.json file was being created.

R. Corn, September 29, 2021

Version 3.8.0

*	Fixed bugs in GamePad class.
*	Updated project to use 2021 WpiLib.

R. Corn, September 23 2021

Version 3.7.0

*	Add support for simulation.
*	Add support for TalonFX.
*	Fix various bugs/issues revealed during the extensive testing of simulation and 2021 complex
	autonomous programs. Primarily in NavX and Talon encoder classes.
*	Modify to better support motion profiling and path following.

R. Corn, May 8 2021

Version 3.6.1

*	Fix locking design error in CameraFeed class.
*	Add build.gradle option to fix problem with Javadoc search function when generated by SDK-11.
*	Add build.gradle option to generate Javadoc with frames (will be dropped by Java in the future).
*	Add global gradle.properties option to fix gradle refresh failure: must run under SDK-11.
*	Update launch configurations to point to 2020 FRC SDK and libraries.
*	Minor tweaks and documentation updates.
*	Clean up and better document the procedure to develop and release this library.

R. Corn, November 27 2020

Version 3.6.0

*	Remove all Wpilib deprecation warnings. Almost all warnings related to PIDController class:
	Copied deprecated PID* classes from Wpilib to this library so we can keep using them if we wish.
*	Converted all imports from Wpilib PID* to this library so deprecation warnings go away.
*	Copied Wpilib Sendable class to wrap new SendableRegistry class in a more sensible way and
	get rid of deprecation warnings.
*	Converted our classes that implement Sendable to use our new Sendable.

R. Corn, March 30 2020

Version 3.5.0

*	Add resetColorMatcher() function to REVColorSensor class.

R. Corn, March 4, 2020

Version 3.4.0

*	Add wrapper class for REV ColorSensor V3.
*	Add wrapper class for Lidar V3. Note this class was added but not yet tested.

R. Corn, January 15, 2020

Version 3.3.0

*	Update for 2020 WpiLib and 3rd party library changes.
*	Modify CameraServer class to explicitly load Opencv native library.

R. Corn, January 3, 2020

Version 3.2.0

*	Improve tracing in the PDxShim library classes.
*	Modify JoyStick, LaunchPad and GamePad classes to correct design error and allow these objects to be
	created in the Devices class.
*	Implement internal event monitoring scheme in NavX class that runs on a notifier and supports adding
	automated monitoring functions to NavX that interact with robot code by raising an event. Robot code
	turns event monitoring on/off and sets the time interval of the monitoring notifier.
*	Implement a collision detection function in NavX using the above event monitoring capability. Notifies robot
	code of a collision via an event. Robot code sets the collision threshold in g.
*	Fix locking design error in CameraFeed class introduced in v2.3.	

R. Corn, June 12, 2019

Version 3.1.1

*	Fix typo in 3.1.0.

R. Corn, March 13, 2019

Version 3.1.0

*	Fix problems in LaunchPad class button enumeration. The constructor that adds all buttons automatically did
	not work correctly due to misunderstanding how the Java enum class works. BUTTON_BLUE(2) and BUTTON_TWO(2) are
	NOT the same thing and are not interchangeable even though they have the same integer value. These are two
	different objects and apparently the case statement compares object ids not the integer value...
*	Add squaredInput() function to Util class.
*	ValveDA and SA now trace the pcm and port the valve is attached to.

R Corn, March 13, 2019

Version 3.0.5

*	Fix errors in LaunchPad constructor that adds all available buttons to event handling.

R Corn, February 26, 2019

Version 3.0.4

*	Minor improvements to ValveSA and DA classes. They now track open/closed state.
*	Fix SRXMagenticEncoderRelative class. The reset(timeout) function was not correct
	due to how the CTRE API handles Talon updates.
*	Modified the tracing function so that when 99 log files are reached, the trace setup function
	will delete the 99th file instead of throwing an exception. The rest of the files are moved
	down so 98 becomes 99, 97 becomes 98 and so on. The most recent file is number 0.

R Corn, January 31, 2019
	
Version 3.0.3

*	Correct Navx class getHeadingR() method documentation, streamline algorithm.
*	Add some editing to make sure appropriate PIDRateType is passed to methods that use it.
*	Add timeout to SRXMagneticEncoder.reset() method to optionally have the method wait for the reset to complete.

R Corn, January 21, 2019

Version 3.0.2 skipped due to Travis failure.

Version 3.0.1

*	Fix bug in Util.round function, was returning float instead of double.
*	In Util.SendableVersion, log ignored exception if the init function fails.

V Vairaperumal, R Corn, January 12, 2019

Version 3.0

*	Update for compatibility with the 2019 changes to WPILib and GradleRIO.

R Corn, S Flo, January 7, 2019

Version 2.4

*	Add "round" function to Util class.
*	Add method to get rotational velocity around selected axis to NavX class.
*	Add inches per second as a velocity unit to SRXMagneticEncoder class.

R Corn, January 5, 2019

Version 2.3

*	Modify CameraFeed class to support drawing target rectangles or contours on the outgoing feed. Research 
	and concept development by Abhinav Gundrala.
*	Modify CameraFeed class to support calling programs reading images directly from the camera and writing 
	images directly to the feed, allowing an external loop to drive the feed.
	
A. Gundrala, R Corn, December 7, 2018
	
Version 2.2

*	Add SendableVersion class.
*	Add functions to CameraFeed class to allow manipulation of individual camera settings.
*	CameraFeed exposure & whitebalance now default to auto.

R Corn, October 30,  2018

Version 2.1

*	Modify NavX class to implement Sendable interface so it can be used with Shuffleboard Gyro widget.
*	Modify NavX class to implement PIDSource interface so it can be used to feed PID controllers.
*	Modify SynchronousPID to implement Sendable interface it can be used with Shuffleboard PIDController widget.

R Corn, October 15, 2018

Version 2.0.1

*	Fix backwards compatibility with ant projects.
*	Fix more issues with gradle/travis/jitpack deployment.
*	The file names in the github release (pre-2019) have changed. They have the fixed text -local appended to
	each file name. This is an artifact of the gradle conversion.

R Corn, September 24, 2018

Version 2.0

*	Convert to Gradle project. Thanks to Sean Flo for figuring all of this out.
*	Add Jitpak support.

R Corn
September 10, 2018

Version 1.15

*	More updates to SRXMagneticEncoderRelative class to support velocity based PID.

R Corn
August 28, 2018

Version 1.14

*	Update SRXMagneticEncoderRelative class adding functions to better support path finding.

R Corn
August 27, 2018

Version 1.13

*	Add methods to Util class to do elapsed time and inches to meters conversion.
*	Add method getHeadingR to Navx class to support path finder.

R Corn
June 11, 2018

Version 1.12

*	Add wrapper class for SRX Magnetic Encoder (relative mode) connected to a CanTalon.

R Corn
June 8, 2018

Version 1.11.1

*	Update Javadoc.

R Corn
May 24, 2018

Version 1.11

*	Enhance JoyStick class with dead zone by axis, optional inverted mode for each axis to support new standard of + motor
	values meaning forward direction. (JS Y axis reports negative values when stick pushed forward)
*	Enhance NavX class to report yaw from a target heading to support navigation by heading.
*	Added a number of numeric editing functions to the Util class. Range checking and clamping.
*	Added SynchronousPID class.

R Corn
May 23, 2018

Version 1.10.4

*	Fixed bug in JoyStick class introduced in update to 2018 (v1.10) that caused the ghost JS button press problem.
*	Adjusted pressure sensor conversion factor to better match gauge readings.

S Flo & R Corn
April 18, 2018

Version 1.10.3

*	Fixed CameraFeed not to run image feed thread if no cameras are detected.

R Corn
March 24, 2018

Version 1.10.2

*	Fix bug in CameraFeed class that caused JVM crash at robot code start up.

R Corn
March 19, 2018

Version 1.10.1

*	Fix problem with log file numbering. Quit working with 2018 update. Had to fix the Java library call as it 
	changed	with the update.
	
R Corn
March 7, 2018

Version 1.10

* 	Initial release for the 2018 season.

S Flo & R Corn
January 17, 2018

Version 1.9.3

*	Minor fixes and clean up of AbsoluteEncoder class.

R Corn
January 4, 2018

Version 1.9.0

*	Add AbsoluteEncoder class.

R Corn
November 15, 2017

Version 1.8.0

*	Minor adjustments to 1.7.0.
*	Added Documentation. Javadoc is included in RobotLib.jar.
*	Cleaned up project references.

R Corn
November 1, 2017

Version 1.7.0

*	Modified to support passing device object instances as well as port numbers where appropriate.

R Corn
September 18, 2017

Version 1.6.1

*	Cleaned up method documentation.

R. Corn
June 1, 2017

Version 1.6

*	Add a low pressure DS alarm LED/logging/correction factor to MonitorCompressor.

R. Corn
May 25, 2017

Version 1.5

*	Add support for air pressure sensor to MonitorCompressor.

Version 1.4

*	Modify NavX class to support NavX-Micro.

R. Corn
May 11, 2017

Version 1.3

*	More updates to distance monitoring classes. Add PDP monitoring class.

R. Corn
March 30, 2017

Version 1.2

*	Updates to distance monitoring classes.

R. Corn
March 15, 2017

Version 1.1

*	Fix errors and clean up library build process.
*	Add wrapper class for NavX MXP navigation board.

R. Corn
February 1, 2017

Version 1.0.1

*   Setup the project to use Travis-CI to compile and release any tagged commits.

S. Flo
January 21, 2017

Version 1.0

*	RobotLib is live and adopted starting with Robot10 project for 2017.

R. Corn
January 21, 2017

Version 0.1

*    Move all library functions out of Robot9 and into this library project. This is a pilot project for now.

R. Corn
December 17, 2015
