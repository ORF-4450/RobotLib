
package Team4450.Lib;

import Team4450.Robot9.Robot;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;

/**
 * USB camera feed task. Runs as a thread separate from Robot class.
 * Manages one or more usb cameras feeding their images to the 
 * CameraServer class to send to the DS.
 * Uses NI image library to access cameras directly.
 */

public class CameraFeed extends Thread
{
	public	int		 			cam1 = -1, cam2 = -1;
	public	double				frameRate = 30;		// frames per second
	private int 				currentCamera;
	private Image 				frame;
	private CameraServer 		server;
	private boolean				cameraChangeInProgress;
	private Robot				robot;
	private static CameraFeed	cameraFeed;

	// Create single instance of this class and return that single instance to any callers.
	
	/**
	 * Get a reference to global CameraFeed object.
	 * @param robot Robot class instance.
	 * @return Reference to global CameraFeed object.
	 */
	  
	public static CameraFeed getInstance(Robot robot) 
	{
		Util.consoleLog();
		
		if (cameraFeed == null) cameraFeed = new CameraFeed(robot);
	    
	    return cameraFeed;
	}

	// Private constructor means callers must use getInstance.
	// This is the singleton class model.
	
	private CameraFeed(Robot robot)
	{
		try
		{
    		Util.consoleLog();
    
    		this.setName("CameraFeed");
    		
    		this.robot = robot;
    		
    		// Get camera ids by supplying camera name ex 'cam0', found on roborio web interface.
    		// This code sets up first two cameras found using all the camera names known on our
    		// 2 RoboRios.
    		
    		// Camera initialization based on robotid from properties file.
    		
    		if (robot.isComp)
    		{
        		try
        		{
        			cam1 = NIVision.IMAQdxOpenCamera("cam1", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        		}
        		catch (Exception e) {}
        		
        		try
        		{
        			cam2 = NIVision.IMAQdxOpenCamera("cam0", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        		}
        		catch (Exception e) {}
    		}
    		
    		if (robot.isClone)
    		{
    			Util.consoleLog("in clone");
    			
        		try
        		{
        			cam1 = NIVision.IMAQdxOpenCamera("cam0", NIVision.IMAQdxCameraControlMode.CameraControlModeController);
        		}
        		catch (Exception e) {}
    		}
    		
    		// trace the camera ids.
    		
    		Util.consoleLog("cam1=%d, cam2=%d", cam1, cam2);
            
            // Frame that will contain camera image.
            frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
            
            // Server that we'll give the image to.
            server = CameraServer.getInstance();
            server.setQuality(50);
    
            // Set starting camera.
            currentCamera = cam1;
            
            ChangeCamera(currentCamera);
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	// Run thread to read and feed camera images. Called by Thread.start().
	public void run()
	{
		try
		{
			Util.consoleLog();

			while (true)
			{
				if (!cameraChangeInProgress) UpdateCameraImage();
		
				Timer.delay(1 / frameRate);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	/**
	 * Get last image read from camera.
	 * @return Image Last image from camera.
	 */
	public Image CurrentImage()
	{
		Util.consoleLog();
		
		return frame;
	}
	
	/**
	 * Stop feed, ie close camera stream.
	 */
	public void EndFeed()
	{
		try
		{
    		Util.consoleLog();
    
    		if (currentCamera != -1) NIVision.IMAQdxStopAcquisition(currentCamera);
		}
		catch (Throwable e)	{Util.logException(e);}
	}
	
	/**
	 * Change the camera to get images from to a different one 
	 * @param newId Camera number to change to. Use cam1 or cam2.
	 */
	public void ChangeCamera(int newId)
    {
		Util.consoleLog("newid=%d", newId);
		
		if (newId == -1) return;
		
		try
		{
    		cameraChangeInProgress = true;

    		NIVision.IMAQdxStopAcquisition(currentCamera);
    		
        	NIVision.IMAQdxConfigureGrab(newId);
        	
        	NIVision.IMAQdxStartAcquisition(newId);
        	
        	currentCamera = newId;
        	
        	cameraChangeInProgress = false;
		}
		catch (Throwable e)	{Util.logException(e);}
    }
    
	 // Get an image from current camera and give it to the server.
    private void UpdateCameraImage()
    {
    	try
    	{
    		if (currentCamera != -1)
    		{	
            	NIVision.IMAQdxGrab(currentCamera, frame, 1);
                
            	server.setImage(frame);
    		}
		}
		catch (Throwable e) {Util.logException(e);}
    }
}
