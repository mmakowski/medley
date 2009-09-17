/*
 * Created on 2004-08-06
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Artist;
import com.mmakowski.medley.resources.Resources;

/**
 * The pane allowing to view and edit basic artist information.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $  $Date: 2004/08/08 01:11:37 $
 */
public class ArtistBasicInfoPane extends Composite {

    /** the album title control */
    protected Text name;
    /** original release year control */
    protected Text sortName;
    /** option buttons for artist type */
    protected Button individual;
    protected Button ensemble;
    /** the album notes control */
    protected Text notes;    
    /** are the values editable? */
    protected boolean editable;
    /** the artist */
    protected Artist artist;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param album the album whose data is presented
     */
    public ArtistBasicInfoPane(Composite parent, int style, 
    						  boolean editable, Artist artist) 
    		throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.artist = artist;
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

        // the artist name
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "name"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        name = new Text(this, txtStyle | SWT.SINGLE);
        name.setText(artist.getName());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        name.setLayoutData(data);
        name.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	artist.setName(name.getText());
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        name.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		name.selectAll();
        	}
        });
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;
        
        // the artist sort name
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "sortName"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        sortName = new Text(this, txtStyle | SWT.SINGLE);
        sortName.setText(artist.getSortName());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        sortName.setLayoutData(data);
        sortName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	artist.setSortName(sortName.getText());
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        sortName.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		sortName.selectAll();
        	}
        });
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;

        // type
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "type"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        individual = new Button(this, SWT.RADIO);
        individual.setText(Resources.getStr(this, "individual"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 100;
        individual.setLayoutData(data);
        individual.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event e) {
        		if (individual.getSelection()) {
                    try {
            			artist.setType(Artist.INDIVIDUAL);
                    } catch (MedleyException ex) {
                    	(new ExceptionWindow(getDisplay(), ex)).show();
                    }
        		}
        	}
        });
        ensemble = new Button(this, SWT.RADIO);
        ensemble.setText(Resources.getStr(this, "ensemble"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 200 + 2 * Settings.ITEM_SPACING_H);
        data.width = 100;
        ensemble.setLayoutData(data);
        ensemble.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event e) {
        		if (ensemble.getSelection()) {
                    try {
            			artist.setType(Artist.ENSEMBLE);
                    } catch (MedleyException ex) {
                    	(new ExceptionWindow(getDisplay(), ex)).show();
                    }
        		}
        	}
        });
    	individual.setSelection(artist.getType() == Artist.INDIVIDUAL);
    	ensemble.setSelection(artist.getType() == Artist.ENSEMBLE);
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;
        
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
        notes.setText(artist.getComments());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        notes.setLayoutData(data);
        notes.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	artist.setComments(notes.getText());
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
    }
}
