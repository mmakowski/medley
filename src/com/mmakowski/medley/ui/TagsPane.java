/*
 * Created on 15-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.HierarchyNode;
import com.mmakowski.medley.data.Tag;
import com.mmakowski.medley.data.TagGroup;
import com.mmakowski.medley.data.Taggable;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane presenting tree of tags for given Taggable item.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/05/08 10:02:58 $
 */
public class TagsPane extends Composite {
	protected static final String ID = "id";
	
	protected static final int COL_NAME = 0;
	protected static final int COL_VALUE = 1;

	/** the taggable item */
	protected Taggable item;
    /** the table */
    protected Tree table;
    /** are the values editable? */
    protected boolean editable;
	
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param item the item whose artists are presented
     */
    public TagsPane(Composite parent, int style, 
    				boolean editable, Taggable item) throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.item = item;
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }
	
    /**
	 * Initialize the widgets in the pane.
	 */
	protected void initWidgets() throws MedleyException {
	    table = new Tree(this, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
	    // add columns
	    TreeColumn column = new TreeColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "tag"));
	    column.setWidth(160);
	    column = new TreeColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "value"));
	    column.setWidth(160);
	    
	    FormData data = new FormData();
	    data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    table.setLayoutData(data);
	    table.pack();

	    // create editors
	    final TreeEditor valueEditor = new TreeEditor(table);
	    valueEditor.grabHorizontal = true;

	    table.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e){
	            // Clean up any previous editor control
	        	Control oldEditor = valueEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	
	            // Identify the selected row
	            final TreeItem titem = (TreeItem)e.item;
	            if (titem == null) { 
	            	// no item selected
	            	return;
	            }
	            if (titem.getData(ID) == null) {
	            	// this is a group item
	            	return;
	            }
	            final int tagId = ((Integer) titem.getData(ID)).intValue(); 
	            
	            try {
		            // the editor for values
		            Tag t = Tag.load(tagId);
		            switch (t.getType()) {
		            case Tag.TYPE_ENUM:
		            case Tag.TYPE_LIST:
		            	// combo box for enum/list
			            CCombo combo = new CCombo(table, SWT.FLAT | (t.getType() == Tag.TYPE_ENUM ? SWT.READ_ONLY : SWT.NONE));
			            combo.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			            combo.setItems(toStringArray(t.getValues()));
			            combo.setText(titem.getText(COL_VALUE));
			            combo.addModifyListener(new ModifyListener() {
			                public void modifyText(ModifyEvent e) {
			                    CCombo cmb = (CCombo) valueEditor.getEditor();
			                    valueEditor.getItem().setText(COL_VALUE, cmb.getText());
			                    // save the change to the model
			                    try {
			                    	item.setTagValue(tagId, cmb.getText());
			                    } catch (MedleyException ex) {
			                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
			                    }
			                }
			            });
			            valueEditor.setEditor(combo, titem, COL_VALUE);
		            	break;
		            default:
		            	// text field
			            Text name = new Text(table, SWT.FLAT);
			            name.setText(titem.getText(COL_VALUE));
			            name.addModifyListener(new ModifyListener() {
			                public void modifyText(ModifyEvent e) {
			                    Text txt = (Text) valueEditor.getEditor();
			                    valueEditor.getItem().setText(COL_VALUE, txt.getText());
			                    // save the change to the model
			                    try {
			                    	item.setTagValue(tagId, txt.getText());
			                    } catch (MedleyException ex) {
			                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
			                    }
			                }
			            });
			            valueEditor.setEditor(name, titem, COL_VALUE);
		            	break;
		            }
		            t.dispose();
	            } catch (MedleyException ex) {
                	(new ExceptionWindow(Display.getCurrent(), ex)).show();
	            }
	        }
	    });        

	    refresh();
	}
	
	/**
	 * @param v a Vector containing strings
	 * @return string array containing strings from v
	 */
	protected String[] toStringArray(Vector v) {
		String arr[] = new String[v.size()];
		int j = 0;
		for (Iterator i = v.iterator(); i.hasNext();) {
			arr[j++] = (String) i.next();
		}
		return arr;
	}
	
	/**
	 * Refresh items displayed in the table tree.
	 * @throws MedleyException
	 */
	public void refresh() throws MedleyException {
		// create tag hierarchy display
		table.removeAll();
		HierarchyNode root = TagGroup.getTagHierarchy(item.getTaggableType());
		addItems(table, root);
	}
	
	/**
	 * Add table tree items under given item
	 * @param w widget to which TableTreeItems should be added
	 * @param parent hierarchy node whose children should be added
	 * @throws MedleyException
	 */
	public void addItems(Widget w, HierarchyNode parent) throws MedleyException {
		if (parent.getChildren() == null) {
			return;
		}
		for (Iterator i = parent.getChildren().iterator(); i.hasNext();) {
			HierarchyNode n = (HierarchyNode) i.next();
			TreeItem ttItem = null;
			if (w instanceof Tree) {
				 ttItem = new TreeItem((Tree) w, SWT.NONE);
			} else {
				 ttItem = new TreeItem((TreeItem) w, SWT.NONE);
			}
			ttItem.setText(COL_NAME, n.getLabel());
			if (n.getType() == HierarchyNode.GROUP) {
				addItems(ttItem, n);
				ttItem.setExpanded(true);
				// set bold font
				Font f = ttItem.getFont();
				FontData[] data = f.getFontData();
				int style = data[0].getStyle();
				data[0].setStyle(style | SWT.BOLD);
				f = new Font(Display.getCurrent(), data[0]);
				ttItem.setFont(f);
			} else if (n.getType() == HierarchyNode.ELEMENT) {
				ttItem.setData(ID, new Integer(n.getId()));
				ttItem.setText(COL_VALUE, item.getTagValue(n.getId())); 
			}
		}
	}
}
