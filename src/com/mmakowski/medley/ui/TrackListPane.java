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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.mmakowski.events.ListenerRegistry;
import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Album;
import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.Record;
import com.mmakowski.medley.data.Track;
import com.mmakowski.medley.data.events.DataObjectEvent;
import com.mmakowski.medley.data.events.DataObjectListener;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A list presenting all tracks.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/04/10 20:55:46 $
 */
public class TrackListPane extends ViewPane implements DataObjectListener {
	// TODO: create common superclass for MusicalItem panes
	
	protected static final String TRACK_ID = "id";
	protected static final int NO_ID = -1;
	
    /** the table */
    protected Table table;
    /** the display */
    protected Display display;

	/**
	 * Construct the track list pane.
	 */
    public TrackListPane(Composite parent, int style) throws MedleyException {
        super(parent, style);
        display = Display.getCurrent();
        ListenerRegistry.registerListener(this, Track.class);
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
     * Initialize the widgets in the pane.
     */
    protected void initWidgets() throws MedleyException {
        table = new Table (this, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
        refresh();
        table.setSize(table.computeSize(SWT.DEFAULT, 400));
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        table.setLayoutData(data);
        table.addMouseListener(new MouseAdapter() {
        	public void mouseDoubleClick(MouseEvent e) {
        		// show the edit album window
        		try {
                	int id = getSelectedId();
                	if (id != NO_ID) {
            			Cursor c = new Cursor(getDisplay(), SWT.CURSOR_WAIT);
            			setCursor(c);
	                	TrackWindow trackWin = TrackWindow.createEditTrackWindow(getShell(), true, id);
	        	    	c.dispose();
	        	    	c = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
	        	    	setCursor(c);
	                	trackWin.show();
	                	refreshSelected();
                	}
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
        		
        	}
        });
        // add the keyboard shortcuts for table operations
        table.addKeyListener(new KeyAdapter() {
        	public void keyReleased(KeyEvent e) {
        		try {
	        		switch (e.keyCode) {
	        		case SWT.DEL:
	        			// delete selected track
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
	        		}
        		} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
        		}
        	}
        });
    }

    /**
     * Refresh the contents of the table.
     */
    public void refresh() throws MedleyException {
        // if there's no data source active, don't do anything
    	if (!DataSource.isActive()) {
        	return;
        }
    	String[] titles = {Resources.getStr(this, "title"),
    					   Resources.getStr(this, "artist"),	
						   Resources.getStr(this, "year"),
						   Resources.getStr(this, "album")
    				       };
    	if (table.getColumnCount() == 0) {
	        for (int i=0; i<titles.length; i++) {
	            TableColumn column = new TableColumn (table, SWT.NULL);
	            column.setText (titles [i]);
	        }   
    	}
    	table.removeAll();
    	Vector tracks = Track.getAllTracks();
        for (Iterator i = tracks.iterator(); i.hasNext();) {
            TableItem item = new TableItem (table, SWT.NULL);
            Track t = (Track) i.next();
            item.setData(TRACK_ID, new Integer(t.getId()));
            setItemColumns(item, t);
            t.dispose();
        }
        for (int i=0; i<titles.length; i++) {
            table.getColumn (i).pack ();
        }   
    }
    
    /**
     * Refresh list item based on supplied track.
     * @param trk
     * @throws MedleyException
     */
    protected void refreshFromObject(Track trk) throws MedleyException {
    	int i = findIdIndex(trk.getId());
    	if (i >= 0) {
    		setItemColumns(table.getItem(i), trk);
			// TODO: reorder items if required
    	}
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
	 * Fill in the columns of a TableItem with data from given Record.
	 * @param item the TableItem for which the columns should be set
	 * @param t the Track from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Track t) throws DataSourceException {
		// TODO: customizable columns
		Record r = t.getRecord();
		Album a = r.getAlbum();
		r.dispose();
		String tmp = t.getTitle();
        item.setText(0, tmp == null ? "" : tmp);
        item.setText(1, t.getMainArtistString());
        item.setText(2, String.valueOf(a.getOriginalReleaseYear()));
        item.setText(3, a.getTitle());
        a.dispose();
	}
	
	/**
	 * @return the record id of the selected table item
	 */
	protected int getSelectedId() {
    	TableItem[] sel = table.getSelection();
    	if (sel.length == 0) {
    		return NO_ID;
    	}
    	return ((Integer) sel[0].getData(TRACK_ID)).intValue();
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#attributeChanged(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void attributeChanged(DataObjectEvent e) {
		// do nothing (wait for save)
		
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectCreated(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectCreated(DataObjectEvent e) {
		// insert new track into the list
		final Track t = (Track) e.getSource();
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
		        TableItem item = new TableItem (table, SWT.NULL);
		        item.setData(TRACK_ID, new Integer(t.getId()));
				try {
			        setItemColumns(item, t);
		        } catch (MedleyException ex) {
		        	(new ExceptionWindow(getDisplay(), ex)).show();
		        }
			}
		});	
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectSaved(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectSaved(DataObjectEvent e) {
		final Track t = (Track) e.getSource();
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
				try {
					refreshFromObject(t);
		        } catch (MedleyException ex) {
		        	(new ExceptionWindow(getDisplay(), ex)).show();
		        }
			}
		});	
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectDeleted(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectDeleted(DataObjectEvent e) {
		final int id = e.getId();
		display.syncExec(new Runnable() {
			public void run() {
				int si = table.getSelectionIndex();
				int i = findIdIndex(id);
				if (i >= 0) {
					table.remove(i);
					if (si == i) {
						table.setSelection((si >= table.getItemCount()) ? si - 1 : si);
					}
				}
			}
		});	
	}

	/**
	 * @param id track id
	 * @return index of table item containing a or -1 if a is not in the list
	 */
	protected int findIdIndex(int id) {
    	for (int i = 0; i < table.getItemCount(); i++) {
    		TableItem ti = table.getItem(i);
    		if (((Integer) ti.getData(TRACK_ID)).intValue() == id) {
    			return i;
    		}
    	}
    	return -1;
	}
	
}
