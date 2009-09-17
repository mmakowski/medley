/*
 * Created on 30-May-2005
 */
package com.mmakowski.medley.audrec;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.ExceptionWindow;
import com.mmakowski.medley.ui.Settings;

/**
 * The main window of Audition Recorder.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/08/19 17:54:34 $
 */
public class MainWindow {
    // preferences paths
    public static final String PREF_WINDOW_POSITION_X = "/main_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/main_window/location/y";

    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
    /** logger */
    private static final Logger log = Logger.getLogger(MainWindow.class.getName());
    
    /** the display managing the window */
    private Display display;
    /** an SWT Shell of the main window */
    private Shell shell;
    /** the type of musical item for which audition will be recorded */
    private int itemType;
    
    /**
     * Construct the window.
     * @param display
     * @param itemType the type of musical item
     */
    public MainWindow(Display display, int itemType) throws MedleyException {
        this.display = display;
        this.itemType = itemType;
        shell = new Shell(display, SWT.DIALOG_TRIM);
        shell.setText(Resources.getStr(this, "title"));
        // set window icon
        shell.setImages(new Image[] {new Image(display, "img/icon-audRec-16.gif"),
                                     new Image(display, "img/icon-audRec-32.gif")});
                
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
        initWidgets();   
    }
    
    /**
     * Initalize window's widgets 
     */
    private void initWidgets() throws MedleyException {
        // the form pane
        final AuditionPane audPane = AuditionPane.create(shell, SWT.NONE, itemType);
        FormData data = new FormData();
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        audPane.setLayoutData(data);
        
        // ok button
        Button okBtn = new Button(shell, SWT.PUSH);
        okBtn.setText(Resources.getStr("ok")); 
        okBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
                // create audition
                try {
                    if (audPane.recordAudition()) {
                        // save
                        log.fine("saving file");
                        DataSource.getNotNull().save();
                        // close the window
                        shell.close();
                    }
                } catch (DataSourceException ex) {
                    (new ExceptionWindow(display, ex)).show();
                }
            }
        });
        data = new FormData();
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.right = new FormAttachment(100, -Settings.MARGIN_LEFT - Settings.BUTTON_WIDTH - Settings.ITEM_SPACING_H);
        data.width = Settings.BUTTON_WIDTH;
        data.height = Settings.BUTTON_HEIGHT;
        okBtn.setLayoutData(data);

        // cancel button
        Button cancelBtn = new Button(shell, SWT.PUSH | SWT.CANCEL);
        cancelBtn.setText(Resources.getStr("cancel")); 
        cancelBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
                // just close the window
                shell.close();
            }
        });
        data = new FormData();
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.right = new FormAttachment(100, -Settings.MARGIN_LEFT);
        data.width = Settings.BUTTON_WIDTH;
        data.height = Settings.BUTTON_HEIGHT;
        cancelBtn.setLayoutData(data);
    }

    /**
     * Show the window and return when it's closed.
     */
    public void show() {
        shell.pack();
        shell.setLocation(new Point(prefs.getInt(PREF_WINDOW_POSITION_X, 10), prefs.getInt(PREF_WINDOW_POSITION_Y, 10)));
        // add listeners that save window position and state
        shell.addControlListener(new ControlAdapter() {
            public void controlMoved(ControlEvent e) {
                Point loc = shell.getLocation();
                prefs.putInt(PREF_WINDOW_POSITION_X, loc.x);
                prefs.putInt(PREF_WINDOW_POSITION_Y, loc.y);
            }
        });
        shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
    }
    
}
