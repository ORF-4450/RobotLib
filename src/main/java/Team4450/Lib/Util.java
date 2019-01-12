
package Team4450.Lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.hal.can.CANJNI;
import edu.wpi.first.hal.util.BoundaryException;

/**
 * Provides a set of utility functions.
 */

public class Util
{
	/**
	 * Open a print stream that writes to the log file. Example of use:
	 * exception.printStackTrace(Util.logPrintStream);
	 */
	public static final PrintStream	logPrintStream = new PrintStream(new LoggingOutputStream());

	/**
	 * Logging class for use by other classes to log though this custom logging scheme. All
	 * logging should be done by calls to methods on this class instance or with the 
	 * convenience methods elsewhere in the Util class.
	 */
	public final static Logger logger = Logger.getGlobal();
	
	private static double timeMarker = 0;
	
	// Private constructor means this class cannot be instantiated. All access is static.
	
	private Util()
	{
		
	}
	
	/**
	 * Read properties file from RobRio disk into a Properties object.
	 * @return A Properties object.
	 * @throws IOException
	 */
	public static Properties readProperties() throws IOException
	{
		consoleLog();
		
		Properties props = new Properties();
		
		FileInputStream is = new FileInputStream("/home/lvuser/Robot.properties");
		
		props.load(is);
		
		is.close();
		
		//props.list(new PrintStream(System.out));
		props.list(logPrintStream);

		return props;
	}
	
	/**
	 * Configures and holds (static) classes for our custom logging system. 
	 * Call setup() method to initialize logging. Logging is then done via
	 * the logging convenience methods in the Util class.
	 */
	public static class CustomLogger 
	{
        static private FileHandler 		fileTxt;
        //static private SimpleFormatter	formatterTxt;
        static private LogFormatter		logFormatter;
        
        /**
         *  Initializes our logging system.
         *  Call before using any logging methods.
         *  @throws IOException
         */
        static public void setup() throws IOException 
        {
            // get the global logger to configure it and add a file handler.
            Logger logger = Logger.getGlobal();
                   
            logger.setLevel(Level.ALL);

            // If we decide to redirect system.out to our log handler, then following
            // code will delete the default log handler for the console to prevent
            // a recursive loop. We would only redirect system.out if we only want to
            // log to the file. If we delete the console handler we can skip setting
            // the formatter...otherwise we set our formatter on the console logger.
            
            Logger rootLogger = Logger.getLogger("");

            Handler[] handlers = rootLogger.getHandlers();
            
//            if (handlers[0] instanceof ConsoleHandler) 
//            {
//                rootLogger.removeHandler(handlers[0]);
//                return;
//            }

            logFormatter = new LogFormatter();

            // Set our formatter on the console log handler.
            if (handlers[0] instanceof ConsoleHandler) handlers[0].setFormatter(logFormatter);

            // Now create a handler to log to a file on roboRio "disk".
            
            //if (true) throw new IOException("Test Exception");
            
            if (new File("/home/lvuser/Logging.txt.99").exists() != true)
            	fileTxt = new FileHandler("/home/lvuser/Logging.txt", 0, 99);
            else
            	throw new IOException("Max number of log files reached.");
            
            fileTxt.setFormatter(logFormatter);

            logger.addHandler(fileTxt);
        }
	}
	
	// Our custom formatter for logging output.
	
	private static class LogFormatter extends Formatter 
	{
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:S");
        
        public LogFormatter()
        {
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        }

        public String format(LogRecord rec) 
        {
            StringBuffer buf = new StringBuffer(1024);
            
            buf.append(String.format("<%d>", rec.getThreadID()));
            buf.append(dateFormat.format(new Date(rec.getMillis())));
            buf.append(" ");
            buf.append(formatMessage(rec));
            buf.append("\n");
        
            return buf.toString();
        }
	}
	
	// An output stream that writes to our logging system. Writes data with flush on
	// flush call or on a newline character in the stream.
	
	private static class LoggingOutputStream extends OutputStream 
	{
	    private static final int	DEFAULT_BUFFER_LENGTH = 2048;
	    private boolean 			hasBeenClosed = false;
	    private byte[] 				buf;
	    private int 				count, curBufLength;

	    public LoggingOutputStream()
	    {
	        curBufLength = DEFAULT_BUFFER_LENGTH;
	        buf = new byte[curBufLength];
	        count = 0;
	    }

