/*
 * Created on 2004-01-04
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
import com.mmakowski.medley.data.events.DataObjectEvent;
import com.mmakowski.medley.data.events.DataObjectListener;
import com.mmakowski.medley.resources.Resources;

/**
 * The pane presenting the list of albums.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.15 $ $Date: 2005/08/19 18:05:19 $
 */
public class AlbumListPane extends ViewPane implements DataObjectListener {

	protected static final String ALBUM_ID = "id";
	protected static final int NO_ID = -1;
	
    /** the table */
    protected Table table;
    /** the display */
    protected Display display;
    
	/**
	 * Construct the album list pane.
	 */
    public AlbumListPane(Composite parent, int style) throws MedleyException {
        super(parent, style);
        display = Display.getCurrent();
        ListenerRegistry.registerListener(this, Album.class);
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
	                	AlbumWindow albumWin = AlbumWindow.createEditAlbumWindow(getShell(), true, id);
	        	    	c.dispose();
	        	    	c = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
	        	    	setCursor(c);
	                	albumWin.show();
	                	//refreshSelected();
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
	        			// delete selected album
	        			int id = getSelectedId();
	        			if (id != NO_ID) {
                            // TODO: according to preferences either delete or remove the album
	        				// ask if the user is sure to delete
	        				MessageBox mb = new MessageBox(getShell(), 
	        											   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    	    				mb.setText(Resources.getStr(this, "mb.deleteAlbum.title"));
    	    				mb.setMessage(Resources.getStr(this, "mb.deleteAlbum.msg"));
	        				if (mb.open() == SWT.YES) {
	        				    // TODO: ask for date
                                Album a = Album.load(id);
                                a.remove(null);
                                a.dispose();
                                //Album.delete(id);
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
    	String[] titles = {Resources.getStr(this, "artist"),
    					   Resources.getStr(this, "title"),
						   Resources.getStr(this, "year"),
						   Resources.getStr(this, "label")};
    	if (table.getColumnCount() == 0) {
	        for (int i=0; i<titles.length; i++) {
	            TableColumn column = new TableColumn (table, SWT.NULL);
	            column.setText (titles [i]);
	        }   
    	}
    	table.removeAll();
    	Vector albums = Album.getAllAlbums();
        for (Iterator i = albums.iterator(); i.hasNext();) {
            TableItem item = new TableItem (table, SWT.NULL);
            // item.setBackground(new Color(display, 127 + i, (100 + i) % 256, 30 + i));
            Album a = (Album) i.next();
            item.setData(ALBUM_ID, new Integer(a.getId()));
            setItemColumns(item, a);
        }
        Album.disposeAll(albums);
        for (int i=0; i<titles.length; i++) {
            table.getColumn (i).pack ();
        }   
    }

    /**
     * Refresh list item based on supplied album.
     * @param alb
     * @throws MedleyException
     */
    protected void refreshFromObject(Album alb) throws MedleyException {
    	int i = findIdIndex(alb.getId());
    	if (i >= 0) {
    		setItemColumns(table.getItem(i), alb);
			// TODO: reorder items if required
    	}
    }
    
    /**
     * Re-read the data for one row in the table. 
     * @param item the TableItem to be refreshed
     */
    protected void refreshItem(TableItem item) throws MedleyException {
    	int id = ((Integer) item.getData(ALBUM_ID)).intValue();
    	Album a = Album.load(id);
        setItemColumns(item, a);
        a.dispose();
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
	 * Fill in the columns of a TableItem with data from given Album.
	 * @param item the TableItem for which the columns should be set
	 * @param a the Album from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Album a) throws DataSourceException {
		// TODO: customizable columns
        item.setText(0, a.getMainArtistString());
		item.setText(1, a.getTitle());
        item.setText(2, String.valueOf(a.getOriginalReleaseYear()));
        item.setText(3, a.getLabel());
	}
	
	/**
	 * @return the album id of the selected table item
	 */
	protected int getSelectedId() {
    	TableItem[] sel = table.getSelection();
    	if (sel.length == 0) {
    		return NO_ID;
    	}
    	return ((Integer) sel[0].getData(ALBUM_ID)).intValue();
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#attributeChanged(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void attributeChanged(DataObjectEvent e) {
		// do nothing
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectCreated(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectCreated(DataObjectEvent e) {
		// insert new album into the list
		final Album a = (Album) e.getSource();
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
		        TableItem item = new TableItem (table, SWT.NULL);
		        item.setData(ALBUM_ID, new Integer(a.getId()));
				try {
			        setItemColumns(item, a);
		        } catch (MedleyException ex) {
		        	(new ExceptionWindow(getDisplay(), ex)).show();
		        }
		        table.select(table.indexOf(item));
		        // scroll to selected item
		        table.showSelection();
			}
		});	
	}

	/**
	 * @see com.mmakowski.medley.data.events.DataObjectListener#objectSaved(com.mmakowski.medley.data.events.DataObjectEvent)
	 */
	public void objectSaved(DataObjectEvent e) {
		final Album a = (Album) e.getSource();
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
				try {
					refreshFromObject(a);
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
	 * @param id album id
	 * @return index of table item containing a or -1 if a is not in the list
	 */
	protected int findIdIndex(int id) {
    	for (int i = 0; i < table.getItemCount(); i++) {
    		TableItem ti = table.getItem(i);
    		if (((Integer) ti.getData(ALBUM_ID)).intValue() == id) {
    			return i;
    		}
    	}
    	return -1;
	}
}
