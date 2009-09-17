/*
 * Created on 2004-08-07
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Artist;
import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * The pane with list of all artists in the database.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $  $Date: 2005/02/05 13:16:31 $
 */
public class ArtistListPane extends ViewPane {
	
	protected static final String ARTIST_ID = "id";
	protected static final int NO_ID = -1;
	
    /** the list */
    protected List list;
    /** the list of albums */
    // TODO: more flexible view in the right pane
    protected List albumList;

	/**
	 * Construct the album list pane.
	 */
    public ArtistListPane(Composite parent, int style) throws MedleyException {
        super(parent, style);    
        setLayout(new FillLayout());
        initWidgets();
    }

    /**
     * Initialize the widgets in the pane.
     */
    protected void initWidgets() throws MedleyException {
        SashForm contents = new SashForm(this, getStyle() | SWT.HORIZONTAL);
        contents.setLayout(new FillLayout());
        
    	list = new List (contents, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        refresh();
        list.setSize(list.computeSize(SWT.DEFAULT, 400));
        list.addMouseListener(new MouseAdapter() {
        	public void mouseDoubleClick(MouseEvent e) {
        		// show the edit album window
        		try {
                	int id = getSelectedId();
                	if (id != NO_ID) {
            			Cursor c = new Cursor(getDisplay(), SWT.CURSOR_WAIT);
            			setCursor(c);
	                	ArtistWindow artistWin = new ArtistWindow(getShell(), true, id);
	        	    	c.dispose();
	        	    	c = new Cursor(getDisplay(), SWT.CURSOR_ARROW);
	        	    	setCursor(c);
	                	artistWin.show();
	                	refreshSelected();
                	}
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
                }
        		
        	}
        });
        // add the keyboard shortcuts for table operations
        list.addKeyListener(new KeyAdapter() {
        	public void keyReleased(KeyEvent e) {
        		try {
	        		switch (e.keyCode) {
	        		case SWT.DEL:
	        			// delete selected album
	        			int id = getSelectedId();
	        			if (id != NO_ID) {
	        				// ask if the user is sure to delete
	        				MessageBox mb = new MessageBox(getShell(), 
	        											   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
	        				try {
	    	    				mb.setText(Resources.getStr(this, "mb.deleteArtist.title"));
	    	    				mb.setMessage(Resources.getStr(this, "mb.deleteArtist.msg"));
	        				} catch (ResourceException ex) {
	                        	(new ExceptionWindow(getDisplay(), ex)).show();
	        				}
	        				if (mb.open() == SWT.YES) {
		        				Artist.delete(id);
		        				int si = list.getSelectionIndex();
		        				list.remove(si);
		        				list.setSelection((si >= list.getItemCount()) ? si - 1 : si);
	        				}
	        			}
	        		}
        		} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
        		}
        	}
        });
        list.addSelectionListener(new SelectionAdapter () {
        	// TODO: fill in the album/record/track list 
        });
        
        // the album list
        albumList = new List(contents, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
        albumList.setSize(list.computeSize(SWT.DEFAULT, 400));
        
        contents.setWeights(new int[] {50, 50});
    }

    /**
     * Refresh the contents of the table.
     */
    public void refresh() throws MedleyException {
        // if there's no data source active, don't do anything
    	if (!DataSource.isActive()) {
        	return;
        }

    	int sel = list.getSelectionIndex();
    	list.removeAll();
        Vector artists = Artist.getAllArtists();
        for (Iterator i = artists.iterator(); i.hasNext();) {
            Artist a = (Artist) i.next();
            list.add(a.getName());
            a.dispose();
        }
        list.setSelection(new int[] {sel});
    }
    
    /**
     * Refresh all the selected items in the table.
     * @throws MedleyException
     */
    protected void refreshSelected() throws MedleyException {
    	refresh();
    }
    
	
	/**
	 * @return the id of selected artist
	 */
	protected int getSelectedId() throws MedleyException {
		if (list.getSelectionCount() == 0) {
			return NO_ID;
		}
		Artist a = Artist.load(list.getSelection()[0]);
		int id = a.getId();
		a.dispose();
		return id;
	}

	
}
