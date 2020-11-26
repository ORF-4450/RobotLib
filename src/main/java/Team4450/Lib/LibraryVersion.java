/**
 * RobotLib Version Number. Update for each release. Also update overview.html for
 * the JavaDoc as well as the readme.md and gradle.properties files.
 */

package Team4450.Lib;

/**
 * Provides static access to current version of the library.
 */

public class LibraryVersion
{
	/**
	 * Returns current version of RobotLib.
	 */
	
	/*
	 * When you start a new version of the library, you can change the version here
	 * so that it is reflected by the robot programs that use the library. After a
	 * compile, you go the robot program project's external references and do a
	 * refresh to pull in the changes. You can push to github along the way to save
	 * the work. Do not update any of the other locations where the version number
	 * appears.
	 * 
	 * Note: The gradle process stores the compiled library in the local gradle
	 * cache. The Robot program projects on this PC use a RobotLib.json file that
	 * specifies local as the desired version number so the library artifacts are
	 * pulled from the local cache. However, a side effect of this scheme is that
	 * since the version, "local", never changes, you have to refresh the programs
	 * external references to have the updated local version pulled into the project.
	 * Robot projects on other PCs use a RobotLib.json with an actual version number
	 * to pull that version from the online artifact library Jitpack.io.
	 * 
	 * When you are ready to release, update the readme.md, src/main/resources/overview.html 
	 * and gradle.properties files to the new version number. Do a final compile. Next, edit 
	 * build.gradle file to switch version = "local" to version = LibraryVersion. Then push to 
	 * github. After push switch the build.gradle version = back to "local". Then on the github 
	 * repository you generate a new release for the new version. This will trigger a Travis 
	 * compile on github which will generate the release artifacts and store the release artifacts 
	 * on Jitpack.io. 
	 * 
	 * Robot program projects on other computers can then download or update RobotLib.json with. 
	 * the new version number. On the next compile gradle will then pull down the new library 
	 * release from Jitpack.
	 * 
	 * Note: The Javadoc output is not pushed to github. Javadoc will be generated as an artifact 
	 * of the Travis compile and will be available in the release for consumption by others.
	 */
	
	public static final String version = "3.6.1 (11.26.2020)";	

	// Private constructor means this class can't be instantiated.
	private LibraryVersion()
	{			
		
	}
}