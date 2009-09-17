/*
 * Created on 04-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.ImageData;
import com.mmakowski.medley.data.Visible;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * Pane showing item's images.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/04 17:48:36 $
 */
public class ImagesPane extends Composite {

	/** should the user be allowed to add new/delete existing images? */
	protected boolean editable;
	/** the item whose images should be displayed */
	protected Visible item;
	/** the images to be displayed */
	protected Vector images;
	/** index of image currently displayed */
	protected int currentImage;
    /** next image button */
    protected Button nextBtn;
    /** previous image button */
    protected Button prevBtn;
    /** full view button */
    protected Button fullViewBtn;
    /** add image button */
    protected Button addBtn;
    /** delete image button */
    protected Button deleteBtn;
    /** export image button */
    protected Button exportBtn;
    /** the label displaying image */
	protected Label imageLbl;
	
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param item the item whose images are presented
     */
    public ImagesPane(Composite parent, int style, 
    				  boolean editable, Visible item) throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.item = item;
        this.images = item.getImages();
        this.currentImage = 0;
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
     * Initialise pane's widgets
     * @throws MedleyException
     */
    protected void initWidgets() throws MedleyException {
    	int top, left;
    	
    	// create image label
    	imageLbl = new Label(this, SWT.CENTER);
	    FormData data = new FormData();
	    data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT - Settings.BUTTON_WIDTH - Settings.ITEM_SPACING_H);
	    data.width = Settings.THUMBNAIL_MAX_WIDTH;
	    data.height = Settings.THUMBNAIL_MAX_HEIGHT;
	    imageLbl.setLayoutData(data);
	    
	    top = Settings.MARGIN_TOP + Settings.THUMBNAIL_MAX_HEIGHT + Settings.ITEM_SPACING_V;
	    left = Settings.MARGIN_LEFT;
	    
