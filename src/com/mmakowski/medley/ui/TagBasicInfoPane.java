/*
 * Created on 07-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Tag;
import com.mmakowski.medley.data.TagGroup;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane allowing to edit basic tag parameters.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/05/22 00:07:38 $
 */
public class TagBasicInfoPane extends Composite {

	/** the parent window */
	protected TagWindow window;
    /** the album title control */
    protected Text name;
    /** the label control */
    protected Combo group;
    /** 'text' tag type */
    protected Button textBtn;
    /** 'list' tag type */
    protected Button listBtn;
    /** 'enum' tag type */
    protected Button enumBtn;
    /** are the values editable? */
    protected boolean editable;
    /** the tag */
    protected Tag tag;
    /** tag group ids */
    protected int[] groupIds;
    
    /**
     * Construct the pane.
     * @param window the parent window
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param tag the tag whose data is presented
     */
    public TagBasicInfoPane(Composite parent, int style, 
    						TagWindow window, boolean editable, Tag tag) 
    		throws MedleyException {
        super(parent, style);
        this.window = window;
        this.editable = editable;
        this.tag = tag;
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
        
        // the tag name
        Label lbl = new Label(this, SWT.NONE);
        lbl.setText(Resources.getStr(this, "name"));
        FormData data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = 100;
        lbl.setLayoutData(data);
        name = new Text(this, txtStyle | SWT.SINGLE);
        name.setText(tag.getName());
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 100 + Settings.ITEM_SPACING_H);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        name.setLayoutData(data);
        name.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                try {
                	tag.setName(name.getText());
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

        // tag group
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
        Vector tgv = TagGroup.getAllTagGroups();
        groupIds = new int[tgv.size() + 1];
        // add "none" option for parent group
        group.add(Resources.getStr(this, "none"));
        groupIds[0] = 0;
        if (tag.getTagGroupId() == 0) {
        	group.select(0);
        }
        int gi = 1;
        for (Iterator i = tgv.iterator(); i.hasNext();) {
        	TagGroup tg = (TagGroup) i.next();
        	group.add(tg.getName());
        	groupIds[gi++] = tg.getId();
        	if (tag.getTagGroupId() == tg.getId()) {
        		group.select(gi - 1);
        	}
        }
        TagGroup.disposeAll(tgv);
        group.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
            	int sel = group.getSelectionIndex();
            	if (sel == -1) {
            		return;
            	}
                try {
                	tag.setTagGroupId(groupIds[sel]);
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
        
        textBtn = new Button(this, SWT.RADIO);
        textBtn.setText(Resources.getStr(this, "text"));
	    textBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		tag.setType(Tag.TYPE_TEXT);
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        textBtn.setLayoutData(data);
        if (tag.getType() == Tag.TYPE_TEXT) {
        	textBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.RADIO_SPACING_V;
        
        listBtn = new Button(this, SWT.RADIO);
        listBtn.setText(Resources.getStr(this, "list"));
	    listBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		tag.setType(Tag.TYPE_LIST);
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        listBtn.setLayoutData(data);
        if (tag.getType() == Tag.TYPE_LIST) {
        	listBtn.setSelection(true);
        }
        
        top += Settings.LINE_HEIGHT + Settings.RADIO_SPACING_V;
        
        enumBtn = new Button(this, SWT.RADIO);
        enumBtn.setText(Resources.getStr(this, "enum"));
	    enumBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		tag.setType(Tag.TYPE_ENUM);
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
        data = new FormData();
        data.top = new FormAttachment(0, top);
        data.height = Settings.LINE_HEIGHT;
        data.left = new FormAttachment(0, left);
        data.width = 100;
        enumBtn.setLayoutData(data);
        if (tag.getType() == Tag.TYPE_ENUM) {
        	enumBtn.setSelection(true);
        }
        
    }
    
}
