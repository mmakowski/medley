/*
 * Created on 2004-08-06
 */
package com.mmakowski.medley.ui;


import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mmakowski.medley.Medley;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.windows.Window;
import com.mmakowski.util.WebBrowser;

/**
 * The About Medley window.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.9 $  $Date: 2005/05/22 00:07:38 $
 */
public class AboutWindow extends Window {
    private static final String MEDLEY_URL = "http://www.mmakowski.com/medley";
    
    /** logger */
    private static Logger log = Logger.getLogger(AboutWindow.class.getName());

    public AboutWindow(Shell parent) throws ResourceException {
		super(parent);
        shell = new Shell(parent, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
        shell.setText(Resources.getStr(this, "title"));
        initWidgets();
	}

    /**
     * Show the window and return when it's closed.
     */
    public void show() {
        shell.pack ();
        center();
        shell.open ();
        Display disp = parent.getDisplay();
        while (!shell.isDisposed ()) {
            if (!disp.readAndDispatch ()) disp.sleep ();
        }
    }

	/**
	 * @see com.mmakowski.swt.events.Window#initWidgets()
	 */
	protected void initWidgets() throws ResourceException {
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);

        int top = Settings.MARGIN_TOP;
        Label lbl = new Label(shell, SWT.CENTER);
        lbl.setText("Medley");
        lbl.setFont(new Font(shell.getDisplay(), 
        					 new FontData(Settings.FONT_LARGE)));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = 25; 
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        lbl.setLayoutData(data);
        top += 25 + Settings.ITEM_SPACING_V;
        
        lbl = new Label(shell, SWT.NONE);
        lbl.setText(Resources.formatStr(this, "version", new Object[] {Medley.VERSION}) + 
        		    "\n" + Medley.COPYRIGHT);
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT * 2;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        lbl.setLayoutData(data);
        top += Settings.LINE_HEIGHT * 2 + Settings.ITEM_SPACING_V;

        //final Hyperlink link = new Hyperlink(shell, SWT.NONE);
        final Link link = new Link(shell, SWT.NO_FOCUS);
        link.setText("<a href=\"" + MEDLEY_URL + "\">" + MEDLEY_URL + "</a>");
        link.setCapture(true);
        link.addSelectionListener(new SelectionListener() {
        	public void widgetSelected(SelectionEvent e) {
                WebBrowser.displayURL(MEDLEY_URL);
        	}
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        link.setLayoutData(data);
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;
        
        // TODO: some more info
        
        Button btn = new Button(shell, SWT.NONE);
        btn.setText(Resources.getStr("close"));
        btn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	shell.close();
            }
        });        
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.BUTTON_HEIGHT;
        data.left = new FormAttachment(50, -(Settings.BUTTON_WIDTH / 2));
        data.width = Settings.BUTTON_WIDTH;
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        btn.setLayoutData(data);
        
	}

	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		// TODO: dispose widgets
	}
}
