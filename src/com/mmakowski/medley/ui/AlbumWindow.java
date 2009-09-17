/*
 * Created on 2003-12-25
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
import com.mmakowski.medley.data.Album;
import com.mmakowski.medley.resources.Resources;

/**
 * The window containing the information on album.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.16 $ $Date: 2005/05/22 00:07:38 $
 */
class AlbumWindow extends DataWindow {
    // preferences paths
    public static final String PREF_WINDOW_SIZE_X = "/album_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/album_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/album_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/album_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/album_window/maximized";

    /** the album this window presents */
    protected Album album;
    
    /**
     * Create new album window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @return created window
     * @throws MedleyException
     */
    public static AlbumWindow createNewAlbumWindow(Shell parent, boolean editable) throws MedleyException {
    	Album a = Album.create();
    	return new AlbumWindow(parent, editable, a, true);
    }

    /**
     * Create edit album window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param albumId id of album which should be presented in the window
     * @return created window
     * @throws MedleyException
     */
    public static AlbumWindow createEditAlbumWindow(Shell parent, boolean editable, int albumId) throws MedleyException {
    	Album a = Album.load(albumId);
    	return new AlbumWindow(parent, editable, a, false);
    }
    
    /**
     * Create album window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param album the album which should be presented in the window
     * @param isNew is this a new album?
     * @throws MedleyException
     */
    protected AlbumWindow(Shell parent, boolean editable, Album album, boolean isNew) throws MedleyException {
    	this.album = album;
    	initWindow(parent, editable, Resources.getStr(this, isNew ? "titleNew" : "titleEdit"), album);
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
        AlbumBasicInfoPane basicInfo = new AlbumBasicInfoPane(folder, SWT.NONE, editable, album);
        item.setControl(basicInfo);
        item.setText(Resources.getStr(this, "basicInfo"));
        item.setToolTipText(Resources.getStr(this, "basicInfoToolTip"));

        // create records tab
        item = new TabItem(folder, SWT.NULL);
        RecordsPane records = new RecordsPane(folder, SWT.NONE, editable, album);
        item.setControl(records);
        item.setText(Resources.getStr(this, "records"));
        item.setToolTipText(Resources.getStr(this, "recordsToolTip"));
        
        // create artists tab
        item = new TabItem(folder, SWT.NULL);
        ArtistsPane artists = new ArtistsPane(folder, SWT.NONE, editable, album);
        item.setControl(artists);
        item.setText(Resources.getStr(this, "artists"));
        item.setToolTipText(Resources.getStr(this, "artistsToolTip"));
   
        // create tags tab
        item = new TabItem(folder, SWT.NULL);
        TagsPane tags = new TagsPane(folder, SWT.NONE, editable, album);
        item.setControl(tags);
        item.setText(Resources.getStr(this, "tags"));
        item.setToolTipText(Resources.getStr(this, "tagsToolTip"));
        
        // create ratings tab
        item = new TabItem(folder, SWT.NULL);
        RatingsPane ratings = new RatingsPane(folder, SWT.NONE, editable, album);
        item.setControl(ratings);
        item.setText(Resources.getStr(this, "ratings"));
        item.setToolTipText(Resources.getStr(this, "ratingsToolTip"));
        
        // create auditions tab
        item = new TabItem(folder, SWT.NULL);
        AuditionsPane auditions = new AuditionsPane(folder, SWT.NONE, editable, album);
        item.setControl(auditions);
        item.setText(Resources.getStr(this, "auditions"));
        item.setToolTipText(Resources.getStr(this, "auditionsToolTip"));
        
        // create images tab
        item = new TabItem(folder, SWT.NULL);
        ImagesPane images = new ImagesPane(folder, SWT.NONE, editable, album);
        item.setControl(images);
        item.setText(Resources.getStr(this, "images"));
        item.setToolTipText(Resources.getStr(this, "imagesToolTip"));
        
        folder.setSize(folder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

	/**
	 * @see com.mmakowski.medley.ui.DataWindow#getTag()
	 */
	protected String getTag() {
		return "album";
	}

	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		//TODO: dispose widgets
		
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
