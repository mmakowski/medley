/*
 * Created on 23-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.RatingGroup;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane presenting list of all rating groups. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/02/17 22:40:36 $
 */
public class RatingGroupListPane extends Composite {
	protected static final String ID = "id";
	protected static final int NO_ID = -1;

	protected static final int COL_NAME = 0;
	protected static final int COL_PARENT = 1;
	protected static final int COL_DELETE = 2;
	
	/** list control */
    protected Table table;
    /** "add group" button */
    protected Button addGroupBtn;
    /** group name cache for combo box */
    protected String[] groupNames;
    /** an array allowing to obtain group id based on selection index */
    protected int[] groupIds;
	
	
	/**
	 * @param parent parent control
	 * @param style control style
	 */
	public RatingGroupListPane(Composite parent, int style) throws MedleyException {
		super(parent, style);
        FormLayout layout = new FormLayout();
        setLayout(layout);
		initWidgets();
	}

	/**
	 * Initialise widgets in pane.
	 * @throws MedleyException
	 */
	protected void initWidgets() throws MedleyException {
	    table = new Table (this, SWT.BORDER | SWT.FULL_SELECTION);
	    table.setLinesVisible(false);
	    table.setHeaderVisible(true);
	    // add columns
	    TableColumn column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "name"));
	    column.setWidth(120);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "group"));
	    column.setWidth(120);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "actions"));
	    column.setWidth(Settings.ACTION_COLUMN_WIDTH);
	    column.setResizable(false);
	
	    // fill in table contents and refresh cache
	    refresh();

	    final TableEditor nameEditor = new TableEditor(table);
	    nameEditor.grabHorizontal = true;
	    final TableEditor parentEditor = new TableEditor(table);
	    parentEditor.grabHorizontal = true;
	    final TableEditor deleteEditor = new TableEditor(table);
	    deleteEditor.grabHorizontal = true;

	    table.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e){
	            // Clean up any previous editor control
	        	Control oldEditor = nameEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	            oldEditor = parentEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	            oldEditor = deleteEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	
	            // Identify the selected row
	            final TableItem titem = (TableItem)e.item;
	            final int ratingGroupId = ((Integer) titem.getData(ID)).intValue(); 
	            if (titem == null) return;
	            
	            // the editor for group name
	            Text name = new Text(table, SWT.FLAT);
	            name.setText(titem.getText(COL_NAME));
	            name.addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent arg0) {
	                    Text txt = (Text) nameEditor.getEditor();
	                    nameEditor.getItem().setText(COL_NAME, txt.getText());
	                    // save the change to the model
	                    try {
	                    	// update the rating group name 
	                    	RatingGroup tg = RatingGroup.load(ratingGroupId);
	                    	tg.setName(txt.getText());
	                    	tg.dispose();
	                    	refreshGroups();
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
					}
	            });
	            nameEditor.setEditor(name, titem, COL_NAME);
	
	            // the editor for parent group
	            CCombo combo = new CCombo(table, SWT.FLAT | SWT.READ_ONLY);
	            combo.setItems(groupNames);
	            combo.setText(titem.getText(COL_PARENT));
	            combo.addSelectionListener(new SelectionAdapter () {
	            	public void widgetSelected(SelectionEvent e) {
	                    CCombo cmb = (CCombo)parentEditor.getEditor();
	                    int sel = cmb.getSelectionIndex();
	                    if (sel == -1) {
	                    	return;
	                    }
	                    parentEditor.getItem().setText(COL_PARENT, cmb.getText());
	                    // save the change to the model
	                    try {
	                    	// TODO: check if selected group is allowed as parent group
	                    	RatingGroup tg = RatingGroup.load(ratingGroupId);
	                    	tg.setRatingGroupId(groupIds[sel]);
	                    	tg.dispose();
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	            	}
	            });
	            parentEditor.setEditor(combo, titem, COL_PARENT);
	
	            // the delete button
	            Button button = new Button(table, SWT.FLAT);
	            try {
	            	button.setText(Resources.getStr(this, "delete"));
	            } catch (ResourceException ex) {
	            	(new ExceptionWindow(getDisplay(), ex)).show();	            	
	            }
	            button.addListener(SWT.Selection, new Listener() {
	                public void handleEvent (Event e) {
	                    try {
		                    RatingGroup.delete(ratingGroupId);
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	                    table.remove(table.indexOf(titem));
	                    table.deselectAll();
	                    nameEditor.getEditor().dispose();
	                    parentEditor.getEditor().dispose();
	                    deleteEditor.getEditor().dispose();
	                }
	            });
	            deleteEditor.setEditor(button, titem, COL_DELETE);
	        }
	    });        
	
	    if (table.getItemCount() > 0) {
		    TableColumn[] cols = table.getColumns();
		    for (int i = 0; i < cols.length; i++) {
		        if (cols[i].getResizable()) {
		            cols[i].pack();
		        }
		    } 
	    }
	    
	    table.setSize(table.computeSize(SWT.DEFAULT, 200));
	    FormData data = new FormData();
	    data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - 
	    									  Settings.BUTTON_HEIGHT - 
											  Settings.ITEM_SPACING_V);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    table.setLayoutData(data);
	    
	    // add artist button
	    addGroupBtn = new Button(this, SWT.NONE);
	    addGroupBtn.setText(Resources.getStr(this, "addRatingGroup"));
        addGroupBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		RatingGroup tg = RatingGroup.create();
        	        TableItem item = new TableItem(table, SWT.NULL);
        	        item.setText(COL_NAME, tg.getName());          
        	        RatingGroup parent = tg.getRatingGroup();
        	        item.setText(COL_PARENT, parent == null ? Resources.getStr(this, "none") : parent.getName());
        	        if (parent != null) {
        	        	parent.dispose();
        	        }
        	        item.setData(ID, new Integer(tg.getId()));
        	        tg.dispose();
        	        refreshGroups();
        	        table.setSelection(new TableItem[] {item});
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
            	}
            }
        });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    addGroupBtn.setLayoutData(data);
	    
	    
	}

	/**
	 * Refresh the groups cache.
	 */
	protected void refreshGroups() throws MedleyException {
        // read the list of available artists
        String[] contents = null;
        Vector tgv = RatingGroup.getAllRatingGroups(); 
    	groupNames = new String[tgv.size() + 1];
    	groupIds = new int[tgv.size() + 1];
    	// add "none" option
    	groupNames[0] = Resources.getStr(this, "none");
    	groupIds[0] = 0;
    	
    	int ti = 1;
    	for (Iterator i = tgv.iterator(); i.hasNext();) {
    		RatingGroup tg = (RatingGroup) i.next();
    		groupNames[ti] = tg.getName();
    		groupIds[ti++] = tg.getId();
    	}
    	RatingGroup.disposeAll(tgv);
	}


	/**
	 * Refresh the contents of artists table.
	 * @throws MedleyException
	 */
	protected void refresh() throws MedleyException {
		table.removeAll();
		Vector groups = RatingGroup.getAllRatingGroups();
	    for (Iterator i = groups.iterator(); i.hasNext();) {
	        TableItem item = new TableItem (table, SWT.NULL);
	        RatingGroup tg = (RatingGroup) i.next();
	        item.setText(COL_NAME, tg.getName());
	        RatingGroup parent = tg.getRatingGroup();
	        item.setText(COL_PARENT, parent == null ? Resources.getStr(this, "none") : parent.getName());
	        if (parent != null) {
	        	parent.dispose();
	        }
	        item.setData(ID, new Integer(tg.getId()));
	    }
	    RatingGroup.disposeAll(groups);
	    refreshGroups();
	}
	

}
