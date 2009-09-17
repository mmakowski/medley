/*
 * Created on 26-Dec-2004
 */
package com.mmakowski.medley.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Errors;

/**
 * The pane presenting Medley home screen.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2004/12/29 22:51:54 $
 */
public class HomePane extends ViewPane {

	/** the path to the template file for home page */
	protected static String TEMPLATE_FILE = "tmpl/home.html";
	
	/** the browser control that displays page */
	protected Browser browser;
	
	/**
	 * Construct Home pane
	 * @param parent parent control
	 * @param style style flags
	 */
	public HomePane(Composite parent, int style) throws MedleyException {
		super(parent, style);
		setLayout(new FillLayout());
		initWidgets();
	}

	/**
	 * Create pane's widgets.
	 *
	 */
	protected void initWidgets() throws MedleyException {
		// create browser
		browser = new Browser(this, SWT.NONE);
		// set browser content
		browser.setText(getPageHtml());
	}
	
	/**
	 * @see com.mmakowski.medley.ui.ViewPane#refresh()
	 */
	public void refresh() throws MedleyException {
		// regenerate home page
		browser.setText(getPageHtml());
	}

	/**
	 * @return HTML to be displayed by the browser 
	 * @throws MedleyException
	 */
	protected String getPageHtml() throws MedleyException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(TEMPLATE_FILE));
		} catch (FileNotFoundException ex) {
			throw new MedleyException(Errors.FILE_NOT_FOUND, new Object[] {TEMPLATE_FILE}, ex);
		}
		
		// read in the contents of template file
		String line;
		StringBuffer template = new StringBuffer();
		
		try {
			while ((line = reader.readLine()) != null) {
				template.append(line);
				template.append("\n");
			}
		} catch (IOException ex) {
			throw new MedleyException(Errors.CANT_READ_TEXT_FILE, new Object[] {TEMPLATE_FILE}, ex);
		}
		
		// format template string
		return MessageFormat.format(template.toString(), getPageElements());
	}

	/**
	 * @return objects to be inserted into the home page template
	 */
	protected Object[] getPageElements() {
		// TODO: customizable page elements -- attach to queries
		return new Object[] {};
	}
}
