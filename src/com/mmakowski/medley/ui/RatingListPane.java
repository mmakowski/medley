/*
 * Created on 23-Jan-2005
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
import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.Rating;
import com.mmakowski.medley.data.RatingGroup;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane containing list of available ratings and controls to
 * manipulate them.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/02/05 13:16:32 $
 */
public class RatingListPane extends Composite {

	protected static final String ID = "id";
	protected static final int NO_ID = -1;

	/** type of taggable to display tags for */
	protected int ratableType;
	/** list control */
    protected Table table;
	/** "new" button */
	protected Button newRatingBtn;
	/** "edit" button */
	protected Button editRatingBtn;
	/** "delete" button */
	protected Button deleteRatingBtn;
	
	/**
	 * @param parent parent control
	 * @param style control style
	 * @param type type of taggable to display tags for
	 */
	public RatingListPane(Composite parent, int style, int ratableType) throws MedleyException {
		super(parent, style);
		this.ratableType = ratableType;
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
        refresh();
        table.setSize(table.computeSize(SWT.DEFAULT, 400));
        FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - Settings.BUTTON_HEIGHT - Settings.ITEM_SPACING_V);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        table.setLayoutData(data);
        table.addMouseListener(new MouseAdapter() {
        	public void mouseDoubleClick(MouseEvent e) {
        		// show the edit album window
        		try {
        			editSelected();
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
	        		case SWT.DEL: deleteSelected(); break;
	        		}
        		} catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();
        		}
        	}
        });
        table.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		editRatingBtn.setEnabled(table.getSelectionCount() > 0);
        		deleteRatingBtn.setEnabled(table.getSelectionCount() > 0);
        	}
        });
        
        int left = Settings.MARGIN_LEFT;
        
	    // new tag button
	    newRatingBtn = new Button(this, SWT.NONE);
	    newRatingBtn.setText(Resources.getStr(this, "newRating"));
	    newRatingBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		RatingWindow ratingWin = RatingWindow.createNewRatingWindow(getShell(), true, ratableType);
	        		ratingWin.show();
	        		refresh();
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, left);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    newRatingBtn.setLayoutData(data);

	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;

	    // edit tag button
	    editRatingBtn = new Button(this, SWT.NONE);
	    editRatingBtn.setText(Resources.getStr(this, "editRating"));
	    editRatingBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		editSelected();
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, left);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    editRatingBtn.setLayoutData(data);
	    editRatingBtn.setEnabled(false);
	    
	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    
	    // delete tag button
	    deleteRatingBtn = new Button(this, SWT.NONE);
	    deleteRatingBtn.setText(Resources.getStr(this, "deleteRating"));
	    deleteRatingBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		deleteSelected();
	        	} catch (MedleyException ex) {
	        		(new ExceptionWindow(getDisplay(), ex)).show();
	        	}
	        }
	    });
	    data = new FormData();
	    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
	    data.left = new FormAttachment(0, left);
	    data.width = Settings.BUTTON_WIDTH;
	    data.height = Settings.BUTTON_HEIGHT;
	    deleteRatingBtn.setLayoutData(data);
	    deleteRatingBtn.setEnabled(false);
	}

	/**
	 * Refresh list contents.
	 *
	 */
	public void refresh() throws MedleyException {
        // if there's no data source active, don't do anything
    	if (!DataSource.isActive()) {
        	return;
        }
    	String[] titles = {Resources.getStr(this, "name"), 
    					   Resources.getStr(this, "group")};
    	if (table.getColumnCount() == 0) {
	        for (int i=0; i<titles.length; i++) {
	            TableColumn column = new TableColumn (table, SWT.NULL);
	            column.setText (titles[i]);
	        }   
    	}
    	table.removeAll();
    	Vector ratings = Rating.getAllRatings(ratableType);
        for (Iterator i = ratings.iterator(); i.hasNext();) {
            TableItem item = new TableItem (table, SWT.NULL);
            Rating r = (Rating) i.next();
            item.setData(ID, new Integer(r.getId()));
            setItemColumns(item, r);
        }
        Rating.disposeAll(ratings);
        for (int i=0; i<titles.length; i++) {
            table.getColumn (i).pack ();
        }   
	}
	
	/**
	 * @return the id of selected artist
	 */
	protected int getSelectedId() throws MedleyException {
    	TableItem[] sel = table.getSelection();
    	if (sel.length == 0) {
    		return NO_ID;
    	}
    	return ((Integer) sel[0].getData(ID)).intValue();
	}

	/**
	 * Delete selected tag.
	 * @throws MedleyException
	 */
	protected void deleteSelected() throws MedleyException {
		// delete selected rating
		int id = getSelectedId();
		if (id != NO_ID) {
			// ask if the user is sure to delete
			MessageBox mb = new MessageBox(getShell(), 
										   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			try {
				mb.setText(Resources.getStr(this, "mb.deleteRating.title"));
				mb.setMessage(Resources.getStr(this, "mb.deleteRating.msg"));
			} catch (ResourceException ex) {
            	(new ExceptionWindow(getDisplay(), ex)).show();
			}
			if (mb.open() == SWT.YES) {
				Rating.delete(id);
				int si = table.getSelectionIndex();
				table.remove(table.getSelectionIndex());
				table.setSelection((si >= table.getItemCount()) ? si - 1 : si);
			}
		}
	}

    /**
     * Re-read the data for one row in the table. 
     * @param item the TableItem to be refreshed
     */
    protected void refreshItem(TableItem item) throws MedleyException {
    	int id = ((Integer) item.getData(ID)).intValue();
    	Rating r = Rating.load(id);
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
	 * Fill in the columns of a TableItem with data from given Album.
	 * @param item the TableItem for which the columns should be set
	 * @param r the Rating from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Rating r) throws DataSourceException {
		item.setText(0, r.getName());
		RatingGroup rg = r.getRatingGroup();
		String group = "";
		if (rg != null) {
			group = rg.getName();
		}
        item.setText(1, group);
	}

	/**
	 * Show edit window for selected tag.
	 * @throws MedleyException
	 */
	protected void editSelected() throws MedleyException {
    	int id = getSelectedId();
    	if (id != NO_ID) {
    		RatingWindow ratingWin = RatingWindow.createEditRatingWindow(getShell(), true, id);
    		ratingWin.show();
    		refreshSelected();
    	}
	}

}
