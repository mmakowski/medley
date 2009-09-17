/*
 * Created on 09-Jan-2005
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Tag;
import com.mmakowski.medley.data.TagValue;
import com.mmakowski.medley.data.events.DataObjectEvent;
import com.mmakowski.medley.data.events.DataObjectListener;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane listing all values for enum tag.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/24 16:02:46 $
 */
public class TagValuesPane extends Composite implements DataObjectListener {
	protected static final String ID = "id";
	protected static final int NO_ID = -1;

	protected static final int COL_VALUE = 0;
	protected static final int COL_DELETE = 1;
	
	/** list control */
    protected Table table;
    /** "add group" button */
    protected Button addValueBtn;
	/** the parent window */
	protected TagWindow window;
    /** are the values editable? */
    protected boolean editable;
    /** the tag */
    protected Tag tag;
    /** the display for this pane */
    protected Display display;
	
    /**
     * Construct the pane.
     * @param window the parent window
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param tag the tag whose data is presented
     */
    public TagValuesPane(Composite parent, int style, 
    						TagWindow window, boolean editable, Tag tag) 
    		throws MedleyException {
        super(parent, style);
        this.display = Display.getCurrent();
        this.window = window;
        this.editable = editable;
        this.tag = tag;
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
        this.tag.addDataObjectListener(this);
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
	    column.setText(Resources.getStr(this, "value"));
	    column.setWidth(120);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "actions"));
	    column.setWidth(Settings.ACTION_COLUMN_WIDTH); 
	    column.setResizable(false);
	
	    // fill in table contents and refresh cache
	    refresh();

	    final TableEditor valueEditor = new TableEditor(table);
	    valueEditor.grabHorizontal = true;
	    final TableEditor deleteEditor = new TableEditor(table);
	    deleteEditor.grabHorizontal = true;

	    table.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e){
	            // Clean up any previous editor control
	        	Control oldEditor = valueEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	            oldEditor = deleteEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	
	            // Identify the selected row
	            final TableItem titem = (TableItem)e.item;
	            final int tagValueId = ((Integer) titem.getData(ID)).intValue(); 
	            if (titem == null) return;
	            
	            // the editor for group name
	            Text name = new Text(table, SWT.FLAT);
	            name.setText(titem.getText(COL_VALUE));
                name.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent ev) {
                        Text txt = (Text) valueEditor.getEditor();
                        valueEditor.getItem().setText(COL_VALUE, txt.getText());
                        // save the change to the model
                        try {
                            // update the tag group name 
                            TagValue tv = new TagValue(tagValueId);
                            tv.setValue(txt.getText());
                            tv.dispose();
                        } catch (MedleyException ex) {
                            (new ExceptionWindow(getDisplay(), ex)).show();                         
                        }
                    }
                });
	            valueEditor.setEditor(name, titem, COL_VALUE);
	
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
		                    TagValue.delete(tagValueId);
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	                    table.remove(table.indexOf(titem));
	                    table.deselectAll();
	                    valueEditor.getEditor().dispose();
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
	    
	    // add value button
	    addValueBtn = new Button(this, SWT.NONE);
	    addValueBtn.setText(Resources.getStr(this, "addTagValue"));
        addValueBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
            		TagValue tv = TagValue.create(tag.getId());
        	        TableItem item = new TableItem(table, SWT.NULL);
        	        item.setText(COL_VALUE, tv.getValue());          
        	        item.setData(ID, new Integer(tv.getId()));
        	        tv.dispose();
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
	    addValueBtn.setLayoutData(data);
	}

	/**
	 * Refresh the contents of values table.
	 * @throws MedleyException
	 */
	protected void refresh() throws MedleyException {
		table.removeAll();
		if (tag.getType() != Tag.TYPE_ENUM) {
			setEnabled(false);
		} else {
			setEnabled(true);
			Vector vals = TagValue.getAllForTag(tag.getId());
		    for (Iterator i = vals.iterator(); i.hasNext();) {
		        TableItem item = new TableItem (table, SWT.NULL);
		        TagValue tv = (TagValue) i.next();
		        item.setText(COL_VALUE, tv.getValue());
		        item.setData(ID, new Integer(tv.getId()));
		    }
		    TagValue.disposeAll(vals);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#attributeChanged(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void attributeChanged(DataObjectEvent e) {
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
				try {
					refresh();
		        } catch (MedleyException ex) {
		        	(new ExceptionWindow(getDisplay(), ex)).show();	            	
		        }
			}
		});
	}
	
	/**
	 * Enable/disable this pane.
	 */
	public void setEnabled(boolean enable) {
		if (table != null) {
			table.setEnabled(enable);
		}
		if (addValueBtn != null) {
			addValueBtn.setEnabled(enable);
		}
		super.setEnabled(enable);
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectCreated(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectCreated(DataObjectEvent e) {
		// do nothing
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectSaved(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectSaved(DataObjectEvent e) {
		// do nothing
		
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectDeleted(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectDeleted(DataObjectEvent e) {
		// do nothing
		
	}
	
}
