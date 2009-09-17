/*
 * Created on 16-Oct-2004
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Album;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.DataObject;
import com.mmakowski.medley.data.Record;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * The pane allowing to view list of records for album.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.6 $ $Date: 2005/02/17 22:40:36 $
 */
public class RecordsPane extends Composite {
	protected static final String RECORD_ID = "id";
	protected static final int NO_ID = -1;
	
	protected static final int COL_NUMBER = 0;
	protected static final int COL_ARTIST = 1;
    protected static final int COL_TITLE = 2;
    protected static final int COL_DELETE = 3;
    
    /** the table */
    protected Table table;
    /** "new" button */
    protected Button newRecordBtn;
    /** "delete" button */
    protected Button deleteRecordBtn;
    /** "edit" button */
    protected Button editRecordBtn;
    /** are the values editable? */
    protected boolean editable;
    /** the album/record/track */
    protected Album album;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param item the item whose artists are presented
     */
    public RecordsPane(Composite parent, int style, 
    				   boolean editable, Album album) throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.album = album;
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
	 * Initialize the widgets in the pane.
	 */
	protected void initWidgets() throws MedleyException {
	    
	    // new record button
	    newRecordBtn = new Button(this, SWT.NONE);
	    newRecordBtn.setText(Resources.getStr(this, "newRecord"));
	    newRecordBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		RecordWindow recWin = RecordWindow.createNewRecordWindow(getShell(), true, album.getId());
	        		recWin.show();
	        		refresh();
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
	    FormData data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    newRecordBtn.setLayoutData(data);
	    
	    // edit record button
	    editRecordBtn = new Button(this, SWT.NONE);
	    editRecordBtn.setText(Resources.getStr(this, "editRecord"));
	    editRecordBtn.setEnabled(false);
	    editRecordBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
        		editSelected();
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT + Settings.BUTTON_WIDTH + 
	    								  Settings.ITEM_SPACING_H);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    editRecordBtn.setLayoutData(data);
	
	    // delete record button
	    deleteRecordBtn = new Button(this, SWT.NONE);
	    deleteRecordBtn.setText(Resources.getStr(this, "deleteRecord"));
	    deleteRecordBtn.setEnabled(false);
	    deleteRecordBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	deleteSelected();
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT + 2 * Settings.BUTTON_WIDTH + 
	    								  2 * Settings.ITEM_SPACING_H);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    deleteRecordBtn.setLayoutData(data);

		// records table
		table = new Table (this, SWT.BORDER | SWT.FULL_SELECTION);
	    table.setLinesVisible(false);
	    table.setHeaderVisible(true);
	    // add columns
	    TableColumn column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "number"));
	    column.setWidth(40);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "artist"));
	    column.setWidth(200);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "title"));
	    column.setWidth(200);
	
	    refresh();
	    
	    if (table.getItemCount() > 0) {
		    TableColumn[] cols = table.getColumns();
		    for (int i = 0; i < cols.length; i++) {
		        if (cols[i].getResizable()) {
		            cols[i].pack();
		        }
		    } 
	    }
	    
	    table.setSize(table.computeSize(SWT.DEFAULT, 200));
	    data = new FormData();
	    data.top = new FormAttachment(0, Settings.MARGIN_TOP);
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - 
	    									  Settings.BUTTON_HEIGHT - 
											  Settings.ITEM_SPACING_V);
	    data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
	    data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
	    table.setLayoutData(data);
	    table.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
    			editRecordBtn.setEnabled(e.item != null);
    			deleteRecordBtn.setEnabled(e.item != null);
	    	}
	    });
        table.addMouseListener(new MouseAdapter() {
        	public void mouseDoubleClick(MouseEvent e) {
    			editSelected();
        	}
        });
        table.addKeyListener(new KeyAdapter() {
        	public void keyReleased(KeyEvent e) {
        		switch (e.keyCode) {
        		case SWT.DEL:
        			deleteSelected();
        		}
        	}
        });
        
	}

	/**
	 * Refresh the list of records.
	 * @throws MedleyException
	 */
	protected void refresh() throws MedleyException {
		table.removeAll();
	    Vector albrecs = album.getRecords();
	    for (Iterator i = albrecs.iterator(); i.hasNext();) {
	        TableItem item = new TableItem (table, SWT.NULL);
	        Record r = (Record) i.next();
	        setItemColumns(item, r);
	    }
	    DataObject.disposeAll(albrecs);
	    editRecordBtn.setEnabled(false);
	    deleteRecordBtn.setEnabled(false);
	}

	/**
	 * @return the record id of the selected table item
	 */
	protected int getSelectedId() {
    	TableItem[] sel = table.getSelection();
    	if (sel.length == 0) {
    		return NO_ID;
    	}
    	return ((Integer) sel[0].getData(RECORD_ID)).intValue();
	}
	
    /**
     * Re-read the data for one row in the table. 
     * @param item the TableItem to be refreshed
     */
    protected void refreshItem(TableItem item) throws MedleyException {
    	int id = ((Integer) item.getData(RECORD_ID)).intValue();
    	Record r = Record.load(id);
        setItemColumns(item, r);
        r.dispose();
    }

    /**
     * Refresh all the selected items in the table.
     * @throws MedleyException
     */
    protected void refreshSelected() throws MedleyException {
    	TableItem[] sel = table.getSelection();
    	for (int i = 0; i < sel.length; i++) {
    		refreshItem(sel[i]);
    	}
    }
	
	/**
	 * Fill in the columns of a TableItem with data from given Record.
	 * @param item the TableItem for which the columns should be set
	 * @param r the Record from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Record r) throws DataSourceException {
        item.setText(COL_NUMBER, String.valueOf(r.getNumber()));          
        item.setText(COL_ARTIST, r.getMainArtistString());
        String title = r.getTitle();
        item.setText(COL_TITLE, title == null ? "" : title);        
        item.setData(RECORD_ID, new Integer(r.getId()));
	}

	/**
	 * Show record edit window for selected record.
	 */
	protected void editSelected() {
		try {
			int id = getSelectedId();
			if (id != NO_ID) {
				RecordWindow recWin = RecordWindow.createEditRecordWindow(getShell(), true, id);
				recWin.show();
				refreshSelected();
			}
        } catch (MedleyException ex) {
        	(new ExceptionWindow(getDisplay(), ex)).show();
        }
	}
	
	/**
	 * Delete selected record.
	 */
	protected void deleteSelected() {
		try {
			int id = getSelectedId();
			if (id != NO_ID) {
				// ask if the user is sure to delete
				MessageBox mb = new MessageBox(getShell(), 
											   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				try {
    				mb.setText(Resources.getStr(this, "mb.deleteRecord.title"));
    				mb.setMessage(Resources.getStr(this, "mb.deleteRecord.msg"));
				} catch (ResourceException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
				}
				if (mb.open() == SWT.YES) {
    				Record.delete(id);
    				int si = table.getSelectionIndex();
    				table.remove(table.getSelectionIndex());
    				table.setSelection((si >= table.getItemCount()) ? si - 1 : si);
				}
			}
        } catch (MedleyException ex) {
        	(new ExceptionWindow(getDisplay(), ex)).show();
        }
	}
}
