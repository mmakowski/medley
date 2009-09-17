/*
 * Created on 01-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Track;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.events.IntegerVerifier;

/**
 * A pane allowing to view and edit basic track information.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/05/22 00:07:38 $
 */
public class TrackBasicInfoPane extends Composite {

	/** track number control */
	protected Text number;
    /** the track title control */
    protected Text title;
    /** the playing time control */
    protected Time playingTime;
    /** the track notes control */
    protected Text notes;    
    /** are the values editable? */
    protected boolean editable;
    /** the track */
    protected Track track;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param track the track whose data is presented
     */
    public TrackBasicInfoPane(Composite parent, int style, 
    						   boolean editable, Track track) 
    		throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.track = track;
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
        
        // TODO: record name and record edit button
        
        // track number
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "number"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        number = new Text(this, txtStyle | SWT.SINGLE);
        number.setText(String.valueOf(track.getNumber()));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 30;
        number.setLayoutData(data);
        number.addVerifyListener(new IntegerVerifier(0, 999));
        number.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (number.getText().length() > 0) {
	            	try {
	            		track.setNumber(Integer.parseInt(number.getText()));
	                } catch (MedleyException ex) {
	                	(new ExceptionWindow(getDisplay(), ex)).show();
	                }
                }
            }
        });
        number.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		number.selectAll();
        	}
        });
        
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;
        
        // the record title
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "title"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        title = new Text(this, txtStyle | SWT.SINGLE);
        String tmp = track.getTitle();
        title.setText(tmp == null ? "" : tmp);
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        title.setLayoutData(data);
        title.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	track.setTitle(title.getText());
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
        playingTime.setTime(track.getLength());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.TIME_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        playingTime.setLayoutData(data);
        playingTime.addModifyListener(new ModifyListener () {
            public void modifyText(ModifyEvent e) {
                try {
                	Date time = playingTime.getTime();
                	if (time != null) {
                		track.setLength(time);
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
        notes.setText(track.getComments());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        notes.setLayoutData(data);
        notes.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	track.setComments(notes.getText());
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
    }
    

}
