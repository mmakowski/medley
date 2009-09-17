/*
 * Created on 2004-08-06
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Artist;
import com.mmakowski.medley.resources.Resources;

/**
 * The window containing information about an artist.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.10 $  $Date: 2005/04/17 21:50:07 $
 */
class ArtistWindow extends DataWindow {
    // preferences paths
    public static final String PREF_WINDOW_SIZE_X = "/artist_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/artist_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/artist_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/artist_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/artist_window/maximized";

    /** the artist this window presents */
    protected Artist artist;
    
    public ArtistWindow(Shell parent, boolean editable, int artistId) throws MedleyException {
    	this.artist = Artist.load(artistId);
    	initWindow(parent, editable, Resources.getStr(this, "titleEdit"), artist);
    }

    public ArtistWindow(Shell parent, boolean editable, String artistName) throws MedleyException {
    	this.artist = Artist.load(artistName);
    	initWindow(parent, editable, Resources.getStr(this, "titleEdit"), artist);
    }

    /**
     * Construct the window.
     * @param display
     */
    public ArtistWindow(Shell parent, boolean editable) throws MedleyException {
    	this.artist = Artist.create();
    	initWindow(parent, editable, Resources.getStr(this, "titleNew"), artist);
    }
    
    /**
     * Initialize the widgets in the window.
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
	    
        // create basic info tab
        TabItem item = new TabItem(folder, SWT.NULL);
        ArtistBasicInfoPane basicInfo = new ArtistBasicInfoPane(folder, SWT.NONE, editable, artist);
        item.setControl(basicInfo);
        item.setText(Resources.getStr(this, "basicInfo"));
        item.setToolTipText(Resources.getStr(this, "basicInfoToolTip"));

        // create tags tab
        item = new TabItem(folder, SWT.NULL);
        TagsPane tags = new TagsPane(folder, SWT.NONE, editable, artist);
        item.setControl(tags);
        item.setText(Resources.getStr(this, "tags"));
        item.setToolTipText(Resources.getStr(this, "tagsToolTip"));
        
        // create ratings tab
        item = new TabItem(folder, SWT.NULL);
        RatingsPane ratings = new RatingsPane(folder, SWT.NONE, editable, artist);
        item.setControl(ratings);
        item.setText(Resources.getStr(this, "ratings"));
        item.setToolTipText(Resources.getStr(this, "ratingsToolTip"));

        // create images tab
        item = new TabItem(folder, SWT.NULL);
        ImagesPane images = new ImagesPane(folder, SWT.NONE, editable, artist);
        item.setControl(images);
        item.setText(Resources.getStr(this, "images"));
        item.setToolTipText(Resources.getStr(this, "imagesToolTip"));
        
        folder.setSize(folder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

	/**
	 * @see com.mmakowski.medley.ui.DataWindow#getTag()
	 */
	protected String getTag() {
		return "artist";
	}

	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		// TODO: dispose widgets
		
	}    

    /**
     * @return default height for this window
     */
    protected int getDefaultHeight() {
        return 230;
    }
    
    /**
     * @see com.mmakowski.medley.ui.DataWindow#getPREF_WINDOW_SIZE_X()
     */
    protected String getPREF_WINDOW_SIZE_X() {
        return PREF_WINDOW_SIZE_X;
    }

    /**
     * @see com.mmakowski.medley.ui.DataWindow#getPREF_WINDOW_SIZE_Y()
     */
    protected String getPREF_WINDOW_SIZE_Y() {
        return PREF_WINDOW_SIZE_Y;
    }

    /**
     * @see com.mmakowski.medley.ui.DataWindow#getPREF_WINDOW_POSITION_X()
     */
    protected String getPREF_WINDOW_POSITION_X() {
        return PREF_WINDOW_POSITION_X;
    }

    /**
     * @see com.mmakowski.medley.ui.DataWindow#getPREF_WINDOW_POSITION_Y()
     */
    protected String getPREF_WINDOW_POSITION_Y() {
        return PREF_WINDOW_POSITION_Y;
    }

    /**
     * @see com.mmakowski.medley.ui.DataWindow#getPREF_WINDOW_MAXIMIZED()
     */
    protected String getPREF_WINDOW_MAXIMIZED() {
        return PREF_WINDOW_MAXIMIZED;
    }    

}
