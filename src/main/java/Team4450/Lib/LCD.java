
package Team4450.Lib;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.util.logging.Level;

/**
 * Wrapper class for Driver Station LCD panel. Will format and send data
 * to the dashboard simulated lines 1-10. Can send lines on demand or
 * store them in memory to be sent later.
 */

public class LCD
{
	private LCD				lcd;
	private static String	lcdLines[] = new String[11];

	/**
	 * Get a reference to the global instance of LCD class.
	 * @return Reference to global LCD class instance.
	 */
	public LCD getInstance()
	{
	  	if (lcd == null) lcd = new LCD();
	    
	    return lcd;
	}

	private LCD()
	{
		Util.consoleLog();
		
		clearAll();
	}

	/**
	 * Release any resources held by LCD class.
	 */
	public void dispose()
	{
		Util.consoleLog();
	}

	@Deprecated
	/**
	 * Log message to the console as well as our log file.
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */
	public static void consoleLog(String message, Object... parms)
	{
		// logs to the console as well as our log file on RR disk.
		Util.logger.log(Level.INFO, String.format("robot: %s", String.format(message, parms)));
	}

	@Deprecated
	/**
	 * Log message to the console as well as our log file.
	 * @param message Message to display.
	 */
	public static  void consoleLogNoFormat(String message)
	{
		Util.logger.log(Level.INFO, String.format("robot: %s", message));
	}

	/**
	 * Print data to LCD line starting at column (no clear of line). Data
	 * is sent immediately to dashboard.
	 * @param line LCD line to print on (1-based).
	 * @param column Column in which to start printing (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void print(int line, int column, String message, Object... parms)
	{
		String	lcdLine = "";
		
		if (column < 1) column = 1;
		
		column--;	// in here, the column is zero based.
		
		switch (line)
		{
			case 1:
                lcdLine = "LCD_Line_1";
				break;

			case 2:
				lcdLine = "LCD_Line_2";
				break;

			case 3:
				lcdLine = "LCD_Line_3";
				break;

			case 4:
				lcdLine = "LCD_Line_4";
				break;

			case 5:
				lcdLine = "LCD_Line_5";
				break;

			case 6:
				lcdLine = "LCD_Line_6";
				break;

			case 7:
				lcdLine = "LCD_Line_7";
				break;

			case 8:
				lcdLine = "LCD_Line_8";
				break;

			case 9:
				lcdLine = "LCD_Line_9";
				break;

			case 10:
				lcdLine = "LCD_Line_10";
				break;
		}

		StringBuffer oldMessage = new StringBuffer(SmartDashboard.getString(lcdLine,""));
		String newMessage = String.format(message, parms);
		oldMessage.replace(column, newMessage.length() + column, newMessage);
		lcdLines[line] = oldMessage.toString();
		
		SmartDashboard.putString(lcdLine, lcdLines[line]);
	}

	/**
	 * Print data to LCD line (line cleared first). Data is sent to dashboard
	 * immediately.
	 * @param line LCD line to print on (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void printLine(int line, String message, Object... parms)
	{
		clearLine(line);

		print(line, 1, message, parms);
	}

	/**
	 * Clear LCD line. Line is cleared on dashboard immediately.
	 * @param line Line to clear (1-based).
	 */
	
	public static void clearLine(int line)
	{
		// The strange looking format specifier below repeats the "" 100 times.
		String blankLine = String.format("%1$-100s", "");	//"                                          ";

		print(line, 1, blankLine);
	} 

	/**
	 * Clear all LCD lines. Lines are cleared on dashboard immediately.
	 */
	
	public static void clearAll()
	{
		String blankLine = String.format("%1$-100s", "");	//"                                          ";
		
		for (int i = 1; i < 11; i++) {print(i, 1, blankLine);}
	} 

	/**
	 * Print data to LCD line starting at column (no clear of line). Data
	 * is stored in memory to be sent later by sendLine() or sendAll().
	 * @param line LCD line to print on (1-based).
	 * @param column Column in which to start printing (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void set(int line, int column, String message, Object... parms)
	{
		if (column < 1) column = 1;
		
		column--;	// in here, the column is zero based.

		StringBuffer oldMessage = new StringBuffer(lcdLines[line]);
		String newMessage = String.format(message, parms);
		oldMessage.replace(column, newMessage.length() + column, newMessage);
		lcdLines[line] = oldMessage.toString();
	}

	/**
	 * Print data to LCD line (line cleared first). Data is stored to be sent
	 * later by sendLine() or sendAll().
	 * @param line LCD line to print on (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void setLine(int line, String message, Object... parms)
	{
		clearLine(line);

		set(line, 1, message, parms);
	}

	/**
	 * Clear LCD line. Line is cleared in memory to be sent later by sendLine() or sendAll().
	 * @param line Line to clear (1-based).
	 */
	
	public static void setClearLine(int line)
	{
		String blankLine = String.format("%1$-100s", "");	//"                                          ";

		set(line, 1, blankLine);
	} 

	/**
	 * Clear all LCD lines. Lines are cleared in memory to be sent later.
	 */
	
	public static void setClearAll()
	{
		String blankLine = String.format("%1$-100s", "");	//"                                          ";
		
		for (int i = 1; i < 11; i++) {set(i, 1, blankLine);}
	} 

	/**
	 * Append data to LCD line stored in memory starting at column (no clear of line). Data
	 * is stored to be sent later by sendLine() or sendAll().
	 * @param line LCD line to print on (1-based).
	 * @param message Message to display with optional formatting parameters.
	 * @param parms optional objects matching formatting parameters.
	 */

	public static void append(int line, String message, Object... parms)
	{
		if (lcdLines[line] == String.format("%1$-100s", ""))
		{
			lcdLines[line] = String.format(message, parms);
		}
		else
		{
			lcdLines[line] += String.format(message, parms);
		}
	}
	
	/**
	 * Send an LCD line from memory to the dashboard.
	 * @param line Line number (1-10) to send.
	 */
	public static void sendLine(int line)
	{
		String	lcdLine = "";
		
		switch (line)
		{
			case 1:
                lcdLine = "LCD_Line_1";
				break;

			case 2:
				lcdLine = "LCD_Line_2";
				break;

			case 3:
				lcdLine = "LCD_Line_3";
				break;

			case 4:
				lcdLine = "LCD_Line_4";
				break;

			case 5:
				lcdLine = "LCD_Line_5";
				break;

			case 6:
				lcdLine = "LCD_Line_6";
				break;

			case 7:
				lcdLine = "LCD_Line_7";
				break;

			case 8:
				lcdLine = "LCD_Line_8";
				break;

			case 9:
				lcdLine = "LCD_Line_9";
				break;

			case 10:
				lcdLine = "LCD_Line_10";
				break;
		}

		SmartDashboard.putString(lcdLine, lcdLines[line]);
	}
	
	/**
	 * Send all LCD lines stored in memory to the dashboard.
	 */
	public static void sendAll()
	{
		for (int i = 1; i < 11; i++) {sendLine(i);}
	}
}
