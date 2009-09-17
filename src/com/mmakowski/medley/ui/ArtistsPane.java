/*
 * Created on 2004-01-04
 */
package com.mmakowski.medley.ui;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo; 
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.TableItem;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Artist;
import com.mmakowski.medley.data.ArtistRole;
import com.mmakowski.medley.data.DataObject;
import com.mmakowski.medley.data.MusicalItem;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * The pane allowing to view and edit artist information for 
 * album, record or track.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.18 $ $Date: 2005/04/17 21:50:07 $
 */
class ArtistsPane extends Composite {
	protected static final String ARTIST_ROLE_ID = "id";
	
	protected static final int COL_MAIN = 0;
    protected static final int COL_ARTIST = 1;
    protected static final int COL_ROLE = 2;
    protected static final int COL_DELETE = 3;
    
    /** the table */
    protected Table table;
    /** import artists from records/tracks button */
    protected Button importUpBtn;
    /** import artists from album/record button */
    protected Button importDownBtn;
    /** "add artist" button */
    protected Button addArtistBtn;
    /** "new artist" button */
    protected Button newArtistBtn;
    /** are the values editable? */
    protected boolean editable;
    /** the album/record/track */
    protected MusicalItem item;
    /** the artist cache for combo boxes */
    protected String[] artists;
    /** the role cache for combo boxes */
    protected String[] roles;
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param item the item whose artists are presented
     */
    public ArtistsPane(Composite parent, int style, 
    				   boolean editable, MusicalItem item) throws MedleyException {
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
	    table = new Table (this, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
	    table.setLinesVisible(false);
	    table.setHeaderVisible(true);
	    // add columns
	    TableColumn column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "main"));
	    column.setWidth(Settings.CHECKBOX_COLUMN_WIDTH);
	    column.setResizable(false);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "artist"));
	    column.setWidth(160);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "role"));
	    column.setWidth(120);
	    column = new TableColumn (table, SWT.NULL);
	    column.setText(Resources.getStr(this, "actions"));
	    column.setWidth(Settings.ACTION_COLUMN_WIDTH);
	    column.setResizable(false);
	
	    // fill in table contents and refresh cache
	    refresh();

	    final TableEditor artistEditor = new TableEditor(table);
	    artistEditor.grabHorizontal = true;
	    final TableEditor roleEditor = new TableEditor(table);
	    roleEditor.grabHorizontal = true;
	    final TableEditor deleteEditor = new TableEditor(table);
	    deleteEditor.grabHorizontal = true;

	    table.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent e){
	            // Clean up any previous editor control
	        	Control oldEditor = artistEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	            oldEditor = roleEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	            oldEditor = deleteEditor.getEditor();
	            if (oldEditor != null) oldEditor.dispose();
	
	            // Identify the selected row
	            final TableItem titem = (TableItem)e.item;
	            if (titem == null) return;
	            final int artistRoleId = ((Integer) titem.getData(ARTIST_ROLE_ID)).intValue(); 
	            
	            // save the "main" state
                try {
                	// update the role
                	ArtistRole ar = ArtistRole.load(item.getType(), artistRoleId);
                	ar.setMain(titem.getChecked());
                	ar.dispose();
                } catch (MedleyException ex) {
                	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
                }
	            
	            // the editor for artists
	            CCombo combo = new CCombo(table, SWT.READ_ONLY | SWT.FLAT);
	            combo.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
	            combo.setItems(artists);
	            combo.setText(titem.getText(COL_ARTIST));
                combo.setVisibleItemCount(artists.length <= 10 ? 5 : 10);
	            combo.addFocusListener(new FocusAdapter() {
	            	public void focusLost(FocusEvent ev) {
	                    CCombo cmb = (CCombo)artistEditor.getEditor();
	                    artistEditor.getItem().setText(COL_ARTIST, cmb.getText());
	                    // save the change to the model
	                    try {
	                    	// find the artist id based on name
	                    	Artist a = Artist.load(cmb.getText());
	                    	int artistId = a.getId();
	                    	a.dispose();
	                    	// update the role
	                    	ArtistRole ar = ArtistRole.load(item.getType(), artistRoleId);
	                    	ar.setArtistId(artistId);
	                    	ar.dispose();
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	            		
	            	}
	            });
	            artistEditor.setEditor(combo, titem, COL_ARTIST);
	
	            // the editor for roles
	            combo = new CCombo(table, SWT.FLAT);
	            combo.setItems(roles);
	            combo.setText(titem.getText(COL_ROLE));
                combo.setVisibleItemCount(roles.length <= 10 ? 5 : 10);
	            combo.addFocusListener(new FocusAdapter() {
	            	public void focusLost(FocusEvent ev) {
	                    CCombo cmb = (CCombo)roleEditor.getEditor();
	                    roleEditor.getItem().setText(COL_ROLE, cmb.getText());
	                    // save the change to the model
	                    try {
	                    	ArtistRole ar = ArtistRole.load(item.getType(), artistRoleId);
	                    	ar.setRole(cmb.getText());
	                    	ar.dispose();
		                    refreshRoles();
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	            		
	            	}
	            });
	            roleEditor.setEditor(combo, titem, COL_ROLE);
	
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
		                    ArtistRole.delete(item.getType(),
 		                					  artistRoleId);
	                    } catch (MedleyException ex) {
	                    	(new ExceptionWindow(getDisplay(), ex)).show();	                    	
	                    }
	                    table.remove(table.indexOf(titem));
	                    table.deselectAll();
	                    artistEditor.getEditor().dispose();
	                    roleEditor.getEditor().dispose();
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
	    
	    // import down button
	    int left = Settings.MARGIN_LEFT;
	    importDownBtn = null;
	    final Composite parent = this;
	    if (item.getType() != MusicalItem.ALBUM) {
		    importDownBtn = new Button(this, SWT.NONE);
		    importDownBtn.setText(Resources.getStr(this, "importArtistsDown"));
	        importDownBtn.addListener(SWT.Selection, new Listener() {
	            public void handleEvent (Event e) {
	            	try {
	            		setCursor(SWT.CURSOR_WAIT);
	            		item.importArtistsDown();
	            		refresh();
	            		setCursor(SWT.CURSOR_ARROW);
	            	} catch (MedleyException ex) {
	            		setCursor(SWT.CURSOR_ARROW);
	                	(new ExceptionWindow(getDisplay(), ex)).show();
	            	}
	            }
	        });
		    data = new FormData();
		    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
		    data.left = new FormAttachment(0, left);
		    data.width = Settings.BUTTON_WIDTH;
		    data.height = Settings.BUTTON_HEIGHT;
		    importDownBtn.setLayoutData(data);
		    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    }
	    
	    // import up button
	    importUpBtn = null;
	    if (item.getType() != MusicalItem.TRACK) {
		    importUpBtn = new Button(this, SWT.NONE);
		    importUpBtn.setText(Resources.getStr(this, "importArtistsUp"));
	        importUpBtn.addListener(SWT.Selection, new Listener() {
	            public void handleEvent (Event e) {
	            	try {
	            		setCursor(SWT.CURSOR_WAIT);
	            		item.importArtistsUp();
	            		refresh();
	            		setCursor(SWT.CURSOR_ARROW);
	            	} catch (MedleyException ex) {
	            		setCursor(SWT.CURSOR_ARROW);
	                	(new ExceptionWindow(getDisplay(), ex)).show();
	            	}
	            }
	        });
		    data = new FormData();
		    data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
		    data.left = new FormAttachment(0, left);
		    data.width = Settings.BUTTON_WIDTH;
		    data.height = Settings.BUTTON_HEIGHT;
		    importUpBtn.setLayoutData(data);
		    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    }
	    
	    // add artist button
	    addArtistBtn = new Button(this, SWT.NONE);
	    addArtistBtn.setText(Resources.getStr(this, "addArtist"));
        addArtistBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	ArtistRole ar = null;
            	try {
            		ar = ArtistRole.create(item.getType(), item.getId());
            		// if it's the first artist, set it to main by default
            		if (item.getArtists().size() == 1) {
            			ar.setMain(true);
            		}
        	        TableItem item = new TableItem(table, SWT.NULL);
        	        item.setText(COL_ARTIST, ar.getArtistName());          
        	        item.setText(COL_ROLE, ar.getRole());        
        	        item.setChecked(ar.isMain());
        	        item.setData(ARTIST_ROLE_ID, new Integer(ar.getId()));
        	        ar.dispose();
        	        table.setSelection(new TableItem[] {item});
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
	    addArtistBtn.setLayoutData(data);
	    left += Settings.BUTTON_WIDTH + Settings.ITEM_SPACING_H;
	    
	    // new artist button
	    newArtistBtn = new Button(this, SWT.NONE);
	    newArtistBtn.setText(Resources.getStr(this, "newArtist"));
        newArtistBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
            	try {
                    ArtistWindow artistWin = new ArtistWindow(getShell(), true);
                	artistWin.show();
                	refreshArtists();
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
	    newArtistBtn.setLayoutData(data);
	}

	/**
	 * Refresh the artists cache.
	 */
	protected void refreshArtists() throws MedleyException {
        // read the list of available artists
        String[] contents = null;
        Vector av = Artist.getAllArtists(); 
    	artists = new String[av.size()];
    	int ai = 0;
    	for (Iterator i = av.iterator(); i.hasNext();) {
    		artists[ai++] = ((Artist) i.next()).getName();
    	}
    	Artist.disposeAll(av);
	}

	/**
	 * Set cursor
	 * @param style style of cursor (SWT constant)
	 */
	protected void setCursor(int style) {
		Cursor c = new Cursor(Display.getCurrent(), style);
		this.setCursor(c);
	}
	
	/**
	 * Refresh the roles cache.
	 */
	protected void refreshRoles() throws MedleyException {
        // read the list of available roles
        String[] contents = null;
        Vector rv = ArtistRole.getAllRoles(item.getType()); 
    	roles = (String[]) rv.toArray(new String[0]);
    	ArtistRole.disposeAll(rv);
	}

	/**
	 * Refresh the contents of artists table.
	 * @throws MedleyException
	 */
	protected void refresh() throws MedleyException {
		table.removeAll();
		Vector albarts = item.getArtists();
	    for (Iterator i = albarts.iterator(); i.hasNext();) {
	        TableItem item = new TableItem (table, SWT.NULL);
	        ArtistRole ar = (ArtistRole) i.next();
	        item.setText(COL_ARTIST, ar.getArtistName());          
	        item.setText(COL_ROLE, ar.getRole());        
	        item.setChecked(ar.isMain());
	        item.setData(ARTIST_ROLE_ID, new Integer(ar.getId()));
	    }
	    DataObject.disposeAll(albarts);
	    refreshArtists();
	    refreshRoles();
	}
}