	    public void write(final int b) throws IOException 
	    {
	        if (hasBeenClosed) {throw new IOException("The stream has been closed.");}
	        
	        // don't log nulls
	        if (b == 0) return;
	        
	        // force flush on newline character, dropping the newline.
	        if ((byte) b == '\n') 
	        {
	        	flush();
	        	return;
	        }
	        
	        // would this be writing past the buffer?
	        if (count == curBufLength) 
	        {
	            // grow the buffer
	            final int newBufLength = curBufLength + DEFAULT_BUFFER_LENGTH;
	            final byte[] newBuf = new byte[newBufLength];
	            System.arraycopy(buf, 0, newBuf, 0, curBufLength);
	            buf = newBuf;
	            curBufLength = newBufLength;
	        }

	        buf[count] = (byte) b;
	        
	        count++;
	    }

	    @SuppressWarnings("deprecation")
		public void flush() 
	    {
	        if (count == 0) return;
	        
	        final byte[] bytes = new byte[count];

	        System.arraycopy(buf, 0, bytes, 0, count);
	        
	        String str = new String(bytes);
	        
	        LCD.consoleLogNoFormat(str);
	        
	        count = 0;
	    }

	    public void close() 
	    {
	        flush();
	        hasBeenClosed = true;
	    }
	}

	/**
     * Returns program location where call to this method is located.
     * @return String containing program location of method called from.
     */
    public static String currentMethod()
    {
        return currentMethod(2);
    }

    private static String currentMethod(Integer level)
    {
        StackTraceElement stackTrace[];
    
        stackTrace = new Throwable().getStackTrace();

        // This scheme depends on having one level in the package name between
        // Team4450 and the class name, ie: Team4450.lib.Util.method. New levels
        // will require rewrite.
        
        try
        {
            String method = stackTrace[level].toString().split("4450.")[1];
            
            int startPos = method.indexOf(".") + 1;
            
            return method.substring(startPos);
        }
        catch (Throwable e)
        {
			return "method not found";
        }

//        try
//        {
//        	return stackTrace[level].toString().split("Robot9.")[1];
//        }
//        catch (Throwable e)
//        {
//        	return stackTrace[level].toString().split("Lib.")[1];
//        }
	}

	// Works the same as LCD.consoleLog but automatically includes the program location from which
	// trace was called.
    
    /**
     * Write message to console log with optional formatting and program location.
     * @param message Message with optional format specifiers for listed parameters.
     * @param parms Parameter list matching format specifiers.
     */
	public static void consoleLog(String message, Object... parms)
	{
		// logs to the console as well as our log file on RR disk.
		logger.log(Level.INFO, String.format("robot: %s: %s", currentMethod(2), String.format(message, parms)));
	}
    
	/**
	 * Write blank line with program location to the console log.
	 */
	public static void consoleLog()
	{
		// logs to the console as well as our log file on RR disk.
		logger.log(Level.INFO, String.format("robot: %s", currentMethod(2)));
	}
    
	/**
	 * Handler for uncaught exceptions. Records thread name and exception to log file.
	 * @param t Thread where exception was thrown.
	 * @param e Exception that was  thrown.
	 */
	public static void uncaughtException(Thread t, Throwable e) 
	{
        consoleLog("Uncaught exception from thread " + t);
        logException(e);
    }
    
	/**
	 * Write exception message to DS console window and exception stack trace to
	 * log file.
	 * @param e The exception to log.
	 */
	public static void logException(Throwable e)
	{
		DriverStation.reportError(e.toString(), false);
		
		e.printStackTrace(Util.logPrintStream);
	}

	/** helper routine to get last received message for a given ID */
	private static long checkMessage(int fullId, int deviceID) 
	{
		ByteBuffer targetID = ByteBuffer.allocateDirect(4);
		ByteBuffer timeStamp = ByteBuffer.allocateDirect(4);

		try 
		{
			targetID.clear();
			targetID.order(ByteOrder.LITTLE_ENDIAN);
			targetID.asIntBuffer().put(0,fullId|deviceID);

			timeStamp.clear();
			timeStamp.order(ByteOrder.LITTLE_ENDIAN);
			timeStamp.asIntBuffer().put(0,0x00000000);
			
			CANJNI.FRCNetCommCANSessionMuxReceiveMessage(targetID.asIntBuffer(), 0x1fffffff, timeStamp);
		
			long retval = timeStamp.getInt();
			
			retval &= 0xFFFFFFFF; /* undo sign-extension */ 
			
			return retval;
		} catch (Exception e) {return -1;}
	}
	
