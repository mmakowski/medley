/*
 * Created on 26-Dec-2004
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;

/**
 * Abstract class representing the panes of views.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2004/12/29 22:53:51 $
 */
public abstract class ViewPane extends Composite {

	/**
	 * Construct view pane.
	 * @param parent the parent control
	 * @param style style flags
	 */
	public ViewPane(Composite parent, int style) {
		super(parent, style);
	}
	
	public abstract void refresh() throws MedleyException;
}
