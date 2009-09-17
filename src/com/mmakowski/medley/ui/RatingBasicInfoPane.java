/*
 * Created on 23-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Rating;
import com.mmakowski.medley.data.RatingGroup;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane allowing to edit basic rating parameters.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/05/22 00:07:38 $
 */
public class RatingBasicInfoPane extends Composite {
    // possible actions when range changes
    public static final int RANGE_CHANGE_DONT_SCALE = 0;
    public static final int RANGE_CHANGE_SCALE = 1;
    public static final int RANGE_CHANGE_ASK_USER = 2;
    
    // preferences paths
    public static final String PREF_ON_RANGE_CHANGE = "/rating_basic_info_pane/on_range_change";
        
    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(RatingBasicInfoPane.class);
    /** logger */
    private static Logger log = Logger.getLogger(RatingBasicInfoPane.class.getName());

    /** the rating name control */
    protected Text name;
    /** the group control */
    protected Combo group;
    /** 'decimal' rating type */
    protected Button decimalBtn;
    /** 'whole number' rating type */
    protected Button wholeNumBtn;
    /** 'percentage' rating type */
    protected Button percentageBtn;
    /** 'letter' rating type */
    protected Button letterBtn;
    /** min value */
    protected Text minValue;
    /** max value */
    protected Text maxValue;
    /** are the values editable? */
    protected boolean editable;
    /** the rating */
    protected Rating rating;
    /** tag group ids */
    protected int[] groupIds;
    /** 
     * a flag saying that an event is currently being handled
     * and that lost focus events should not be processed.
     */
    private boolean ignoreFocusLost = false;
    
    /**
     * Construct the pane.
     * @param window the parent window
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param rating the tag whose data is presented
     */
    public RatingBasicInfoPane(Composite parent, int style, 
    						boolean editable, Rating rating) 
    		throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.rating = rating;
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
        
        // the rating name
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "name"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        name = new Text(this, txtStyle | SWT.SINGLE);
        name.setText(rating.getName());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        name.setLayoutData(data);
        name.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	rating.setName(name.getText());
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

