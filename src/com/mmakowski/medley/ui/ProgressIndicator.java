/*
 * Created on 22-Jan-2005
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

/**
 * A pane presenting task progress.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/22 19:13:41 $
 */
public class ProgressIndicator extends Composite {

	protected static final int PROGRESS_BAR_WIDTH = 200;
	
	/** the progress bar */
	protected ProgressBar bar;
	/** the label */
	protected Label label;
	
	/**
	 * @param parent parent control
	 * @param style style flags
	 */
	public ProgressIndicator(Composite parent, int style) {
		super(parent, style);
        FormLayout layout = new FormLayout();
        setLayout(layout);
		initWidgets();
	}
	
	/**
	 * Initialise pane's widgets.
	 */
	protected void initWidgets() {
        label = new Label(this, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, -PROGRESS_BAR_WIDTH);
        label.setLayoutData(data);
        
        bar = new ProgressBar(this, SWT.HORIZONTAL | SWT.SMOOTH);
        data = new FormData();
        data.top = new FormAttachment(50, -Settings.PROGRESS_BAR_HEIGHT / 2);
        data.height = Settings.PROGRESS_BAR_HEIGHT;
        data.left = new FormAttachment(100, -PROGRESS_BAR_WIDTH);
        data.right = new FormAttachment(100, 0);
        bar.setLayoutData(data);
	}

	/**
	 * Set the label text
	 * @param txt text to display
	 */
	public void setStatus(String txt) {
		label.setText(txt);
	}
	
	/**
	 * Set progress range
	 * @param min min value
	 * @param max max value
	 */
	public void setProgressRange(int min, int max) {
		bar.setMinimum(min);
		bar.setMaximum(max);
	}
	
	/**
	 * Set progress value
	 * @param val the value
	 */
	public void setProgressValue(int val) {
		bar.setSelection(val);
	}
}
