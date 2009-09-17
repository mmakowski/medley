/*
 * Created on 2003-12-25
 */
package com.mmakowski.medley.ui;

import java.util.Date;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo; 
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label; 
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Album;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.events.IntegerVerifier;

/**
 * The pane allowing to view and edit basic album information.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $ $Date: 2005/05/22 00:07:38 $
 */
class AlbumBasicInfoPane extends Composite {

    /** the album title control */
    protected Text title;
    /** original release year control */
    protected Text origReleaseYear;
    /** release year control */
    protected Text releaseYear;
    /** the label control */
    protected Combo label;
    /** the playing time control */
    protected Time playingTime;
    /** the album notes control */
    protected Text notes;    
    /** are the values editable? */
    protected boolean editable;
    /** the album */
    protected Album album;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param album the album whose data is presented
     */
    public AlbumBasicInfoPane(Composite parent, int style, 
    						  boolean editable, Album album) 
    		throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.album = album;
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
     * Initialize the widgets in the pane.
     */
    protected void initWidgets() throws MedleyException {
        int txtStyle = SWT.BORDER;
        if (!editable) {
            txtStyle |= SWT.READ_ONLY;
        }
        int cmbStyle = SWT.BORDER;
        if (!editable) {
            cmbStyle |= SWT.READ_ONLY;
        } else {
            cmbStyle |= SWT.DROP_DOWN; 
        }
        
        int top = Settings.MARGIN_TOP;
        
        // the album title
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "title"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        title = new Text(this, txtStyle | SWT.SINGLE);
        title.setText(album.getTitle());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        title.setLayoutData(data);
        title.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	album.setTitle(title.getText());
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        title.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		title.selectAll();
        	}
        });
        
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;

        // release year
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "releaseYear"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "original"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 100;
        lbl.setLayoutData(data);
        origReleaseYear = new Text(this, txtStyle | SWT.SINGLE);
        origReleaseYear.setText(String.valueOf(album.getOriginalReleaseYear()));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 200 + 2 * Settings.ITEM_SPACING_H);
        data.width = 32;
        origReleaseYear.setLayoutData(data);
        origReleaseYear.addVerifyListener(new IntegerVerifier(0, 2100));
        origReleaseYear.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	if (origReleaseYear.getText().length() > 0) {
	            	try {
	                	int year = Integer.parseInt(origReleaseYear.getText());
	                	album.setOriginalReleaseYear(year);
	            	} catch (MedleyException ex) {
	                	(new ExceptionWindow(getDisplay(), ex)).show();
	                }
            	}
            }
        });
        origReleaseYear.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		origReleaseYear.selectAll();
        	}
        });
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "thisRelease"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 100;
        lbl.setLayoutData(data);
        releaseYear = new Text(this, txtStyle | SWT.SINGLE);
        releaseYear.setText(String.valueOf(album.getReleaseYear()));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 200 + 2 * Settings.ITEM_SPACING_H);
        data.width = 32;
        releaseYear.setLayoutData(data);
        releaseYear.addVerifyListener(new IntegerVerifier(0, 2100));
        releaseYear.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	if (releaseYear.getText().length() > 0) {
	            	try {
	                	int year = Integer.parseInt(releaseYear.getText());
	                	album.setReleaseYear(year);
	            	} catch (MedleyException ex) {
	                	(new ExceptionWindow(getDisplay(), ex)).show();
	                }
            	}
            }
        });
        releaseYear.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		releaseYear.selectAll();
        	}
        });
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;

        // the label
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "label"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        label = new Combo(this, cmbStyle);
        label.setText(album.getLabel());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        label.setLayoutData(data);
        // add the labels entered so far to the list
        for (Iterator i = Album.getLabels().iterator(); i.hasNext();) {
        	label.add((String) i.next());
        }
        label.select(label.indexOf(album.getLabel()));
        label.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	album.setLabel(label.getText());
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        top += Settings.COMBO_HEIGHT + Settings.ITEM_SPACING_V;
        
        // playing time
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "time"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.TIME_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        playingTime = new Time(this, editable ? SWT.NONE : SWT.READ_ONLY);
        playingTime.setTime(album.getLength());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        data.height = Settings.TIME_HEIGHT;
        playingTime.setLayoutData(data);
        playingTime.addModifyListener(new ModifyListener () {
            public void modifyText(ModifyEvent e) {
                try {
                	Date time = playingTime.getTime();
                	if (time != null) {
                		album.setLength(time);
                	}
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        top += Settings.TIME_HEIGHT + Settings.ITEM_SPACING_V; 
        
        // album notes
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "notes"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        notes = new Text(this, txtStyle | SWT.MULTI | SWT.WRAP | SWT.VERTICAL);
        notes.setText(album.getComments());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        notes.setLayoutData(data);
        notes.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	album.setComments(notes.getText());
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
    }
    
}
