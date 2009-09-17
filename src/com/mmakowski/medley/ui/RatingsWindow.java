/*
 * Created on 23-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.windows.Window;

/**
 * Window presenting the lists of ratings and rating groups.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/24 16:02:46 $
 */
class RatingsWindow extends Window {
	// TODO: edit ratings/rating groups in a tree view
    // preference paths
    // use DataWindow settings
    public static final String PREF_RESTORE_POSITION = "/data_window/restore_position";
    public static final String PREF_WINDOW_SIZE_X = "/ratings_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/ratings_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/ratings_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/ratings_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/ratings_window/maximized";
    
    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(TagsWindow.class);

	/** type of items for which tags should be displayed */
	protected int ratableType;
	
	/**
	 * Construct tags window.
	 * @param parent
	 */
	public RatingsWindow(Shell parent, int ratableType) throws MedleyException {
		super(parent);
		this.ratableType = ratableType;
        shell = new Shell(parent, SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
        shell.setText(Resources.getStr(this, "title." + ratableType));
        // set window icon
        shell.setImages(new Image[] {new Image(shell.getDisplay(), "img/icon-ratings-16.gif"),
        							 new Image(shell.getDisplay(), "img/icon-ratings-32.gif")});
        // save the model contents when the window is closed
        shell.addShellListener(new ShellAdapter() {
        	public void shellClosed(ShellEvent e) {
        		disposeWidgets();
        	}
        });
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
		initWidgets();
	}

	/**
	 * @see com.mmakowski.swt.events.Window#initWidgets()
	 */
	protected void initWidgets() throws MedleyException {
        // create the tab folder
        TabFolder folder = new TabFolder(shell, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        folder.setLayoutData(data);

        // close button
        Button closeBtn = new Button(shell, SWT.NONE);
	    closeBtn.setText(Resources.getStr("close"));
	    closeBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
        		shell.close();
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.right = new FormAttachment(100, -Settings.MARGIN_LEFT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    closeBtn.setLayoutData(data);
        
        // create ratings tab
        TabItem item = new TabItem(folder, SWT.NULL);
        RatingListPane ratings = new RatingListPane(folder, SWT.NONE, ratableType);
        item.setControl(ratings);
        item.setText(Resources.getStr(this, "ratings"));
        item.setToolTipText(Resources.getStr(this, "ratingsToolTip"));
        
		// create groups tab
        item = new TabItem(folder, SWT.NULL);
        RatingGroupListPane ratingGroups = new RatingGroupListPane(folder, SWT.NONE);
        item.setControl(ratingGroups);
        item.setText(Resources.getStr(this, "ratingGroups"));
        item.setToolTipText(Resources.getStr(this, "ratingGroupsToolTip"));

	}

    /**
     * Display tags window.
     * @see com.mmakowski.swt.windows.Window#show()
     */
    public void show() {
        if (prefs.getBoolean(PREF_RESTORE_POSITION, true)) {
            shell.setSize(new Point(prefs.getInt(PREF_WINDOW_SIZE_X, 400), prefs.getInt(PREF_WINDOW_SIZE_Y, 400)));
            shell.setLocation(new Point(prefs.getInt(PREF_WINDOW_POSITION_X, 10), prefs.getInt(PREF_WINDOW_POSITION_Y, 10)));
            shell.setMaximized(prefs.getBoolean(PREF_WINDOW_MAXIMIZED, false));
        } else {
            shell.setSize(400, 400);
        }
        // add listeners that save window position and state
        shell.addControlListener(new ControlAdapter() {
            public void controlMoved(ControlEvent e) {
                if (shell.getMaximized()) {
                    prefs.putBoolean(PREF_WINDOW_MAXIMIZED, true);
                } else {
                    prefs.putBoolean(PREF_WINDOW_MAXIMIZED, false);
                    Point loc = shell.getLocation();
                    prefs.putInt(PREF_WINDOW_POSITION_X, loc.x);
                    prefs.putInt(PREF_WINDOW_POSITION_Y, loc.y);
                }
            }
            public void controlResized(ControlEvent e) {
                if (shell.getMaximized()) {
                    prefs.putBoolean(PREF_WINDOW_MAXIMIZED, true);
                } else {
                    prefs.putBoolean(PREF_WINDOW_MAXIMIZED, false);
                    Point size = shell.getSize();
                    prefs.putInt(PREF_WINDOW_SIZE_X, size.x);
                    prefs.putInt(PREF_WINDOW_SIZE_Y, size.y);
                }
            }
        });
        super.show();
    }
    
	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		// TODO: dispose widgets
	}


}