	/** polls for received framing to determine if a device is present.
	 *   This is meant to be used once initially (and not periodically) since 
	 *   this steals cached messages from the robot API.
	 * @return ArrayList of strings holding the names of devices we've found.
	 */
	public static ArrayList<String> listCANDevices() 
	{
		ArrayList<String> retval = new ArrayList<String>();

		/* get timestamp0 for each device */
		long pdp0_timeStamp0; // only look for PDP at '0'
		long []pcm_timeStamp0 = new long[63];
		long []srx_timeStamp0 = new long[63];
		
		pdp0_timeStamp0 = checkMessage(0x08041400,0);
		 
		for(int i = 0; i < 63; ++i) 
		{
			pcm_timeStamp0[i] = checkMessage(0x09041400, i);
			srx_timeStamp0[i] = checkMessage(0x02041400, i);
		}

		/* wait 200ms */
		try 
		{
			Thread.sleep(200);
		} catch (InterruptedException e) {Util.logException(e);}

		/* get timestamp1 for each device */
		long pdp0_timeStamp1; // only look for PDP at '0'
		long []pcm_timeStamp1 = new long[63];
		long []srx_timeStamp1 = new long[63];
		
		pdp0_timeStamp1 = checkMessage(0x08041400,0);
		
		
		for(int i = 0; i < 63; ++i) 
		{
			pcm_timeStamp1[i] = checkMessage(0x09041400, i);
			srx_timeStamp1[i] = checkMessage(0x02041400, i);
		}

		/* compare, if timestamp0 is good and timestamp1 is good, and they are different, device is healthy */
		if( pdp0_timeStamp0 >= 0 && pdp0_timeStamp1 >= 0 && pdp0_timeStamp0 != pdp0_timeStamp1)
			retval.add("PDP 0");

		for(int i = 0; i < 63; ++i) 
		{
			if( pcm_timeStamp0[i] >= 0 && pcm_timeStamp1[i] >= 0 && pcm_timeStamp0[i] != pcm_timeStamp1[i])
				retval.add("PCM " + i);
		
			if( srx_timeStamp0[i] >= 0 && srx_timeStamp1[i] >= 0 && srx_timeStamp0[i] != srx_timeStamp1[i])
				retval.add("SRX " + i);
		}
		
		return retval;
	}
	
	/**
	 * Check a double value to be within a min/max range.
	 * @param value Value to test.
	 * @param min Lowest valid value.
	 * @param max Maximum valid value.
	 * @return True if value in range, false if not.
	 */
	public static boolean checkRange(double value, double min, double max)
	{
		if (min > max) throw new BoundaryException("min is greater than max");
		
		if (value < min || value > max) return false;
		
		return true;
	}
	
	/**
	 * Check a double value to be within a min/max range. Throws exception with error message if not.
	 * @param value Value to test.
	 * @param min Lowest valid value.
	 * @param max Maximum valid value.
	 * @param errorMessage Error message for exception.
	 * @exception IllegalArgumentException
	 */
	public static void checkRange(double value, double min, double max, String errorMessage)
	{
		if (!checkRange(value, min, max)) throw new IllegalArgumentException(errorMessage);
	}
	
	/**
	 * Check an integer value to be within a min/max range.
	 * @param value Value to test.
	 * @param min Lowest valid value.
	 * @param max Maximum valid value.
	 * @return True if value in range, false if not.
	 */
	public static boolean checkRange(int value, int min, int max)
	{
		if (min > max) throw new BoundaryException("min is greater than max");
		
		if (value < min || value > max) return false;
		
		return true;
	}
	
	/**
	 * Check an integer value to be within a min/max range. Throws exception with error message if not.
	 * @param value Value to test.
	 * @param min Lowest valid value.
	 * @param max Maximum valid value.
	 * @param errorMessage Error message for the exception.
	 * @exception IllegalArgumentException
	 */
	public static void checkRange(int value, int min, int max, String errorMessage)
	{
		if (!checkRange(value, min, max)) throw new IllegalArgumentException(errorMessage);
	}
	
