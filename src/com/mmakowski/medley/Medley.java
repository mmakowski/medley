/*
 * Created on 2003-12-25
 */
package com.mmakowski.medley;

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.resources.Errors;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.MainWindow;

/**
 * The main class of the Medley application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.15 $ $Date: 2005/08/19 17:53:23 $ 
 */
public class Medley {

	public static final String VERSION = "0.2.1";
	public static final String COPYRIGHT = "(c) 2004-2005 Maciek Makowski";
	
	public static final String PATH_LOG = "log";
	public static final String PATH_USER_SETTINGS = "userSettings";
	
	private static final String SETTINGS_PATH_WIN = "/Application Data/Maciek Makowski/Medley";
	private static final String SETTINGS_PATH_UNIX = "/.medley";
	
	/** App's runtime properties */
	private static Properties dirs;
	/** logger */
    private static final Logger log = Logger.getLogger(Medley.class.getName());
	
    // intialisation parameters
    private String fileName = "";
    
    /**
     * Display the main application window.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Medley m = new Medley();
        m.run(args);
    }

    /**
     * The main method of the applications
     * @param args command line arguments
     */
    private void run(String[] args) {
        Display display = new Display();
        try {
            // initialise properties
            parseCmdLineArgs(args);
            initProperties();
            initLogging();
            // open the log file
            log.info("Medley version " + VERSION + " starting");
            
            MainWindow mainWin = new MainWindow(display, fileName);
            // The MainWindow.show() call will exit when the window is closed.
            // We terminate the app then.
            mainWin.show();
            DataSource.deactivate();
            log.info("Medley exiting");
        } catch (Exception ex) {
            (new com.mmakowski.medley.ui.ExceptionWindow(display, ex)).show();
        }
        display.dispose();
    }
    
    /**
     * @param args array of arguments passed to Medley
     */
    private void parseCmdLineArgs(String[] args) {
        // file name option
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option openFileOption = parser.addStringOption('o', "open-file");
        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException ex) {
            log.log(Level.SEVERE, "incorrect command line option", ex);
            printUsage();
            System.exit(2);
        }
        fileName = (String) parser.getOptionValue(openFileOption, null);
    }

    /**
     * Output command line parameters description.
     *
     */
    private void printUsage() {
        try {
            System.err.println(Resources.getStr(this, "usage"));
        } catch (ResourceException ex) {
            log.log(Level.WARNING, "resource error", ex);
        }
    }
    
    /**
     * Initialise logging.
     * @throws MedleyException
     */
    private void initLogging() throws MedleyException {
    	// No need to do anything -- settings are read from properties file
    }
    
    /**
     * Initialise application properties.
     * @throws MedleyException
     */
    private static void initProperties() throws MedleyException {
    	dirs = new Properties();
    	dirs.put(PATH_USER_SETTINGS, getUserSettingsPath());
    	dirs.put(PATH_LOG, getUserSettingsPath() + "/log");
    	// create the directory structure
    	createUserDirs();
    }
    
    /**
     * @param key
     * @return path for given key
     */
    private static String getPath(String key) {
    	return dirs.getProperty(key);
    }
    
    /**
     * @return the path where user-specific settings should be stored
     */
    private static String getUserSettingsPath() {
    	String path = System.getProperty("user.home");
    	Object o = System.getProperties();
    	// for MS Windows systems the directory is under Application Data
    	if (System.getProperty("file.separator").equals("\\")) {
    		path += SETTINGS_PATH_WIN;
    	} else {
    		path += SETTINGS_PATH_UNIX;
    	}
    	return path;
    }
    
    /**
     * Create directories for user-specific settings (if they don't
     * already exist)
     */
    private static void createUserDirs() throws MedleyException {
    	for (Enumeration e = dirs.keys(); e.hasMoreElements();) {
    		createPath(new File(dirs.getProperty((String) e.nextElement())));
    	}
    }
    
    /**
     * Create given path if it doesn't exist, creating all the parent
     * directories if needed.
     * @param path path to the directory to be created
     * @throws MedleyException
     */
    private static void createPath(File path) throws MedleyException {
		try {
			if (!path.exists()) {
				path.mkdirs();
			}
		} catch (Exception ex) {
			throw new MedleyException(Errors.CANT_CREATE_DIR, new Object[] {path}, ex);
		}
    }
}