        // rating group
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "group"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        group = new Combo(this, cmbStyle | SWT.READ_ONLY);
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        group.setLayoutData(data);
        // add groups to the list and select parent for this tag
        Vector tgv = RatingGroup.getAllRatingGroups();
        groupIds = new int[tgv.size() + 1];
        // add "none" option for parent group
        group.add(Resources.getStr(this, "none"));
        groupIds[0] = 0;
        if (rating.getRatingGroupId() == 0) {
        	group.select(0);
        }
        int gi = 1;
        for (Iterator i = tgv.iterator(); i.hasNext();) {
        	RatingGroup tg = (RatingGroup) i.next();
        	group.add(tg.getName());
        	groupIds[gi++] = tg.getId();
        	if (rating.getRatingGroupId() == tg.getId()) {
        		group.select(gi - 1);
        	}
        }
        RatingGroup.disposeAll(tgv);
        group.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
            	int sel = group.getSelectionIndex();
            	if (sel == -1) {
            		return;
            	}
                try {
                	rating.setRatingGroupId(groupIds[sel]);
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
        	}
        });
        top += Settings.COMBO_HEIGHT + Settings.ITEM_SPACING_V;

        // type buttons
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "type"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        
        int left = Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H;
        
        decimalBtn = new Button(this, SWT.RADIO);
        decimalBtn.setText(Resources.getStr(this, "decimal"));
	    decimalBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	if (decimalBtn.getSelection()) {
	        		setRatingType(Rating.TYPE_DECIMAL);
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        decimalBtn.setLayoutData(data);
        if (rating.getType() == Rating.TYPE_DECIMAL) {
        	decimalBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.RADIO_SPACING_V;
        
        wholeNumBtn = new Button(this, SWT.RADIO);
        wholeNumBtn.setText(Resources.getStr(this, "wholeNumber"));
	    wholeNumBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	if (wholeNumBtn.getSelection()) {
	        		setRatingType(Rating.TYPE_WHOLE_NUMBER);
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        wholeNumBtn.setLayoutData(data);
        if (rating.getType() == Rating.TYPE_WHOLE_NUMBER) {
        	wholeNumBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.RADIO_SPACING_V;

        percentageBtn = new Button(this, SWT.RADIO);
        percentageBtn.setText(Resources.getStr(this, "percentage"));
	    percentageBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	if (percentageBtn.getSelection()) {
	        		setRatingType(Rating.TYPE_PERCENTAGE);
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        percentageBtn.setLayoutData(data);
        if (rating.getType() == Rating.TYPE_PERCENTAGE) {
        	percentageBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.RADIO_SPACING_V;

        letterBtn = new Button(this, SWT.RADIO);
        letterBtn.setText(Resources.getStr(this, "letter"));
	    letterBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	if (letterBtn.getSelection()) {
	        		setRatingType(Rating.TYPE_LETTER);
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        letterBtn.setLayoutData(data);
        if (rating.getType() == Rating.TYPE_LETTER) {
        	letterBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;

        // min value
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "minValue"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        minValue = new Text(this, txtStyle | SWT.SINGLE);
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 32;
        minValue.setLayoutData(data);
        minValue.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		minValue.selectAll();
        	}
        	public void focusLost(FocusEvent e) {
                if (ignoreFocusLost) {
                    return;
                }
                setRange(minValue.getText(), maxValue.getText());
                updateRangeControls();
        	}
        });
        
        top += Settings.LINE_HEIGHT + Settings.ITEM_SPACING_V;

        // max value
        lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "maxValue"));
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        maxValue = new Text(this, txtStyle | SWT.SINGLE);
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.width = 32;
        maxValue.setLayoutData(data);
        maxValue.addFocusListener(new FocusAdapter() {
        	public void focusGained(FocusEvent e) {
        		maxValue.selectAll();
        	}
        	public void focusLost(FocusEvent e) {
                if (ignoreFocusLost) {
                    return;
                }
                setRange(minValue.getText(), maxValue.getText());
                updateRangeControls();
            }
        });
        
        updateRangeControls();
    }

    /**
     * Set rating type and modify control state
     * @param type
     */
    private boolean setRatingType(int type) {
    	try {
            boolean scale = false;
            switch (prefs.getInt(PREF_ON_RANGE_CHANGE, RANGE_CHANGE_ASK_USER)) {
            case RANGE_CHANGE_DONT_SCALE: scale = false; break;
            case RANGE_CHANGE_SCALE: scale = true; break;
            case RANGE_CHANGE_ASK_USER:
            default:
                ignoreFocusLost = true;
                MessageBox mb = new MessageBox(getShell(), 
                           SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
                try {
                    mb.setText(Resources.getStr(this, "mb.typeChanged.title"));
                    mb.setMessage(Resources.getStr(this, "mb.typeChanged.msg"));
                } catch (ResourceException ex) {
                    (new ExceptionWindow(Display.getCurrent(), ex)).show();
                }
                switch (mb.open()) {
                case SWT.YES: scale = true; break;
                case SWT.NO: scale = false; break;
                case SWT.CANCEL: ignoreFocusLost = false; return false;
                }
                ignoreFocusLost = false;
            }
    		rating.setType(type, scale);
    	} catch (MedleyException ex) {
    		(new ExceptionWindow(getDisplay(), ex)).show();
    	}
    	updateRangeControls();
        return true;
    }
    
    /**
     * Set new range for this rating.
     * @param newMin new min value
     * @param newMax new max value
     * @return true if change was successful, false otherwise
     */
    private boolean setRange(String newMin, String newMax) {
        try {
            // check that something's actually changed
            if (newMax.equals(rating.getMaxValueString()) && 
                newMin.equals(rating.getMinValueString())) {
                return false;
            }
            // check whether to scale existing values or not
            boolean scale = false;
            switch (prefs.getInt(PREF_ON_RANGE_CHANGE, RANGE_CHANGE_ASK_USER)) {
            case RANGE_CHANGE_DONT_SCALE: scale = false; break;
            case RANGE_CHANGE_SCALE: scale = true; break;
            case RANGE_CHANGE_ASK_USER:
            default:
                ignoreFocusLost = true;
                MessageBox mb = new MessageBox(getShell(), 
                           SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
                try {
                    mb.setText(Resources.getStr(this, "mb.rangeChanged.title"));
                    mb.setMessage(Resources.getStr(this, "mb.rangeChanged.msg"));
                } catch (ResourceException ex) {
                    (new ExceptionWindow(Display.getCurrent(), ex)).show();
                }
                switch (mb.open()) {
                case SWT.YES: scale = true; break;
                case SWT.NO: scale = false; break;
                case SWT.CANCEL: ignoreFocusLost = false; return false;
                }
                ignoreFocusLost = false;
            }
            // perform the change
            if (!newMax.equals(rating.getMaxValueString())) {
                rating.setMaxValueString(newMax, scale);
            }
            if (!newMin.equals(rating.getMinValueString())) {
                rating.setMinValueString(newMin, scale);
            }
        } catch (MedleyException ex) {
            (new ExceptionWindow(getDisplay(), ex)).show();
            return false;
        }
        return true;
    }
    
    /**
     * update range controls
     *
     */
    protected void updateRangeControls() {
    	try {
	    	minValue.setText(rating.getMinValueString());
	    	minValue.setEnabled(!Rating.minValueFixed(rating.getType()));
	    	maxValue.setText(rating.getMaxValueString());
	    	maxValue.setEnabled(!Rating.maxValueFixed(rating.getType()));
    	} catch (MedleyException ex) {
    		(new ExceptionWindow(getDisplay(), ex)).show();
    	}
    }
}
