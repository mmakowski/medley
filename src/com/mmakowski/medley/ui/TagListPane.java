/*
 * Created on 06-Jan-2005
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
import com.mmakowski.medley.data.Tag;
import com.mmakowski.medley.data.TagGroup;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * A pane containing list of available tags and controls to
 * manipulate them.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/02/05 13:16:30 $
 */
public class TagListPane extends Composite {

	protected static final String ID = "id";
	protected static final int NO_ID = -1;

	/** type of taggable to display tags for */
	protected int taggableType;
	/** list control */
    protected Table table;
	/** "new" button */
	protected Button newTagBtn;
	/** "edit" button */
	protected Button editTagBtn;
	/** "delete" button */
	protected Button deleteTagBtn;
	
	/**
	 * @param parent parent control
	 * @param style control style
	 * @param type type of taggable to display tags for
	 */
	public TagListPane(Composite parent, int style, int taggableType) throws MedleyException {
		super(parent, style);
		this.taggableType = taggableType;
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
        		editTagBtn.setEnabled(table.getSelectionCount() > 0);
        		deleteTagBtn.setEnabled(table.getSelectionCount() > 0);
        	}
        });
        
        int left = Settings.MARGIN_LEFT;
        
	    // new tag button
	    newTagBtn = new Button(this, SWT.NONE);
	    newTagBtn.setText(Resources.getStr(this, "newTag"));
	    newTagBtn.addListener(SWT.Selection, new Listener() {
	        public void handleEvent (Event e) {
	        	try {
	        		TagWindow tagWin = TagWindow.createNewTagWindow(getShell(), true, taggableType);
	        		tagWin.show();
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
	    newTagBtn.setLayoutData(data);

	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;

	    // edit tag button
	    editTagBtn = new Button(this, SWT.NONE);
	    editTagBtn.setText(Resources.getStr(this, "editTag"));
	    editTagBtn.addListener(SWT.Selection, new Listener() {
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
	    editTagBtn.setLayoutData(data);
	    editTagBtn.setEnabled(false);
	    
	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    
	    // delete tag button
	    deleteTagBtn = new Button(this, SWT.NONE);
	    deleteTagBtn.setText(Resources.getStr(this, "deleteTag"));
	    deleteTagBtn.addListener(SWT.Selection, new Listener() {
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
	    deleteTagBtn.setLayoutData(data);
	    deleteTagBtn.setEnabled(false);
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
    	Vector tags = Tag.getAllTags(taggableType);
        for (Iterator i = tags.iterator(); i.hasNext();) {
            TableItem item = new TableItem (table, SWT.NULL);
            Tag t = (Tag) i.next();
            item.setData(ID, new Integer(t.getId()));
            setItemColumns(item, t);
        }
        Tag.disposeAll(tags);
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
		// delete selected tag
		int id = getSelectedId();
		if (id != NO_ID) {
			// ask if the user is sure to delete
			MessageBox mb = new MessageBox(getShell(), 
										   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			try {
				mb.setText(Resources.getStr(this, "mb.deleteTag.title"));
				mb.setMessage(Resources.getStr(this, "mb.deleteTag.msg"));
			} catch (ResourceException ex) {
            	(new ExceptionWindow(getDisplay(), ex)).show();
			}
			if (mb.open() == SWT.YES) {
				Tag.delete(id);
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
    	Tag t = Tag.load(id);
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
	 * Fill in the columns of a TableItem with data from given Album.
	 * @param item the TableItem for which the columns should be set
	 * @param a the Album from which data is obtained
	 * @throws DataSourceException
	 */
	protected void setItemColumns(TableItem item, Tag t) throws DataSourceException {
		item.setText(0, t.getName());
		TagGroup tg = t.getTagGroup();
		String group = "";
		if (tg != null) {
			group = tg.getName();
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
    		TagWindow tagWin = TagWindow.createEditTagWindow(getShell(), true, id);
    		tagWin.show();
    		refreshSelected();
    	}
	}
}
