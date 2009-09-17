/*
 * Created on 23-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.HierarchyNode;
import com.mmakowski.medley.data.Ratable;
import com.mmakowski.medley.data.Rating;
import com.mmakowski.medley.data.RatingGroup;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane presenting tree of ratings for given Ratable item.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.7 $ $Date: 2005/05/08 10:02:58 $
 */
public class RatingsPane extends Composite {
	// preferences paths
	public static final String PREF_CONFIRM_DELETE_LATEST_SCORE = "/ratings_pane/confirm_delete_latest_score";

	protected static final String ID = "id";
	
	protected static final int COL_NAME = 0;
	protected static final int COL_SCORE = 1;

    /** preferences */
    protected static final Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(RatingsPane.class);

    /** the ratable item */
	protected Ratable item;
    /** the table */
    protected Tree table;
    /** are the values editable? */
    protected boolean editable;
	
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param item the item whose ratings are presented
     */
    public RatingsPane(Composite parent, int style, 
    				boolean editable, Ratable item) throws MedleyException {
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
	    column.setText(Resources.getStr(this, "rating"));
	    column.setWidth(160);
	    column = new TreeColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "score"));
	    column.setWidth(50);
	    
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
	            final int ratingId = ((Integer) titem.getData(ID)).intValue(); 
	            
	            try {
		            // the editor for values
		            Rating r = Rating.load(ratingId);
		            switch (r.getType()) {
		            default:
		            	// text field
			            Text name = new Text(table, SWT.FLAT);
			            name.setText(titem.getText(COL_SCORE));
			            name.addFocusListener(new FocusAdapter() {
							public void focusLost(FocusEvent arg0) {
			                    Text txt = (Text) valueEditor.getEditor();
			                    try {
				                    if (txt.getText().length() > 0) {
					                    valueEditor.getItem().setText(COL_SCORE, txt.getText());
					                    // save the change to the model
				                    	item.setRatingScore(ratingId, txt.getText());
				                    } else if (item.getRatingScoreString(ratingId).length() > 0){
				                    	// User has deleted the score displayed
				                    	boolean delete = true;
				                		if (prefs.getBoolean(PREF_CONFIRM_DELETE_LATEST_SCORE, true)) {
					                    	// Ask user to confirm
				                			MessageBox mb = new MessageBox(getShell(), 
													   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
											mb.setText(Resources.getStr(this, "mb.deleteLatestScore.title"));
											mb.setMessage(Resources.getStr(this, "mb.deleteLatestScore.msg"));
											if (mb.open() == SWT.NO) {
												delete = false;
											}
				                		}
				                    	if (delete) {
					                    	// Delete latest score
					                    	item.deleteLatestRatingScore(ratingId);
					                    	valueEditor.getItem().setText(COL_SCORE, item.getRatingScoreString(ratingId));
				                    	}
				                    }
			                    } catch (MedleyException ex) {
			                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
			                    }
							}
			            });
			            valueEditor.setEditor(name, titem, COL_SCORE);
		            	break;
		            }
		            r.dispose();
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
		HierarchyNode root = RatingGroup.getRatingHierarchy(item.getRatableType());
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
				ttItem.setText(COL_SCORE, item.getRatingScoreString(n.getId())); 
			}
		}
	}

}
