
package Team4450.Lib;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.MjpegServer;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.UsbCameraInfo;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;

/**
 * USB camera feed task. Runs as a thread separate from Robot class.
 * Manages one or more usb cameras feeding their images to the 
 * WpiLib CameraServer class to send to the DS. Creates camera object 
 * for each detected camera and starts them capturing images. We then 
 * loop on a thread getting the current image from the currently selected 
 * camera and pass the image to the camera server which passes the 
 * image to the driver station.
 * 
 * You can decide to not start the thread and get/put images yourself.
 * 
 * You can set target rectangle(s) or an array of contours to be drawn
 * on the outgoing image.
 */

public class CameraFeed extends Thread
{
	private UsbCamera			currentCamera;
	private int					currentCameraIndex;
	private ArrayList			<UsbCamera>cameras = new ArrayList<UsbCamera>();
	private Mat 				image;
	private static CameraFeed	cameraFeed;
	private boolean				initialized;
	private MjpegServer			mjpegServer;
	private CvSink				imageSource;
	private CvSource			imageOutputStream;
	private boolean				changingCamera;
	
	private ArrayList<Rect>			targetRectangles;
	private ArrayList<MatOfPoint>	contours;
	private Scalar 					targetColor = new Scalar(0, 0, 255);
	private int						targetWidth = 1;
	
	// Default Camera settings
	private final int 		imageWidth = 320; 		//640;
	private final int 		imageHeight = 240;		//480;
	//public final double 	fovH = 48.0;
	//public final double 	fovV = 32.0;
	private final double	frameRate = 20;			// frames per second
	//private final int		whitebalance = 4700;	// Color temperature in K
	private final int		brightness = 50;		// 0 - 100
	//private final int		exposure = 50;			// 0 - 100

	// Create single instance of this class and return that single instance to any callers.
	
	/**
	 * Get a reference to global CameraFeed object.
	 * @return Reference to global CameraFeed object.
	 */
	  
	public static CameraFeed getInstance() 
	{
		Util.consoleLog();
		
		if (cameraFeed == null) cameraFeed = new CameraFeed();
	    
	    return cameraFeed;
	}

	// Private constructor means callers must use getInstance.
	// This is the singleton class model.
	
