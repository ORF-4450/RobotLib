# RobotLib
[![Build Status](https://travis-ci.org/ORF-4450/RobotLib.svg?branch=master)](https://travis-ci.org/ORF-4450/RobotLib)

FRC Team 4450 Robot Control program library classes.

These are the library classes created by the Olympia Robotics Federation (FRC Team 4450).
This library is delivered via jar file for use in our robot control programs.

## How to download
### GradleRIO
Add the maven repository jitpack.io to your build.gradle file:
```
repositories {
	maven { url 'https://jitpack.io' }
}
```
Then add this library as a dependency in your build.gradle file:
```
dependencies {
	//... The rest of the dependencies
	compile 'com.github.MoSadie:RobotLib:cbd8fe518e'	//TODO Update this to correct repo and tag.
}
```
Javadocs, source attachment, and importing the jars will be done automatically after building the project.

### WpiLib Library Manager (Manual)
Download the RobotLib jar files from [here](releases/latest) to your WpiLib user library directory located at:
C:\Users\yourusername\wpilib\user\java\lib. 
Restart Eclipse. RobotLib will be integrated into your robot projects. After that, if RobotLib changes, just download the new RobotLib jars to the lib directory and refresh your robot project. To make the doc available
configure project build path item for RobotLib to locate the javadoc in archive RobotLib.jar with path `/`.

## How to update
### GradleRIO
Change the tag at the end of the string to the latest version and rebuild the project while connected to the internet.

### WpiLib Library Manager (Manual)
Follow the same instructions as above for downloading and installing the library, just overwrite the existing RobotLib.jar and RobotLib-sources.jar
***************************************************************************************************************
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
