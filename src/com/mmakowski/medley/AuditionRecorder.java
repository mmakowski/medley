/*
 * Created on 30-May-2005
 */
package com.mmakowski.medley;

import jargs.gnu.CmdLineParser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;

import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.FileDataSource;
import com.mmakowski.medley.data.MusicalItem;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.audrec.MainWindow;


/**
 * An application that allows to record audition of album/record/track. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/08/19 17:54:01 $
 */
public class AuditionRecorder {

    /** logger */
    private final Logger log = Logger.getLogger(AuditionRecorder.class.getName());

    private String fileName;
    private int itemType;
    
    /**
     * Start the application.
     * @param args
     */
    public static void main(String[] args) {
        AuditionRecorder audRec = new AuditionRecorder();
        audRec.run(args);
    }
    
    /**
     * The main function of the application.
     */
    private void run(String[] args) {
        // parse command line arguments
        parseCmdLineArgs(args);
        // open data file
        openDataFile();
        // open window
        openWindow();
        // close data file
        closeDataFile();
    }
    
    /**
     * @param args array of arguments passed to Medley
     */
    private void parseCmdLineArgs(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        // file name option
        CmdLineParser.Option openFileOption = parser.addStringOption('o', "open-file");
        // item type option
        CmdLineParser.Option itemTypeOption = parser.addStringOption('i', "item-type");
        try {
            parser.parse(args);
        } catch (CmdLineParser.OptionException ex) {
            log.log(Level.SEVERE, "incorrect command line option", ex);
            cmdLineError();
        }
        // read options
        fileName = (String) parser.getOptionValue(openFileOption);
        if (fileName == null) {
            log.severe("file path not supplied");
            cmdLineError();
        }
        String strItemType = (String) parser.getOptionValue(itemTypeOption);
        if (strItemType == null) {
            log.severe("item type not supplied");
            cmdLineError();
        } else {
            try {
                itemType = MusicalItem.stringToItemType(strItemType);
            } catch (DataSourceException ex) {
                log.log(Level.SEVERE, "error while parsing item type", ex);
                cmdLineError();
            }
            if (itemType != MusicalItem.ALBUM &&
                itemType != MusicalItem.RECORD &&
                itemType != MusicalItem.TRACK) {
                log.severe("incorrect item type");
                cmdLineError();
            }
        }
    }
    
    /**
     * Print usage message.
     */
    private void printUsage() {
        try {
            System.err.println(Resources.getStr(this, "usage"));
        } catch (ResourceException ex) {
            log.log(Level.WARNING, "resource error", ex);
        }
    }
    
    /**
     * Report an error while parsing command line and exit.
     */
    private void cmdLineError() {
        printUsage();
        System.exit(2);
    }

    /**
     * Open data file.
     */
    private void openDataFile() {
        DataSource ds = null;
        try {
            FileDataSource.create(fileName);
            ds = DataSource.getNotNull();
        } catch (DataSourceException ex) {
            log.log(Level.SEVERE, "error while opening file: " + fileName);
            System.exit(3);
        }
        if (!ds.isFormatUpToDate()) {
            log.info("the format is not up-to-date");
            try {
                System.out.println(Resources.formatStr(this, "updateFormat", new Object[] {fileName}));
            } catch (ResourceException ex) {
                log.log(Level.WARNING, "resource error", ex);
            }
            closeDataFile();
            System.exit(0);
        }
    }

    /**
     * Close the data file. 
     */
    private void closeDataFile() {
        try {
            DataSource.deactivate();
        } catch (DataSourceException ex) {
            log.log(Level.SEVERE, "error while closing file: " + fileName);
            System.exit(4);
        }
    }

    /**
     * Show dialog window. 
     */
    private void openWindow() {
        Display display = new Display();
        try {
            MainWindow mainWin = new MainWindow(display, itemType);
            // The MainWindow.show() call will exit when the window is closed.
            // We terminate the app then.
            mainWin.show();
        } catch (Exception ex) {
            (new com.mmakowski.medley.ui.ExceptionWindow(display, ex)).show();
        }
        display.dispose();
    }
    
}
