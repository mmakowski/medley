/*
 * Created on 25-Dec-2004
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
import com.mmakowski.medley.data.Record;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.events.IntegerVerifier;

/**
 * The pane allowing to view and edit basic record information.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/05/22 00:07:38 $
 */
public class RecordBasicInfoPane extends Composite {

	/** record number control */
	protected Text number;
    /** the record title control */
    protected Text title;
    /** the playing time control */
    protected Time playingTime;
    /** the album notes control */
    protected Text notes;    
    /** are the values editable? */
    protected boolean editable;
    /** the record */
    protected Record record;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param record the record whose data is presented
     */
    public RecordBasicInfoPane(Composite parent, int style, 
    						   boolean editable, Record record) 
    		throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.record = record;
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
        
        // TODO: album name and album edit button
        
        // record number
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "number"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        number = new Text(this, txtStyle | SWT.SINGLE);
        number.setText(String.valueOf(record.getNumber()));
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
	            		record.setNumber(Integer.parseInt(number.getText()));
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
        String tmp = record.getTitle();
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
                	record.setTitle(title.getText());
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
        playingTime.setTime(record.getLength());
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
                		record.setLength(time);
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
        notes.setText(record.getComments());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        notes.setLayoutData(data);
        notes.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	record.setComments(notes.getText());
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
    }
    
  
}
