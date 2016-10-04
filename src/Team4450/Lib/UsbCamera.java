package Team4450.Lib;

import com.ni.vision.NIVision;
import com.ni.vision.NIVision.Image;
import com.ni.vision.NIVision.ImageType;

import edu.wpi.first.wpilibj.vision.USBCamera;

/**
 * Manages USB camera by wrapping WpiLib USB camera class. 
 */
public class UsbCamera
{
	private String 		name;
	private USBCamera 	cam;
	private Image 		image = NIVision.imaqCreateImage(ImageType.IMAGE_RGB, 0);

	// Camera settings - Static
	public static final int 	width = 640;
	public static final int 	height = 480;
	public static final double 	fovH = 48.0;
	public static final double 	fovV = 32.0;
	public static final int 	fps = 4;

	// Camera settings - Dynamic
	public int whitebalance = 4700;	// Color temperature in K, -1 is auto
	public int brightness = -1;		// 0 - 100, -1 is "do not set"
	public int exposure = -1;		// 0 - 100, -1 is "auto"

	/**
	 * Create UsbCamera object.
	 * @param name Camera name from RoboRio mapping
	 * of connected Usb cameras.
	 */
	public UsbCamera(String name) 
	{
		this.name = name;
	}
	
	/**
	 * Returns the name of the camera.
	 * @return Camera name.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Returns running state of camera.
	 * @return True if capturing images, false if not.
	 */
	public boolean isRunning() 
	{
		return (cam != null);
	}
	
	/**
	 * Update camera with current settings fields values.
	 */
	public void updateSettings() 
	{
		if (!isRunning()) return;

		cam.setSize(width, height);
		cam.setFPS(fps);
		
		if (exposure >= 0) 
			cam.setExposureManual(exposure);
		else 
			cam.setExposureAuto();
		
		if (whitebalance >= 0) 
			cam.setWhiteBalanceManual(whitebalance);
		else 
			cam.setWhiteBalanceAuto();
		
		if (brightness >= 0) cam.setBrightness(brightness);
		
		cam.updateSettings();
	}
	
	/**
	 * Start the camera capturing images.
	 */
	public void startCapture() 
	{
		if (isRunning()) return;

		cam = new USBCamera(name);
		
		cam.openCamera();
		
		updateSettings();

		cam.startCapture();
	}

	/**
	 * Stops camera image capturing.
	 */
	public void stopCapture() 
	{
		if (isRunning()) cam.closeCamera();
	
		cam = null;
	}
	
	/**
	 * Get the last image captured.
	 * @return Last image captured.
	 */
	public Image getImage() 
	{
		startCapture();		
		
		cam.getImage(image);
		
		return image;
	}
}