	    // prev button
	    prevBtn = new Button(this, SWT.NONE);
	    prevBtn.setText(Resources.getStr(this, "previousImage"));
        prevBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	currentImage--;
            	showCurrentImage();
            }
        });	    
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, left);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    prevBtn.setLayoutData(data);

	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    
	    // next button
	    nextBtn = new Button(this, SWT.NONE);
	    nextBtn.setText(Resources.getStr(this, "nextImage"));
        nextBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	currentImage++;
            	showCurrentImage();
            }
        });	    
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, left);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    nextBtn.setLayoutData(data);

	    top = Settings.MARGIN_TOP;
	    
	    // full view button
	    fullViewBtn = new Button(this, SWT.NONE);
	    fullViewBtn.setText(Resources.getStr(this, "fullView"));
        fullViewBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		viewImage();
            	} catch (MedleyException ex) {
            		(new ExceptionWindow(getDisplay(), ex)).show();    			
            	}
            }
        });	    
	    data = new FormData();
	    data.top = new FormAttachment(0, top);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    fullViewBtn.setLayoutData(data);
	    top += Settings.BUTTON_HEIGHT + Settings.ITEM_SPACING_V;
	    
	    // delete button
	    deleteBtn = new Button(this, SWT.NONE);
	    deleteBtn.setText(Resources.getStr(this, "deleteImage"));
        deleteBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		deleteImage();
            	} catch (MedleyException ex) {
            		(new ExceptionWindow(getDisplay(), ex)).show();    			
            	}
            }
        });	    
	    data = new FormData();
	    data.top = new FormAttachment(0, top);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    deleteBtn.setLayoutData(data);
	    top += Settings.BUTTON_HEIGHT + Settings.ITEM_SPACING_V;

	    // export button
	    exportBtn = new Button(this, SWT.NONE);
	    exportBtn.setText(Resources.getStr(this, "exportImage"));
        exportBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		exportImage();
            	} catch (MedleyException ex) {
            		(new ExceptionWindow(getDisplay(), ex)).show();    			
            	}
            }
        });	    
	    data = new FormData();
	    data.top = new FormAttachment(0, top);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    exportBtn.setLayoutData(data);
	    top += Settings.BUTTON_HEIGHT + Settings.ITEM_SPACING_V;
	    
	    // add button
	    addBtn = new Button(this, SWT.NONE);
	    addBtn.setText(Resources.getStr(this, "addImage"));
        addBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		addImage();
            	} catch (MedleyException ex) {
            		(new ExceptionWindow(getDisplay(), ex)).show();    			
            	}
            }
        });	    
	    data = new FormData();
	    data.top = new FormAttachment(0, top);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    addBtn.setLayoutData(data);
	    top += Settings.BUTTON_HEIGHT + Settings.ITEM_SPACING_V;
	    
	    showCurrentImage();
    }

    /**
     * Display full-sized image in image viewer.
     */
    protected void viewImage() throws MedleyException {
    	ImageData img = (ImageData) images.get(currentImage);
    	if (img != null) {
    		ImageWindow wnd = new ImageWindow(getShell(), img);
    		wnd.show();
    	}
    }
    
    /**
     * Add new file for current item
     * @throws MedleyException
     */
    protected void addImage() throws MedleyException {
    	FileDialog dialog = new FileDialog (getShell(), SWT.OPEN);
    	dialog.setFilterNames (new String [] {Resources.getStr("fileFilter.imageFiles"), 
    										  Resources.getStr("fileFilter.allFiles")});
    	dialog.setFilterExtensions (new String [] {"*.gif;*.jpg", "*.*"}); //Windows wild cards
    	String fn = dialog.open();
    	if (fn != null) {
    		images.add(item.addImage(fn));
    		currentImage = images.size() - 1;
    		showCurrentImage();
    	}
    }

    /**
     * Delete current image.
     * @throws MedleyException
     */
    protected void deleteImage() throws MedleyException {
		MessageBox mb = new MessageBox(getShell(), 
				   					   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		try {
			mb.setText(Resources.getStr(this, "mb.deleteImage.title"));
			mb.setMessage(Resources.getStr(this, "mb.deleteImage.msg"));
		} catch (ResourceException ex) {
			(new ExceptionWindow(getDisplay(), ex)).show();
		}
		if (mb.open() == SWT.YES) {
			item.removeImage((ImageData) images.get(currentImage));
			images.remove(currentImage);
			currentImage++;
			showCurrentImage();
		}
    }

    /**
     * Export current image to file.
     * @throws MedleyException
     */
    protected void exportImage() throws MedleyException {
    	FileDialog dialog = new FileDialog (getShell(), SWT.SAVE);
    	dialog.setFilterNames (new String [] {Resources.getStr("fileFilter.jpegFiles"), 
    										  Resources.getStr("fileFilter.allFiles")});
    	dialog.setFilterExtensions (new String [] {"*.jpg", "*.*"}); //Windows wild cards
    	String fn = dialog.open();
    	if (fn != null) {
    		if (!(fn.endsWith(".jpg") || fn.endsWith(".JPG") || fn.endsWith(".jpeg") || fn.endsWith("JPEG"))) {
    			fn += ".jpg";
    		}
    		((ImageData) images.get(currentImage)).export(fn);
    	}
    }
    
    /**
     * Display current image in image label
     *
     */
    protected void showCurrentImage() {
    	try {
    		// dispose of previous image
    		if (imageLbl.getImage() != null) {
	    		imageLbl.getImage().dispose();
	    	}
    		// display current image
	    	if (images.isEmpty()) {
	    		// show "no image" message
	    		try {
	    			imageLbl.setImage(null);
	    			imageLbl.setText(Resources.getStr(this, "noImage"));
	    		} catch (ResourceException ex) {
	    			(new ExceptionWindow(getDisplay(), ex)).show();    			
	    		}
		    	// disable/enable appropriate buttons
	    		prevBtn.setEnabled(false);
	    		nextBtn.setEnabled(false);
	    		exportBtn.setEnabled(false);
	    		deleteBtn.setEnabled(false);
	    		fullViewBtn.setEnabled(false);
	    	} else {
		    	// adjust image number
		    	if (currentImage >= images.size()) {
		    		currentImage = images.size() - 1;
		    	}
		    	if (currentImage < 0) {
		    		currentImage = 0;
		    	}
		    	// disable/enable appropriate buttons
	    		prevBtn.setEnabled(currentImage > 0);
	    		nextBtn.setEnabled(currentImage < images.size() - 1);
	    		exportBtn.setEnabled(true);
	    		deleteBtn.setEnabled(true);
	    		fullViewBtn.setEnabled(true);
	    		
		    	// scale to fit the label
		    	ImageData id = (ImageData) images.get(currentImage);
		    	org.eclipse.swt.graphics.ImageData imageData = id.getSWTImageData(); 
		    	int width = imageData.width;
		    	int height = imageData.height;
		    	if (width > Settings.THUMBNAIL_MAX_WIDTH || height > Settings.THUMBNAIL_MAX_HEIGHT) {
		    		if (width/height > Settings.THUMBNAIL_MAX_WIDTH/Settings.THUMBNAIL_MAX_HEIGHT) {
		    			width = Settings.THUMBNAIL_MAX_WIDTH;
		    			height *= ((double) width / (double) imageData.width);
		    		} else {
		    			height = Settings.THUMBNAIL_MAX_HEIGHT;
		    			width *= ((double) height / (double) imageData.height);
		    		}
		    	}
		    	Image image = new Image(getDisplay(), imageData.scaledTo(width, height));
		    	// display
		    	imageLbl.setText("");
		    	imageLbl.setImage(image);
		    }
    	} catch (Exception ex) {
			(new ExceptionWindow(getDisplay(), ex)).show();    			
    	}
    }
    
}
