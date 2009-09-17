/*
 * Created on 26-Dec-2004
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;

import com.mmakowski.medley.MedleyException;

/**
 * The main view of the application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/01/16 13:05:10 $
 */
public class HomeView extends View {

	/**
	 * @param mainWindow the main window
	 */
	public HomeView(MainWindow mainWindow) {
		super(mainWindow);
	}

	/**
	 * @see com.mmakowski.medley.ui.View#createPane(org.eclipse.swt.widgets.Composite)
	 */
	public ViewPane createPane(Composite parent) throws MedleyException {
		pane = new HomePane(parent, SWT.NONE);
		return pane;
	}

	/**
	 * @see com.mmakowski.medley.ui.View#getTag()
	 */
	protected String getTag() {
		return "home";
	}

	/**
	 * @see com.mmakowski.medley.ui.View#addMenuItems(org.eclipse.swt.widgets.Menu)
	 */
	public void addMenuItems(Menu menu) throws MedleyException {
		// no view-specific items
		
	}

}
