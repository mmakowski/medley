/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley.ui;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * The dialog window that presents error message. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.7 $  $Date: 2005/02/19 10:30:30 $
 */
public class ExceptionWindow {
	protected static final int DEFAULT_WIDTH = 520;
	protected static final int DEFAULT_HEIGHT = 160;
	protected static final int DEFAULT_HEIGHT_DETAILS = 300;
	
	/** most recently used height of stack trace portion of the window */ 
	protected static int detailsHeight = DEFAULT_HEIGHT_DETAILS;
	
    /** the display managing the window */
    protected Display display;
    /** an SWT Shell of the main window */
    protected Shell shell;
    /** the exception to be displayed */
    protected Exception ex;
    /** should the exception stack trace be shown? */
    protected boolean showDetails;
    /** has the "show details" state changed? */
    protected boolean detailsSwitched;
    
    // the widgets -- need to be declared here because
    // their layout will change dynamically
    /** error message label */
    protected Label errorMessage;
    /** OK button */
    protected Button okButton;
    /** Details button */
    protected Button detailsButton;
    /** stack trace text window */
    protected Text stackTrace;
    /** error icon */
    protected Canvas errorIcon;
    
    /**
     * Construct the window.
     * @param display
     */
    public ExceptionWindow(Display display, Exception ex) {
        this.display = display;
        this.ex = ex;
        showDetails = false;
        detailsSwitched = false;
		shell = new Shell(display, SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		
		try {
			shell.setText(Resources.getStr(this, "title"));
		} catch (ResourceException rex) {
			shell.setText("Medley error");
		}
        // on close remember last height of stack trace window
    	shell.addShellListener(new ShellAdapter() {
        	public void shellClosed(ShellEvent e) {
        		detailsHeight = stackTrace.getSize().y;
        	}
        });

    	FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		initWidgets();
    }
    
	/**
     * Show the window and return when it's closed.
     */
    public void show() {
    	Rectangle dispSize = display.getBounds();
        shell.setLocation((dispSize.width - shell.getSize().x) / 2,
				  		  (dispSize.height - shell.getSize().y) / 2);
        shell.open();
        
        // This is to force redraw -- at some point SWT stopped drawing
        // window contents correctly on start, but after resizing everything
        // looks fine.
        shell.setSize(shell.getSize().x + 1, shell.getSize().y + 1);
       
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
    }
    
    /**
     * Initialize the widgets in the window.
     */
    protected void initWidgets() {
    	shell.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    	
    	// error icon
    	final Image imgErr = new Image(display, "img/error.gif"); 
        errorIcon = new Canvas(shell, SWT.NO_REDRAW_RESIZE);
        errorIcon.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
            	e.gc.drawImage(imgErr, 0, 0);
            }
        });
    	
    	// create the label containing error message
    	errorMessage = new Label(shell, SWT.WRAP);
    	try {
    		errorMessage.setText((ex.getMessage() == null) ? Resources.getStr(this, "unknownError") : ex.getMessage());
    	} catch (ResourceException ex) {
    		errorMessage.setText((ex.getMessage() == null) ? "Unknown error (see details)." : ex.getMessage());
    	}
    	errorMessage.pack();

        // the OK button
    	okButton = new Button(shell, SWT.PUSH);
    	try {
    		okButton.setText(Resources.getStr(this, "close"));
    	} catch (ResourceException ex) {
    		okButton.setText("&Close");
    	}
        okButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	shell.close();
            }
        });
        
        // the Details button
    	detailsButton = new Button(shell, SWT.PUSH);
    	try {
    		detailsButton.setText(Resources.getStr(this, showDetails ? "detailsOpen" : "detailsClosed"));
    	} catch (ResourceException ex) {
    		detailsButton.setText(showDetails ? "&Details <<" : "&Details >>");
    	}
    	//final boolean s = showDetails;
        final Button fbtn = detailsButton;
        detailsButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	showDetails = !showDetails;
            	detailsSwitched = true;
            	setLayout();
            	detailsSwitched = false;
            	try {
            		fbtn.setText(Resources.getStr(this, showDetails ? "detailsOpen" : "detailsClosed"));
            	} catch (ResourceException ex) {
            		fbtn.setText(showDetails ? "&Details <<" : "&Details >>");
            	}
            }
            	
        });

        // the text area with stack trace
        stackTrace = new Text(shell, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | 
        	                         SWT.HORIZONTAL | SWT.VERTICAL);
        StringWriter buf = new StringWriter();
        ex.printStackTrace(new PrintWriter(buf));
        stackTrace.setText(buf.toString());

        errorMessage.pack();
        setLayout();
    }
    
    /**
     * Set the layout of widgets depending on whether the details
     * are being shown or not.
     */
    protected void setLayout() {
    	// error icon
    	FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.height = 32;
        data.width = 32;
        errorIcon.setLayoutData(data);

        if (showDetails) {
        	Point curSL;
      	
    		// error message label
        	curSL = errorMessage.getSize();
	    	data = new FormData();
	        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 32 + Settings.ITEM_SPACING_H);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	        data.height = curSL.y;
	        errorMessage.setLayoutData(data);
	        // the OK button
	        data = new FormData();
	        curSL = okButton.getLocation();
	        data.top = new FormAttachment(0, curSL.y);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT - Settings.BUTTON_WIDTH - 
	        								     Settings.ITEM_SPACING_H);
	        data.height = Settings.BUTTON_HEIGHT;
	        data.width = Settings.BUTTON_WIDTH;
	        okButton.setLayoutData(data);
	        // the Details button
	        data = new FormData();
	        curSL = okButton.getLocation();
	        data.top = new FormAttachment(0, curSL.y);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	        data.height = Settings.BUTTON_HEIGHT;
	        data.width = Settings.BUTTON_WIDTH;
	        detailsButton.setLayoutData(data);
	        
	        // stack trace
	        data = new FormData();
	        curSL = okButton.getLocation();
	        data.top = new FormAttachment(0, curSL.y + Settings.BUTTON_HEIGHT + Settings.ITEM_SPACING_V);
	        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	        stackTrace.setLayoutData(data);        

	        // if details have been switched on increase the window height
	        if (detailsSwitched) {
	        	curSL = shell.getSize();
	        	shell.setSize(curSL.x, curSL.y + detailsHeight + Settings.ITEM_SPACING_V);
	        }
    	} else {
        	// if details have been switched off, decrease the window height
        	if (detailsSwitched) {
        		// remember last stack trace window height
        		detailsHeight = stackTrace.getSize().y;
        		
        		Point curSL = shell.getSize();
        		shell.setSize(curSL.x, curSL.y - detailsHeight - Settings.ITEM_SPACING_V);
        	}

        	// error message label
	    	data = new FormData();
	        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 32 + Settings.ITEM_SPACING_H);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	        data.bottom = new FormAttachment(100 - Settings.MARGIN_BOTTOM - 
	        								 Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
	        errorMessage.setLayoutData(data);
	        // the OK button
	        data = new FormData();
	        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT - Settings.BUTTON_WIDTH - 
	        								     Settings.ITEM_SPACING_H);
	        data.height = Settings.BUTTON_HEIGHT;
	        data.width = Settings.BUTTON_WIDTH;
	        okButton.setLayoutData(data);
	        // the Details button
	        data = new FormData();
	        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	        data.height = Settings.BUTTON_HEIGHT;
	        data.width = Settings.BUTTON_WIDTH;
	        detailsButton.setLayoutData(data);
	        // stack trace (hidden)
	        data = new FormData();
	        data.top = new FormAttachment(100, 1);
	        data.height = detailsHeight;
	        stackTrace.setLayoutData(data);        
    	}
    }
}
