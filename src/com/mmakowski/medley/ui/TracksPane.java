/*
 * Created on 01-Jan-2005
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
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.DataObject;
import com.mmakowski.medley.data.Record;
import com.mmakowski.medley.data.Track;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane allowing to view a list of tracks for a record.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/02/17 22:40:37 $
 */
public class TracksPane extends Composite {
	protected static final String TRACK_ID = "id";
	protected static final int NO_ID = -1;
	
	protected static final int COL_NUMBER = 0;
	protected static final int COL_ARTIST = 1;
    protected static final int COL_TITLE = 2;
    protected static final int COL_DELETE = 3;
    
    /** the table */
    protected Table table;
    /** "new" button */
    protected Button newTrackBtn;
    /** "delete" button */
    protected Button deleteTrackBtn;
    /** "edit" button */
    protected Button editTrackBtn;
    /** are the values editable? */
    protected boolean editable;
    /** the album/record/track */
    protected Record record;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param record the record whose tracks are presented
     */
    public TracksPane(Composite parent, int style, 
    				  boolean editable, Record record) throws MedleyException {
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
	    
	    // new record button
	    newTrackBtn = new Button(this, SWT.NONE);
	    newTrackBtn.setText(Resources.getStr(this, "newTrack"));
	    newTrackBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		TrackWindow trkWin = TrackWindow.createNewTrackWindow(getShell(), true, record.getId());
	        		trkWin.show();
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
	    newTrackBtn.setLayoutData(data);
	    
	    // edit record button
	    editTrackBtn = new Button(this, SWT.NONE);
	    editTrackBtn.setText(Resources.getStr(this, "editTrack"));
	    editTrackBtn.setEnabled(false);
	    editTrackBtn.addListener(SWT.Selection, new Listener() {
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
	    editTrackBtn.setLayoutData(data);
	
	    // delete record button
	    deleteTrackBtn = new Button(this, SWT.NONE);
	    deleteTrackBtn.setText(Resources.getStr(this, "deleteTrack"));
	    deleteTrackBtn.setEnabled(false);
	    deleteTrackBtn.addListener(SWT.Selection, new Listener() {
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
	    deleteTrackBtn.setLayoutData(data);

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
    			editTrackBtn.setEnabled(e.item != null);
    			deleteTrackBtn.setEnabled(e.item != null);
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
	 * Refresh the list of tracks.
	 * @throws MedleyException
	 */
	protected void refresh() throws MedleyException {
		table.removeAll();
	    Vector rectrks = record.getTracks();
	    for (Iterator i = rectrks.iterator(); i.hasNext();) {
	        TableItem item = new TableItem (table, SWT.NULL);
	        Track t = (Track) i.next();
	        setItemColumns(item, t);
	    }
	    DataObject.disposeAll(rectrks);
	    editTrackBtn.setEnabled(false);
	    deleteTrackBtn.setEnabled(false);
	}

	/**
	 * @return the track id of the selected table item
	 */
	protected int getSelectedId() {
    	TableItem[] sel = table.getSelection();
    	if (sel.length == 0) {
    		return NO_ID;
    	}
    	return ((Integer) sel[0].getData(TRACK_ID)).intValue();
	}
	
    /**
     * Re-read the data for one row in the table. 
     * @param item the TableItem to be refreshed
     */
    protected void refreshItem(TableItem item) throws MedleyException {
    	int id = ((Integer) item.getData(TRACK_ID)).intValue();
    	Track t = Track.load(id);
        setItemColumns(item, t);
        t.dispose();
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
	 * Fill in the columns of a TableItem with data from given Track.
	 * @param item the TableItem for which the columns should be set
	 * @param t the Track from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Track t) throws DataSourceException {
        item.setText(COL_NUMBER, String.valueOf(t.getNumber()));   
        item.setText(COL_ARTIST, t.getMainArtistString());
        String title = t.getTitle();
        item.setText(COL_TITLE, title == null ? "" : title);        
        item.setData(TRACK_ID, new Integer(t.getId()));
	}

	/**
	 * Show record edit window for selected track.
	 */
	protected void editSelected() {
		try {
			int id = getSelectedId();
			if (id != NO_ID) {
				TrackWindow trkWin = TrackWindow.createEditTrackWindow(getShell(), true, id);
				trkWin.show();
				refreshSelected();
			}
        } catch (MedleyException ex) {
        	(new ExceptionWindow(getDisplay(), ex)).show();
        }
	}
	
	/**
	 * Delete selected track.
	 */
	protected void deleteSelected() {
		try {
			int id = getSelectedId();
			if (id != NO_ID) {
				// ask if the user is sure to delete
				MessageBox mb = new MessageBox(getShell(), 
											   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				try {
    				mb.setText(Resources.getStr(this, "mb.deleteTrack.title"));
    				mb.setMessage(Resources.getStr(this, "mb.deleteTrack.msg"));
				} catch (ResourceException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
				}
				if (mb.open() == SWT.YES) {
    				Track.delete(id);
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
