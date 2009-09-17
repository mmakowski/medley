/*
 * Created on 07-Jan-2005
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
import com.mmakowski.medley.data.Tag;
import com.mmakowski.medley.resources.Resources;

/**
 * A window allowing to view/edit tag.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/04/17 21:50:07 $
 */
class TagWindow extends DataWindow {
    // preferences paths
    public static final String PREF_WINDOW_SIZE_X = "/tag_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/tag_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/tag_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/tag_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/tag_window/maximized";

    /** the tag */
	protected Tag tag;
	/** basic info tab */
	protected TabItem basicInfoTab;
	/** values tab */
	protected TabItem valuesTab;
	/** the tab folder */
	protected TabFolder folder;
	
    /**
     * Create new tag window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param taggableType the type of elements the new tag should apply to
     * @return created window
     * @throws MedleyException
     */
    public static TagWindow createNewTagWindow(Shell parent, boolean editable, int taggableType) throws MedleyException {
    	Tag t = Tag.create(taggableType);
    	return new TagWindow(parent, editable, t, true);
    }

    /**
     * Create edit tag window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param tagId id of tag which should be presented in the window
     * @return created window
     * @throws MedleyException
     */
    public static TagWindow createEditTagWindow(Shell parent, boolean editable, int tagId) throws MedleyException {
    	Tag t = Tag.load(tagId);
    	return new TagWindow(parent, editable, t, false);
    }
    
    /**
     * Create tag window
     * @param parent parent window
     * @param editable should user be allowed to edit contents?
     * @param tag the tag which should be presented in the window
     * @param isNew is this a new album?
     * @throws MedleyException
     */
    protected TagWindow(Shell parent, boolean editable, Tag tag, boolean isNew) throws MedleyException {
    	this.tag = tag;
    	initWindow(parent, editable, Resources.getStr(this, isNew ? "titleNew" : "titleEdit"), tag);
    }

    /**
     * Initialize the widgets in the window.
     */
    protected void initWidgets() throws MedleyException {
        // create the tab folder
        folder = new TabFolder(shell, SWT.NONE);
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
        basicInfoTab = new TabItem(folder, SWT.NULL);
        TagBasicInfoPane basicInfo = new TagBasicInfoPane(folder, SWT.NONE, this, editable, tag);
        basicInfoTab.setControl(basicInfo);
        basicInfoTab.setText(Resources.getStr(this, "basicInfo"));
        basicInfoTab.setToolTipText(Resources.getStr(this, "basicInfoToolTip"));
        
        // create values tab
        valuesTab = new TabItem(folder, SWT.NULL);
        TagValuesPane values = new TagValuesPane(folder, SWT.NONE, this, editable, tag);
        valuesTab.setControl(values);
        valuesTab.setText(Resources.getStr(this, "values"));
        valuesTab.setToolTipText(Resources.getStr(this, "valuesToolTip"));

	    folder.setSize(folder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

	/**
	 * @see com.mmakowski.medley.ui.DataWindow#getTag()
	 */
	protected String getTag() {
		return "tag";
	}

	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		//TODO: dispose widgets
		basicInfoTab.dispose();
		valuesTab.dispose();
		folder.dispose();
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
