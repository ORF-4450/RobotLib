
package Team4450.Lib;

import Team4450.Lib.CameraServer;
import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;

//import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Timer;

/**
 * USB camera feed task. Runs as a thread separate from Robot class.
 * Manages one or more usb cameras feeding their images to the 
 * CameraServer class to send to the DS.
 * Uses UsbCamera objects instead of NI image library.
 */

public class CameraFeed2 extends Thread
{
	public	double			frameRate = 30;		// frames per second
	private UsbCamera		currentCamera, cam1, cam2;
	private Image 			frame;
	private CameraServer 	server;
	
	/**
	 * @param robot Robot class instance.
	 */

	public CameraFeed2(boolean isComp)
	{
		try
		{
    		Util.consoleLog();
    
    		this.setName("CameraFeed2");
    		
            // Frame that will contain current camera image.
            frame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_RGB, 0);
            
            // Server that we'll give the image to.
            server = CameraServer.getInstance();
            server.setQuality(50);
    
            // Open camera.
            // Using one camera at this time.

            if (isComp)
    			cam1 = new UsbCamera("cam1");
    		else
    			cam1 = new UsbCamera("cam0");
    			
            cam2 = cam1;
            
            // Open cameras when using 2 cameras.
            
//            if (isComp)
//            {
//    			cam1 = new UsbCamera("cam1");
//    			cam2 = new UsbCamera("cam0");
//            }
//            else
//            {
//            	cam1 = new UsbCamera("cam0");
//    			cam2 = new UsbCamera("cam1");
//            }
            
            // Set starting camera.

            currentCamera = cam1;
		}
		catch (Throwable e) {e.printStackTrace(Util.logPrintStream);}
	}
	
	// Run thread to read and feed camera images. Called by Thread.start().
	public void run()
	{
		Util.consoleLog();
		
		try
		{
			Util.consoleLog();

			while (true)
			{
				UpdateCameraImage();
		
				Timer.delay(1 / frameRate);
			}
		}
		catch (Throwable e) {e.printStackTrace(Util.logPrintStream);}
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
	 * Stop image feed, ie close camera stream.
	 */
	public void EndFeed()
	{
		try
		{
    		Util.consoleLog();

    		cam1.stopCapture();
    		
    		currentCamera = cam1 = null;
		}
		catch (Throwable e)	{e.printStackTrace(Util.logPrintStream);}
	}
	
	/**
	 * Change the camera to get images from the other camera. 
	 */
	public void ChangeCamera()
    {
		Util.consoleLog();

		if (currentCamera.equals(cam1))
			currentCamera = cam2;
		else
			currentCamera = cam1;
    }
    
	 // Get an image from current camera and give it to the server.
    private void UpdateCameraImage()
    {
    	try
    	{
    		if (currentCamera != null)
    		{	
    			frame = currentCamera.getImage();
    				
            	server.setImage(frame);
    		}
		}
		catch (Throwable e) {e.printStackTrace(Util.logPrintStream);}
    }
}