	private CameraFeed()
	{
		UsbCameraInfo	cameraInfo, cameraList[];
		UsbCamera		camera;

		try
		{
    		Util.consoleLog();
    
    		this.setName("CameraFeed");
    		
    		// Make sure OpenCV native libraries are loaded.

    		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    		image = new Mat();
    		
            // Create Mjpeg stream server.
            
            mjpegServer = CameraServer.getInstance().addServer("4450-mjpegServer", 1180);

            // Create image source.
            
            imageSource = new CvSink("4450-CvSink");
            
            // Create output image stream.
            
            imageOutputStream = new CvSource("4450-CvSource", VideoMode.PixelFormat.kMJPEG, imageWidth, imageHeight, (int) frameRate);
            
            mjpegServer.setSource(imageOutputStream);
            
            // Create cameras by getting the list of cameras detected by the RoboRio and
            // creating camera objects and storing them in an arraylist so we can switch
            // between them.
    		
    		cameraList = UsbCamera.enumerateUsbCameras();
    		
    		for(int i = 0; i < cameraList.length; ++i) 
    		{
    			cameraInfo = cameraList[i];
    			
    			Util.consoleLog("dev=%d name=%s path=%s", cameraInfo.dev, cameraInfo.name, cameraInfo.path);
    			
    			camera = new UsbCamera("cam" + cameraInfo.dev, cameraInfo.dev);
    			
    			updateCameraSettings(camera);
    			
    			cameras.add(camera);
    		}

            initialized = true;
            
            // Set starting camera.

            ChangeCamera();
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	/**
	 * Update camera with default settings.
	 */
	private void updateCameraSettings(UsbCamera camera) 
	{
		Util.consoleLog();

		camera.setResolution(imageWidth, imageHeight);
		camera.setFPS((int) frameRate);
		camera.setExposureAuto();
		camera.setWhiteBalanceAuto();
		camera.setBrightness(brightness);
	}
	
	/**
	 * Return current camera. May be used to configure camera settings.
	 * @return UsbCamera Current camera, may be null. 
	 */
	public UsbCamera getCamera()
	{
		Util.consoleLog();
		
		return currentCamera;
	}
	
	/**
	 * Return the number of cameras in the internal camera list.
	 * @return The camera count.
	 */
	public int getCameraCount()
	{
		Util.consoleLog();
		
		if (!initialized) return 0;
		
		if (cameras.isEmpty()) return 0;
		
		return cameras.size();
	}
	/**
	 * Return camera from internal camera list. May be used to configure camera settings.
	 * @param index Camera index in internal camera list (0 based).
	 * @return Requested camera or null.
	 */
	public UsbCamera getCamera(int index)
	{
		Util.consoleLog("%d", index);
		
		if (!initialized) return null;
		
		if (cameras.isEmpty()) return null;
		
		if (index < 0 || index >= cameras.size()) return null;
		
		return cameras.get(index);
	}
	
	/**
	 * Return named camera from internal camera list. May be used to configure camera settings.
	 * @param name Camera name, will be "camN" where N is the device number.
	 * @return Requested camera or null.
	 */
	public UsbCamera getCamera(String name)
	{
		UsbCamera	camera;
		
		Util.consoleLog("%s", name);
		
		if (!initialized) return null;
		
		if (cameras.isEmpty()) return null;

		for(int i = 0; i < cameras.size(); ++i) 
		{
			camera = cameras.get(i);

			if (camera.getName().equals(name)) return camera;
		}
		
		return null;
	}

	// Run thread to read and feed camera images. Called by Thread.start().
	
	public void run()
	{
		Util.consoleLog();
		
		if (!initialized) return;
		
		if (cameras.isEmpty()) return;
		
		try
		{
			while (!isInterrupted())
			{
				if (!changingCamera) UpdateCameraImage();
		
				//Timer.delay(1 / frameRate);
			}
		}
		catch (Throwable e) {Util.logException(e);}
	}
	
	/**
	 * Get last image read from camera.
	 * @return Last image from camera.
	 */
	public Mat getCurrentImage()
	{
		Util.consoleLog();
		
	    synchronized (this) 
	    {
	    	if (image == null)
	    		return null;
	    	else
	    		return image.clone();
	    }
	}
	
	/**
	 * Get an image from the current camera. Returns null if no image
	 * is available.
	 * @return The image.
	 */
	public Mat getImage()
	{
		long	result;
		
		if (!initialized) return null;
		
		if (cameras.isEmpty()) return null;
		
		try
		{
			if (currentCamera != null)
			{	
			    synchronized (this) 
			    {
			    	result = imageSource.grabFrame(image);
			    }
			    
			    if (result != 0) return image;
			}
		}
		catch (Throwable e)	{Util.logException(e);}
		
		return null;
	}
	
	/**
	 * Stop image feed, ie close cameras stop feed thread, release the
	 * singleton cameraFeed object.
	 */
	public void EndFeed()
	{
		if (!initialized) return;

		try
		{
    		Util.consoleLog();

    		//Thread.currentThread().interrupt();
    		cameraFeed.interrupt();
    		
    		for(int i = 0; i < cameras.size(); ++i) 
    		{
    			currentCamera = cameras.get(i);
    			currentCamera.close();
    		}
    		
    		currentCamera = null;

    		mjpegServer = null;
	
    		cameraFeed = null;
		}
		catch (Throwable e)	{Util.logException(e);}
	}
	
	/**
	 * Change the camera to get images from the next camera in the list of cameras.
	 * At end of list loops around to the first. 
	 */
	public void ChangeCamera()
    {
		Util.consoleLog();
		
		if (!initialized) return;
		
		if (cameras.isEmpty()) return;
		
		changingCamera = true;
		
		if (currentCamera == null)
			currentCamera = cameras.get(currentCameraIndex);
		else
		{
			currentCameraIndex++;
			
			if (currentCameraIndex == cameras.size()) currentCameraIndex = 0;
			
			currentCamera = cameras.get(currentCameraIndex);
		}
		
		Util.consoleLog("current=(%d) %s", currentCameraIndex, currentCamera.getName());
		
	    synchronized (this) 
	    {
	    	imageSource.setSource(currentCamera);
	    }
	    
	    changingCamera = false;
	    
	    Util.consoleLog("end");
    }
    
	/**
	 * Change current camera to specific camera.
	 * @param camera Usb camera object.
	 */
	public void changeCamera(UsbCamera camera)
	{
		Util.consoleLog("%s", camera.getName());
		
		if (!initialized) return;
		
		if (cameras.isEmpty()) return;
		
		changingCamera = true;
		
		currentCamera = camera;
		
	    synchronized (this) 
	    {
	    	imageSource.setSource(camera);
	    }
	    
	    changingCamera = false;
	    
	    Util.consoleLog("end");
	}
	
	// Get an image from current camera and give it to the server.
    
//	private void UpdateCameraImage()
//    {
//		long	result;
//		
//		try
//		{
//			if (currentCamera != null)
//			{	
//			    synchronized (this) 
//			    {
//			    	result = imageSource.grabFrame(image);
//			    }
//			    
//			    if (result != 0) imageOutputStream.putFrame(image);
//			}
//		}
//		catch (Throwable e)	{Util.logException(e);}
//    }
	
	private void UpdateCameraImage()
	{
		Mat image = getImage();
		
		if (image != null) putImage(image);
	}
	
	/**
	 * Write an image to the camera server output.
	 * @param image The image to write.
	 */
	public void putImage(Mat image)
	{
		synchronized(this)
		{
			if (contours != null) Imgproc.drawContours(image, contours, -1, targetColor, targetWidth);
			
			if (targetRectangles != null)
			{
				for (Rect rect: targetRectangles) 
					Imgproc.rectangle(image, 
							new Point(rect.x, rect.y), 
							new Point(rect.x + rect.width, rect.y +  rect.height), 
							targetColor, targetWidth);
			}
			
			imageOutputStream.putFrame(image);
		}
	}
	
	/**
	 * Add a rectangle to be drawn on the camera feed images.
	 * @param rectangle Rectangle to draw, null to clear all rectangles.
	 */
	public void addTargetRectangle(Rect rectangle)
	{
		synchronized(this)
		{
			if (rectangle == null)
			{
				targetRectangles = null;
				return;
			}
		
			if (targetRectangles == null) targetRectangles = new ArrayList<Rect>();
		
			targetRectangles.add(rectangle);
		}
	}
	
	/**
	 * Set an array of contours to be drawn on the camera feed images.
	 * @param contours The array of contours, null to clear.
	 */
	public void setContours(ArrayList<MatOfPoint> contours)
	{
		synchronized(this)
		{
			this.contours = contours;
		}
	}
	
	/**
	 * Set the line color used to draw targets/contours.
	 * @param color B,G,R color values. Defaults to 0,0,255 (red).
	 */
	public void setTargetColor(Scalar color)
	{
		targetColor = color;
	}
	
	/**
	 * Set width of line used to draw targets/contours.
	 * @param width Line width, defaults to 1.
	 */
	public void setTargetWidth(int width)
	{
		if (width > 0) targetWidth =  width;
	}
	
	/**
	 * Write a list of usb cameras known to the RoboRio to the log.
	 */
	public static void listCameras()
	{
		UsbCameraInfo	cameraInfo, cameraList[];
		
		try
		{
			cameraList = UsbCamera.enumerateUsbCameras();
			
			for(int i = 0; i < cameraList.length; ++i) 
			{
				cameraInfo = cameraList[i];
				Util.consoleLog("dev=%d name=%s path=%s", cameraInfo.dev, cameraInfo.name, cameraInfo.path);
			}
		}
		catch (Throwable e)	{Util.logException(e);}
	}
}
