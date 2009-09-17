/*
 * Created on 23-Jan-2005
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Rating;
import com.mmakowski.medley.resources.Resources;

/**
 * A window allowing to view/edit rating.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/04/17 21:50:07 $
 */
class RatingWindow extends DataWindow {
    // preferences paths
    public static final String PREF_WINDOW_SIZE_X = "/rating_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/rating_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/rating_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/rating_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/rating_window/maximized";

    /** the tag */
	protected Rating rating;
	/** basic info pane */
	protected RatingBasicInfoPane basicInfo;
	
    /**
     * Create new rating window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param ratableType the type of elements the new rating should apply to
     * @return created window
     * @throws MedleyException
     */
    public static RatingWindow createNewRatingWindow(Shell parent, boolean editable, int ratableType) throws MedleyException {
    	Rating r = Rating.create(ratableType);
    	return new RatingWindow(parent, editable, r, true);
    }

    /**
     * Create edit rating window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param ratingId id of rating which should be presented in the window
     * @return created window
     * @throws MedleyException
     */
    public static RatingWindow createEditRatingWindow(Shell parent, boolean editable, int ratingId) throws MedleyException {
    	Rating r = Rating.load(ratingId);
    	return new RatingWindow(parent, editable, r, false);
    }
    
    /**
     * Create rating window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param tag the rating which should be presented in the window
     * @param isNew is this a new rating?
     * @throws MedleyException
     */
    protected RatingWindow(Shell parent, boolean editable, Rating rating, boolean isNew) throws MedleyException {
    	this.rating = rating;
    	initWindow(parent, editable, Resources.getStr(this, isNew ? "titleNew" : "titleEdit"), rating);
    }

    /**
     * Initialize the widgets in the window.
     */
    protected void initWidgets() throws MedleyException {
        // create the basic info pane
        basicInfo = new RatingBasicInfoPane(shell, SWT.NONE, editable, rating);
        FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        basicInfo.setLayoutData(data);
        
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
        

	    basicInfo.setSize(basicInfo.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

	/**
	 * @see com.mmakowski.medley.ui.DataWindow#getTag()
	 */
	protected String getTag() {
		return "rating";
	}

	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		//TODO: dispose widgets
		basicInfo.dispose();
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
