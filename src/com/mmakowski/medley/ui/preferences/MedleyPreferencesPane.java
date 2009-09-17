/*
 * Created on 19-Feb-2005
 */
package com.mmakowski.medley.ui.preferences;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.ExceptionWindow;
import com.mmakowski.swt.widgets.PreferencesPane;

/**
 * Abstract preferences pane for Medley.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/02/20 00:16:35 $
 */
public abstract class MedleyPreferencesPane extends PreferencesPane {

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public MedleyPreferencesPane(Composite parent, int style) throws Exception {
		super(parent, style);
	}

	/**
	 * @see com.mmakowski.swt.widgets.PreferencesPane#getTitle()
	 */
	public String getTitle() {
    	try {
    		return Resources.getStr(this, "title");
    	} catch (ResourceException ex) {
        	(new ExceptionWindow(Display.getCurrent(), ex)).show();
        	return "";
    	}
	}

	/**
	 * @see com.mmakowski.swt.widgets.PreferencesPane#getTreePath()
	 */
	public String getTreePath() {
    	try {
    		return Resources.getStr(this, "path");
    	} catch (ResourceException ex) {
        	(new ExceptionWindow(Display.getCurrent(), ex)).show();
        	return "";
    	}
	}

}
