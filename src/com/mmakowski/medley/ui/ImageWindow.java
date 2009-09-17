/*
 * Created on 04-Jan-2005
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.ImageData;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.windows.Window;

/**
 * Window that displays image.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/24 16:02:46 $
 */
class ImageWindow extends Window {

	/** the image displayed by this window */
	protected ImageData imageData;
	protected Button closeBtn;
	protected Button exportBtn;
	protected ScrolledComposite scroll;
	protected Label viewLbl;
	
	/**
	 * @param parent parent window
	 */
	public ImageWindow(Shell parent, ImageData image) throws MedleyException {
		super(parent);
		this.imageData = image;
        shell = new Shell(parent, SWT.MAX | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
        shell.setText(Resources.getStr(this, "title"));
        // set window icon
        shell.setImages(new Image[] {new Image(shell.getDisplay(), "img/icon-imageView-16.gif"),
        							 new Image(shell.getDisplay(), "img/icon-imageView-32.gif")});
        // save the model contents when the window is closed
        shell.addShellListener(new ShellAdapter() {
        	public void shellClosed(ShellEvent e) {
        		disposeWidgets();
        	}
        });
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
		initWidgets();
	}

	/**
	 * @see com.mmakowski.swt.events.Window#initWidgets()
	 */
	protected void initWidgets() throws MedleyException {
        // create the scrolled composite
        scroll = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        scroll.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        scroll.setLayoutData(data);
        
        // view label
        viewLbl = new Label(scroll, SWT.CENTER);
        Image img = new Image(Display.getCurrent(), imageData.getSWTImageData());
        viewLbl.setImage(img);
        viewLbl.setSize(imageData.getSWTImageData().width, imageData.getSWTImageData().height);
        scroll.setContent(viewLbl);
        scroll.pack();
        
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

	    // export button
	    exportBtn = new Button(shell, SWT.NONE);
	    exportBtn.setText(Resources.getStr(this, "exportImage"));
        exportBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		exportImage();
            	} catch (MedleyException ex) {
            		(new ExceptionWindow(Display.getCurrent(), ex)).show();    			
            	}
            }
        });	    
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT - Settings.BUTTON_WIDTH - Settings.ITEM_SPACING_H);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    exportBtn.setLayoutData(data);
	    
	}

    /**
     * Display image window.
     * @see com.mmakowski.swt.windows.Window#show()
     */
    public void show() {
        shell.pack();
        super.show();
    }
    
	/**
	 * @see com.mmakowski.swt.events.Window#disposeWidgets()
	 */
	protected void disposeWidgets() {
		if (viewLbl.getImage() != null) {
			viewLbl.getImage().dispose();
		}
		viewLbl.dispose();
		scroll.dispose();
	}

    /**
     * Export current image to file.
     * @throws MedleyException
     */
    protected void exportImage() throws MedleyException {
    	FileDialog dialog = new FileDialog (shell, SWT.SAVE);
    	dialog.setFilterNames (new String [] {Resources.getStr("fileFilter.jpegFiles"), 
    										  Resources.getStr("fileFilter.allFiles")});
    	dialog.setFilterExtensions (new String [] {"*.jpg", "*.*"}); //Windows wild cards
    	String fn = dialog.open();
    	if (fn != null) {
    		if (!(fn.endsWith(".jpg") || fn.endsWith(".JPG") || fn.endsWith(".jpeg") || fn.endsWith("JPEG"))) {
    			fn += ".jpg";
    		}
    		imageData.export(fn);
    	}
    }
	
}
