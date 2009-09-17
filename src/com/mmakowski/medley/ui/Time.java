/*
 * Created on 2004-01-04
 */
package com.mmakowski.medley.ui;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label; 
import org.eclipse.swt.widgets.Text;

/**
 * A control allowing to show/edit a period of time.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2004/08/08 01:11:37 $
 */
class Time extends Composite {

    /** the hours control */
    protected Text hours;
    /** the minutes control */
    protected Text minutes;
    /** the seconds control */
    protected Text seconds;
    /** is this control editable? */
    protected boolean editable;
    
    public Time(Composite parent, int style) {
        super(parent, style);
        editable = ((style & SWT.READ_ONLY) == 0) ? true : false; 
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
     * Set the time this control holds.
     * @param h the number of hours
     * @param m the number of minutes
     * @param s the number of seconds
     */
    public void setTime(int h, int m, int s) {
        hours.setText(String.valueOf(h));
        DecimalFormat df = new DecimalFormat("00");
        minutes.setText(df.format(m));
        seconds.setText(df.format(s));
    }
    
    /**
     * Set the time this control holds.
     * @param time the time
     */
    public void setTime(Date time) {
    	if (time == null) {
    		setTime(0, 0, 0);
    		return;
    	}
    	Calendar cal = new GregorianCalendar();
    	cal.setTime(time);
    	setTime(cal.get(Calendar.HOUR_OF_DAY),
    			cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND));
    }
    
    public Date getTime() {
    	Calendar cal = new GregorianCalendar();
    	try {
	    	cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours.getText()));
	    	cal.set(Calendar.MINUTE, Integer.parseInt(minutes.getText()));
	    	cal.set(Calendar.SECOND, Integer.parseInt(seconds.getText()));
    	} catch (NumberFormatException ex) {
    		return null;
    	}
    	return cal.getTime();
    }
    
    /**
     * Initialize the widgets in the pane.
     */
    protected void initWidgets() {
        int txtStyle = SWT.BORDER;
        if (!editable) {
            txtStyle |= SWT.READ_ONLY;
        }
        
        // hours
        hours = new Text(this, txtStyle | SWT.SINGLE | SWT.RIGHT);
        hours.setTextLimit(4);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, 0);
        data.width = 12;
        hours.setLayoutData(data);
        hours.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		hours.selectAll();
        	}
        });

        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(":");
        data = new FormData();
        data.top = new FormAttachment(0, 2);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, 26);
        data.width = 4;
        lbl.setLayoutData(data);

        // minutes
        minutes = new Text(this, txtStyle | SWT.SINGLE);
        minutes.setTextLimit(2);
        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, 32);
        data.width = 12;
        minutes.setLayoutData(data);
        minutes.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		minutes.selectAll();
        	}
        });

        lbl = new Label(this, SWT.NONE);
        lbl.setText(":");
        data = new FormData();
        data.top = new FormAttachment(0, 2);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, 58);
        data.width = 4;
        lbl.setLayoutData(data);

        // seconds
        seconds = new Text(this, txtStyle | SWT.SINGLE);
        seconds.setTextLimit(2);
        data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, 64);
        data.width = 12;
        seconds.setLayoutData(data);
        seconds.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		seconds.selectAll();
        	}
        });
    }

    /**
     * Add a listener that would be invoked any time the
     * contents of this control are modified.
     * @param ml the ModifyListener
     */
    public void addModifyListener(ModifyListener ml) {
    	// add given listener to all the events
    	hours.addModifyListener(ml);
    	minutes.addModifyListener(ml);
    	seconds.addModifyListener(ml);
    }
}

