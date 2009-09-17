/*
 * Created on 2004-08-06
 */
package com.mmakowski.medley.ui;

import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.DataObject;
import com.mmakowski.medley.resources.Errors;
import com.mmakowski.swt.windows.Window;

/**
 * An abstract window that presents data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.9 $  $Date: 2005/04/17 21:50:07 $
 */
public abstract class DataWindow extends Window {
    // preference paths
    public static final String PREF_RESTORE_POSITION = "/data_window/restore_position";
	
    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(DataWindow.class);

    /** is the data editable? */
    protected boolean editable;
    /** the object that is displayed in the window */
    protected DataObject model;
	
    /**
     * Initialise the window contents.
     * @param parent the parent window
     * @param editable should the controls be editable
     * @param title the title of the window
     * @throws MedleyException
     */
    protected void initWindow(Shell parent, boolean editable, String title, DataObject mdl) 
    		throws MedleyException {
        this.parent = parent;
        this.editable = editable;
        this.model = mdl;
        shell = new Shell(parent, SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
        shell.setText(title);
        // set window icon
        shell.setImages(new Image[] {new Image(shell.getDisplay(), "img/icon-" + getTag() + "-16.gif"),
        							 new Image(shell.getDisplay(), "img/icon-" + getTag() + "-32.gif")});
        // save the model contents when the window is closed
        shell.addShellListener(new ShellAdapter() {
        	public void shellClosed(ShellEvent e) {
        		disposeWidgets();
        		if (model != null) {
        			Cursor c = null;
        			try {
        				// model is being disposed (and thus saved)
            			c = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
            			shell.setCursor(c);
        				model.dispose();
	        	    	c.dispose();
	        	    	c = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);
	        	    	shell.setCursor(c);
	            	} catch (MedleyException ex) {
	        	    	c.dispose();
	        	    	c = new Cursor(Display.getCurrent(), SWT.CURSOR_ARROW);
	        	    	shell.setCursor(c);
	                	(new ExceptionWindow(shell.getDisplay(), ex)).show();
	            	}
        		}
        	}
        });
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
        try {
        	initWidgets();
        } catch (MedleyException ex) {
        	throw ex;
        } catch (Exception ex) {
        	throw new UIException(Errors.ERROR_INITIALIZING_WIDGETS, ex);
        }
    }

    /**
     * Display Album window.
     * @see com.mmakowski.swt.windows.Window#show()
     */
    public void show() {
        if (prefs.getBoolean(PREF_RESTORE_POSITION, true)) {
            shell.setSize(new Point(prefs.getInt(getPREF_WINDOW_SIZE_X(), getDefaultWidth()), prefs.getInt(getPREF_WINDOW_SIZE_Y(), getDefaultHeight())));
            shell.setLocation(new Point(prefs.getInt(getPREF_WINDOW_POSITION_X(), 10), prefs.getInt(getPREF_WINDOW_POSITION_Y(), 10)));
            shell.setMaximized(prefs.getBoolean(getPREF_WINDOW_MAXIMIZED(), false));
        } else {
            shell.setSize(getDefaultWidth(), getDefaultHeight());
        }
        // add listeners that save window position and state
        shell.addControlListener(new ControlAdapter() {
            public void controlMoved(ControlEvent e) {
                if (shell.getMaximized()) {
                    prefs.putBoolean(getPREF_WINDOW_MAXIMIZED(), true);
                } else {
                    prefs.putBoolean(getPREF_WINDOW_MAXIMIZED(), false);
                    Point loc = shell.getLocation();
                    prefs.putInt(getPREF_WINDOW_POSITION_X(), loc.x);
                    prefs.putInt(getPREF_WINDOW_POSITION_Y(), loc.y);
                }
            }
            public void controlResized(ControlEvent e) {
                if (shell.getMaximized()) {
                    prefs.putBoolean(getPREF_WINDOW_MAXIMIZED(), true);
                } else {
                    prefs.putBoolean(getPREF_WINDOW_MAXIMIZED(), false);
                    Point size = shell.getSize();
                    prefs.putInt(getPREF_WINDOW_SIZE_X(), size.x);
                    prefs.putInt(getPREF_WINDOW_SIZE_Y(), size.y);
                }
            }
        });
        super.show();
    }
    
    /**
     * @return default height for this window
     */
    protected int getDefaultHeight() {
        return 400;
    }

    /**
     * @return default width for this window
     */
    protected int getDefaultWidth() {
        return 400;
    }

    protected abstract String getPREF_WINDOW_SIZE_X();
    protected abstract String getPREF_WINDOW_SIZE_Y();
    protected abstract String getPREF_WINDOW_POSITION_X();
    protected abstract String getPREF_WINDOW_POSITION_Y();
    protected abstract String getPREF_WINDOW_MAXIMIZED();

    /**
     * @return tag string for this window
     */
    protected abstract String getTag();
}