	/**
	 * Check an integer value to be in the range -magnitude to +magnitude.
	 * @param value Value to test.
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @return True if value greater than or equal to -magnitude and less than or equal to + magnitude.
	 */
	public static boolean checkRange(int value, int magnitude)
	{
		return checkRange(value, -magnitude, +magnitude);
	}
	
	/**
	 * Check an integer value to be in the range -magnitude to +magnitude. Throws exception with error message if not.
	 * @param value Value to test.
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @param errorMessage Error message for the exception.
	 * @exception IllegalArgumentException
	 */
	public static void checkRange(int value, int magnitude, String errorMessage)
	{
		if (!checkRange(value,magnitude)) throw new IllegalArgumentException(errorMessage);
	}
	
	/**
	 * Check a double value to be in the range -magnitude to +magnitude.
	 * @param value Value to test.
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @return True if value greater than or equal to -magnitude and less than or equal to + magnitude.
	 */
	public static boolean checkRange(double value, double magnitude)
	{
		return checkRange(value, -magnitude, +magnitude);
	}
	
	/**
	 * Check a double value to be in the range -magnitude to +magnitude. Throws exception with error message if not.
	 * @param value Value to test.
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @param errorMessage Error message for the exception.
	 * @exception IllegalArgumentException
	 */
	public static void checkRange(double value, double magnitude, String errorMessage)
	{
		if (!checkRange(value, magnitude)) throw new IllegalArgumentException(errorMessage);
	}

	/**
	 * Constrain an integer value to be in the range specified by min/max.
	 * @param value Value to test
	 * @param min Smallest valid value.
	 * @param max Largest valid value
	 * @return The value or min/max.
	 */
	public static int clampValue(int value, int min, int max)
	{
		return Math.max(Math.min(value, max), min);
	}
	
	/**
	 * Constrain an integer value to be in the range -magnitude to +magnitude.
	 * @param value Value to test
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @return The value or +- magnitude.
	 */
	public static int clampValue(int value, int magnitude)
	{
		return clampValue(value, -magnitude, magnitude);
	}

	/**
	 * Constrain a double value to be in the range specified by min/max.
	 * @param value Value to test
	 * @param min Smallest valid value.
	 * @param max Largest valid value
	 * @return The value or min/max.
	 */
	public static double clampValue(double value, double min, double max)
	{
		return Math.min(Math.max(value, min), max);
	}
	
	/**
	 * Constrain an double value to be in the range -magnitude to +magnitude.
	 * @param value Value to test
	 * @param magnitude Magnitude or +-range of valid values, always positive.
	 * @return The value or +- magnitude.
	 */
	public static double clampValue(double value, double magnitude)
	{
		return clampValue(value, -magnitude, magnitude);
	}
	
	/**
	 * Round a double to a specified number of decimal places.
	 * @param number Value to round.
	 * @param decimalPlaces Number of decimal places to round to.
	 * @param rounding Selected RoundingMode.
	 * @return Rounded value.
	 */
	public static double round(double number, int decimalPlaces, RoundingMode rounding) 
	{
		BigDecimal bd = new BigDecimal(number);
		bd = bd.setScale(decimalPlaces, rounding);
		return bd.doubleValue();
	}
	
	/**
	 * Return the elapsed time since the last call to this method.
	 * @return Elapsed time in seconds.
	 */
	public static double getElaspedTime()
	{
		double now = Timer.getFPGATimestamp();
		if (timeMarker == 0) timeMarker = now;
		double elapsedTime = now - timeMarker;
		timeMarker = now;
		return elapsedTime;
	}
	
	/**
	 * Return the elapsed time between the specified timestamp and current timestamp.
	 * @param previousTime Previous timestamp.
	 * @return Elapsed time in seconds.
	 */
	public static double getElaspedTime(double previousTime)
	{
		return Timer.getFPGATimestamp() - previousTime;
	}

	/**
	 * Return current FPGA timestamp.
	 * @return The timestamp in seconds.
	 */
	public static double timeStamp()
	{
		return Timer.getFPGATimestamp();
	}
	
	/**
	 * Convert inches to meters.
	 * @param inches Inches value to convert.
	 * @return The distance in meters.
	 */
	public static double inchesToMeters(double inches)
	{
		return inches * .0254;
	}
}
