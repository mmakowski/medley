/*
 * Created on 2003-12-25
 */
package com.mmakowski.medley.ui;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.ToolBar;
//import org.eclipse.swt.widgets.ToolItem;

import com.mmakowski.events.ProgressEvent;
import com.mmakowski.events.ProgressListener;
import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.DataSource;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.FileConverter;
import com.mmakowski.medley.data.events.DataSourceEvent;
import com.mmakowski.medley.data.events.DataSourceListener;
import com.mmakowski.medley.resources.Errors;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.preferences.AppearancePane;
import com.mmakowski.swt.windows.PreferencesWindow;

/**
 * The main window of the Medley application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.32 $ $Date: 2005/05/22 00:07:38 $
 */
public class MainWindow implements DataSourceListener, ProgressListener {
	// preferences paths
    public static final String PREF_WINDOW_SIZE_X = "/main_window/size/x";
    public static final String PREF_WINDOW_SIZE_Y = "/main_window/size/y";
    public static final String PREF_WINDOW_POSITION_X = "/main_window/location/x";
    public static final String PREF_WINDOW_POSITION_Y = "/main_window/location/y";
    public static final String PREF_WINDOW_MAXIMIZED = "/main_window/maximized";
	public static final String PREF_RESTORE_POSITION = "/main_window/restore_position";
	public static final String PREF_VIEW_TABS_ENHANCED = "/main_window/view_tabs/enhanced";
    public static final String PREF_VIEW_TABS_STYLE = "/main_window/view_tabs/style";
    public static final String PREF_VIEW_TABS_SIZE = "/main_window/view_tabs/size";
    
    // available view tab styles
    public static final int VIEW_TAB_TEXT = 0;
    public static final int VIEW_TAB_ICON = 1;
    public static final int VIEW_TAB_TEXT_ICON = 2;
	
	/** data key for id of menu */
	public static final String MENU_ID = "menu_id";
    /** id for file menu */
	public static final String MENU_FILE = "file";
    /** id for edit menu */
    public static final String MENU_EDIT = "edit";
    /** id for view menu */
    public static final String MENU_VIEW = "view";
    /** id for help menu */
    public static final String MENU_HELP = "help";
	/** data key for view */
    protected static final String VIEW = "view";
    
    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
    /** logger */
    private static Logger log = Logger.getLogger(MainWindow.class.getName());
    
    /** the display managing the window */
    protected Display display;
    /** an SWT Shell of the main window */
    protected Shell shell;
    /** the list of views */
    protected Vector views;
    /** active view */
    protected View activeView;
    /** the tool bar area */
    protected CoolBar toolBar;
    /** the menu bar */
    protected Menu menuBar;
    /** folder for view tabs */
    protected CTabFolder tabFolder;
    /** the progress indicator pane */
    protected ProgressIndicator progress;
    /** mapping from view to index of tab inf folder */
    protected Hashtable viewTabMap;
  
    
    /**
     * Construct the window.
     * @param display
     * @param fileName
     */
    public MainWindow(Display display, String fileName) throws MedleyException {
        this.display = display;
        shell = new Shell(display);
        shell.setText(Resources.getStr(this, "title"));
        // set window icon
        shell.setImages(new Image[] {new Image(display, "img/icon-medley-16.gif"),
        							 new Image(display, "img/icon-medley-32.gif")});
                
        // when the window is about to be closed ask the user
        // whether to save the data.
        final Display disp = display;
        shell.addShellListener(new ShellAdapter() {
        	public void shellClosed(ShellEvent e) {
        		DataSource ds = DataSource.get();
            	try {
            		e.doit = continueClosing();
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(disp, ex)).show();
            	}
        	}
        });
        
        FormLayout layout = new FormLayout();
        shell.setLayout(layout);
        initMenu();
        initViews();
        initWidgets();   
        if (fileName == null) {
            newFile();
        } else {
            log.fine("opening file " + fileName);
            openFile(fileName);
        }
     
        // ensure the window is refreshed when preferences change
        prefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            public void preferenceChange(PreferenceChangeEvent ev) {
                // check that a relevant preference has changed
                if (ev.getKey().equals(PREF_VIEW_TABS_ENHANCED) ||
                    ev.getKey().equals(PREF_VIEW_TABS_SIZE) ||
                    ev.getKey().equals(PREF_VIEW_TABS_STYLE)) {
                    // redraw window
                    try {
                        refreshCustomisableElements();
                    } catch (MedleyException ex) {
                        (new ExceptionWindow(disp, ex)).show();
                    }
                }
            }
        });
    }
    
    /**
     * Show the window and return when it's closed.
     */
    public void show() {
    	if (prefs.getBoolean(PREF_RESTORE_POSITION, true)) {
	    	shell.setSize(new Point(prefs.getInt(PREF_WINDOW_SIZE_X, 640), prefs.getInt(PREF_WINDOW_SIZE_Y, 480)));
	    	shell.setLocation(new Point(prefs.getInt(PREF_WINDOW_POSITION_X, 10), prefs.getInt(PREF_WINDOW_POSITION_Y, 10)));
	    	shell.setMaximized(prefs.getBoolean(PREF_WINDOW_MAXIMIZED, false));
    	} else {
    		shell.setSize(640, 480);
    	}
    	// add listeners that save window position and state
    	shell.addControlListener(new ControlAdapter() {
        	public void controlMoved(ControlEvent e) {
        		if (shell.getMaximized()) {
        			prefs.putBoolean(PREF_WINDOW_MAXIMIZED, true);
        		} else {
        			prefs.putBoolean(PREF_WINDOW_MAXIMIZED, false);
        			Point loc = shell.getLocation();
                    prefs.putInt(PREF_WINDOW_POSITION_X, loc.x);
                    prefs.putInt(PREF_WINDOW_POSITION_Y, loc.y);
        		}
        	}
        	public void controlResized(ControlEvent e) {
        		if (shell.getMaximized()) {
        			prefs.putBoolean(PREF_WINDOW_MAXIMIZED, true);
        		} else {
        			prefs.putBoolean(PREF_WINDOW_MAXIMIZED, false);
                    Point size = shell.getSize();
        			prefs.putInt(PREF_WINDOW_SIZE_X, size.x);
                    prefs.putInt(PREF_WINDOW_SIZE_Y, size.y);
        		}
        	}
        });
    	shell.open ();
        while (!shell.isDisposed ()) {
            if (!display.readAndDispatch ()) display.sleep ();
        }
    }
    
    /**
     * Initialise the list of available views.
     * @throws MedleyException
     */
    protected void initViews() throws MedleyException {
    	views = new Vector();
    	// TODO: create views based on user preferences
    	views.add(new HomeView(this));
    	views.add(new AlbumsView(this));
    	views.add(new RecordsView(this));
    	views.add(new TracksView(this));
    	views.add(new ArtistsView(this));
    	// TODO: other views
    	
    	activeView = (View) views.firstElement();
    	// TODO: set default view based on user preferences
    }
    
    /**
     * Create the menu entries.
     */
    protected void initMenu() throws ResourceException {
        menuBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);

        // file menu
        MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(Resources.getStr(this, "menu.file"));
        item.setData(MENU_ID, MENU_FILE);
        Menu submenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(submenu);
        // -- new
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
            	try {
            		newFile();
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
            	}
            }
        });
        item.setText(Resources.getStr(this, "menu.file.new"));
        // -- open
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
            	try {
            		openFile();
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
            	}
            }
        });
        item.setText(Resources.getStr(this, "menu.file.open"));
        item.setAccelerator(SWT.CTRL + 'O');
        // -- save
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
            	try {
            		saveFile();
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
            	}
            }
        });
        item.setText(Resources.getStr(this, "menu.file.save"));
        item.setAccelerator(SWT.CTRL + 'S');
        // -- save as
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
            	try {
            		saveFileAs();
            	} catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
            	}
            }
        });
        item.setText(Resources.getStr(this, "menu.file.saveAs"));
        // -- separator
        item = new MenuItem(submenu, SWT.SEPARATOR);
        // -- exit
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
                shell.close();
            }
        });
        item.setText(Resources.getStr(this, "menu.file.exit"));

        // edit menu
        item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(Resources.getStr(this, "menu.edit"));
        item.setData(MENU_ID, MENU_EDIT);
        submenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(submenu);
        
        // view menu
        item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(Resources.getStr(this, "menu.view"));
        item.setData(MENU_ID, MENU_VIEW);
        submenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(submenu);
        // -- preferences
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
                try {
                    showPreferencesWindow();
                } catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
                }
            }
        });
        item.setText(Resources.getStr(this, "menu.view.preferences"));
        // -- separator
        item = new MenuItem(submenu, SWT.SEPARATOR);
        // views will go here
        
        // help menu
        item = new MenuItem(menuBar, SWT.CASCADE);
        item.setText(Resources.getStr(this, "menu.help"));
        item.setData(MENU_ID, MENU_HELP);
        submenu = new Menu(shell, SWT.DROP_DOWN);
        item.setMenu(submenu);
        // -- about
        item = new MenuItem(submenu, SWT.PUSH);
        item.addListener(SWT.Selection, new Listener () {
            public void handleEvent(Event e) {
                try {
	                AboutWindow aboutWin = new AboutWindow(shell);
	            	aboutWin.show();
                } catch (MedleyException ex) {
                	(new ExceptionWindow(display, ex)).show();
                }
            }
        });
        item.setText(Resources.getStr(this, "menu.help.about"));
    }
    
    /**
	 * Display preferences window.
	 */
	protected void showPreferencesWindow() throws MedleyException {
		PreferencesWindow prefsWindow = null;
		try {
			prefsWindow = new PreferencesWindow(shell);
			prefsWindow.addPane(new AppearancePane(shell, SWT.NONE, this));
            prefsWindow.addPane(new com.mmakowski.medley.ui.preferences.AuditionsPane(shell, SWT.NONE));
			prefsWindow.addPane(new com.mmakowski.medley.ui.preferences.RatingsPane(shell, SWT.NONE));
			// TODO: add further panes
			prefsWindow.initWindow();
		} catch (Exception ex) {
			throw new UIException(Errors.ERROR_INITIALIZING_PREFS_WINDOW, ex);
		}
		
		prefsWindow.show();
	}

	/**
	 * Reset elements that can be customised by user.
	 * @throws MedleyException
	 */
	private void refreshCustomisableElements() throws MedleyException {
	    // this might be executed from another thread so make sure
        // the UI manipulation is done in UI thread
        display.syncExec(new Runnable() {
            public void run() {
        		try {
                    tabFolder.setSimple(!prefs.getBoolean(PREF_VIEW_TABS_ENHANCED, true));
            		// set icon/text based and icon size on user preferences
            		int style = prefs.getInt(PREF_VIEW_TABS_STYLE, VIEW_TAB_TEXT_ICON);
            		int tabSize = prefs.getInt(PREF_VIEW_TABS_SIZE, Settings.VIEWTAB_HEIGHT_SMALL);
            		int iconSize = (tabSize == Settings.VIEWTAB_HEIGHT_NORMAL) ? Settings.ICON_NORMAL : Settings.ICON_SMALL; 
            		tabFolder.setTabHeight(tabSize);
            		tabFolder.redraw();
            		CTabItem[] tabs = tabFolder.getItems();
            		for (int i = 0; i < tabs.length; i++) {
            			View v = (View) tabs[i].getData(VIEW);
            			switch (style) {
            			case VIEW_TAB_TEXT:
            				tabs[i].setText(v.getTitle());
            				tabs[i].setImage(null);
            				break;
            			case VIEW_TAB_ICON:
            				tabs[i].setText("");
            				tabs[i].setImage(new Image(shell.getDisplay(), v.getIconPath(iconSize)));
            				break;
            			default:
            				tabs[i].setText(v.getTitle());
            				tabs[i].setImage(new Image(shell.getDisplay(), v.getIconPath(iconSize)));
            			}
            		}
            		// Dirty hack to make tabs contents redraw properly
            		int i = tabFolder.getSelectionIndex();
            		int j = i + 1 % tabFolder.getItemCount();
            		tabFolder.setSelection(j);
            		tabFolder.setSelection(i);
                } catch (MedleyException ex) {
                    (new ExceptionWindow(display, ex)).show();
                }
            }
        });
    }
	
	/**
     * Initialize the widgets in the window.
     */
    protected void initWidgets() throws MedleyException {
    	FormData data;
    	/*
        // construct the toolbar
        toolBar = new CoolBar(shell, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.bottom = new FormAttachment(0, TestSettings.TOOLBAR_HEIGHT);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        toolBar.setLayoutData(data);
        
        // TODO: main bar
        //CoolItem mainItem = new CoolItem(coolBar, SWT.PUSH);
        ToolBar mainBar = new ToolBar(toolBar, SWT.FLAT);
        //mainItem.setControl(mainBar);
        
        ToolItem newBtn = new ToolItem(mainBar, SWT.PUSH);
        newBtn.setToolTipText(Resources.getStr(this, "tb.new"));
        newBtn.setImage(new Image(shell.getDisplay(), "img/toolbarBtn-new.gif"));
        ToolItem openBtn = new ToolItem(mainBar, SWT.PUSH);
        openBtn.setToolTipText(Resources.getStr(this, "tb.open"));
        openBtn.setImage(new Image(shell.getDisplay(), "img/toolbarBtn-open.gif"));
        ToolItem saveBtn = new ToolItem(mainBar, SWT.PUSH);
        saveBtn.setToolTipText(Resources.getStr(this, "tb.save"));
        saveBtn.setImage(new Image(shell.getDisplay(), "img/toolbarBtn-save.gif"));
        mainBar.pack();
        */        
        //mainItem = new CoolItem(coolBar, SWT.NONE);
        
        // create view folder
		tabFolder = new CTabFolder(shell, SWT.BORDER /*| SWT.SINGLE*/);
		tabFolder.setSelectionBackground(new Color[] {Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT), Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)}, new int[] {50, 100});
		tabFolder.setSelectionForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
		//tabFolder.setSelectionBackground(new Color[] {Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND), Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT), Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND)}, new int[] {50, 100}, true);
		data = new FormData();
        data.top = new FormAttachment(0, 0/*TestSettings.TOOLBAR_HEIGHT*/);
        data.bottom = new FormAttachment(100, -Settings.STATUSBAR_HEIGHT - Settings.STATUSBAR_SPACING_V);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
		tabFolder.setLayoutData(data);

		viewTabMap = new Hashtable();
		// create view tabs
		for (Iterator i = views.iterator(); i.hasNext();) {
			View v = (View) i.next();
			CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
			tab.setData(VIEW, v);
			tab.setToolTipText(v.getTooltip());
			// create the view's pane -- we assume here that it has not been created yet
			tab.setControl(v.createPane(tabFolder));
			/*
			if (v == activeView) {
				tabFolder.setSelection(tab);
			}
			*/
			viewTabMap.put(v, tab);
			// set the data field of tab to point to view associated with it
			tab.setData(v);
			if (v == activeView) {
				v.activate();
			}
		}
		
		// add event handler for view switching
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				View v = (View) e.item.getData();
				switchViewTo(v);
			}
		});
		
		// TODO: status bar
		progress = new ProgressIndicator(shell, SWT.NONE);
		data = new FormData();
        data.top = new FormAttachment(100, -Settings.STATUSBAR_HEIGHT);
        data.bottom = new FormAttachment(100, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
		progress.setLayoutData(data);
		progress.setVisible(false);

		refreshCustomisableElements();
    }
    
	/**
     * Try to save the data to the current file. 
     * @throws MedleyException
     */
    protected void saveFile() throws MedleyException {
    	if (!DataSource.get().save()) {
    		saveFileAs();
    	}
    }
    
    /**
     * Allow user to choose a file name and try to save current
     * data to this file.
     * @throws MedleyException
     */
    protected void saveFileAs() throws MedleyException {
    	FileDialog dialog = new FileDialog (shell, SWT.SAVE);
    	dialog.setFilterNames (new String [] {Resources.getStr("fileFilter.medleyFiles"), 
    										  Resources.getStr("fileFilter.allFiles")});
    	dialog.setFilterExtensions (new String [] {"*.zmd", "*.*"}); //Windows wild cards
    	String fn = dialog.open();
    	if (fn != null) {
			Cursor c = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
			shell.setCursor(c);
    		DataSource.getNotNull().save(fn);
	    	c.dispose();
	    	c = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
	    	shell.setCursor(c);
    	}
    }

    /**
     * Open new, empty file.
     * @throws MedleyException
     */
    protected void newFile() throws MedleyException {
		if (continueClosing()) {
    		com.mmakowski.medley.data.FileDataSource.createNew();
	    	DataSource.get().addDataSourceListener(this);
	    	updateTitleBar();
	    	refreshViews();
		}
    }
    
    /**
     * Allow user to select a file and try to open it.
     * @throws MedleyException
     */
    protected void openFile() throws MedleyException {
    	FileDialog dialog = new FileDialog (shell, SWT.OPEN);
    	dialog.setFilterNames (new String [] {Resources.getStr("fileFilter.medleyFiles"), 
    										  Resources.getStr("fileFilter.allFiles")});
    	dialog.setFilterExtensions (new String [] {"*.zmd", "*.*"}); //Windows wild cards
    	String fn = dialog.open();
    	openFile(fn);
    }

    /**
     * @param fn the name of file to open
     * @throws MedleyException
     * @throws DataSourceException
     */
    private void openFile(String fn) throws MedleyException, DataSourceException {
        if (fn != null) {
            // open the new file
    		if (continueClosing()) {
    			Cursor c = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
    			shell.setCursor(c);
    			com.mmakowski.medley.data.FileDataSource.create(fn);
		    	updateTitleBar();
		    	// check if this file format can be upgraded
		    	FileConverter conv = new FileConverter();
		    	conv.addProgressListener(this);
		    	if (conv.canConvert()) {
					MessageBox mb = new MessageBox(shell, 
							   SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					try {
						mb.setText(Resources.getStr(this, "mb.convertFile.title"));
						mb.setMessage(Resources.getStr(this, "mb.convertFile.msg"));
					} catch (ResourceException ex) {
				    	c = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
				    	shell.setCursor(c);
						(new ExceptionWindow(display, ex)).show();
					}
					if (mb.open() == SWT.YES) { 
						try {
							conv.convert();
						} catch (Exception ex) {
					    	c = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
					    	shell.setCursor(c);
							(new ExceptionWindow(display, ex)).show();
						}
					}
		    	}

		    	updateTitleBar();
		    	DataSource.get().addDataSourceListener(this);
		    	DataSource.get().addProgressListener(this);
		    	refreshViews();
		    	c.dispose();
		    	c = new Cursor(shell.getDisplay(), SWT.CURSOR_ARROW);
		    	shell.setCursor(c);
    		}
    	}
    }

	/**
     * Ask user if current file should be saved.
	 * @return true if closing of current file should continue
	 * @throws MedleyException
	 */
	protected boolean continueClosing() throws MedleyException {
		boolean cont = true;
		DataSource ds = DataSource.get();
		// if there's an unsaved file open ask user whether to save it
		if (ds != null && ds.requiresSave() && ds.isModified()) {
			MessageBox mb = new MessageBox(shell, 
										   SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			try {
				mb.setText(Resources.getStr(this, "mb.fileModified.title"));
				mb.setMessage(Resources.getStr(this, "mb.fileModified.msg"));
			} catch (ResourceException ex) {
		    	(new ExceptionWindow(display, ex)).show();
			}
			switch (mb.open()) {
			case SWT.CANCEL:
				cont = false;
				break;
			case SWT.YES:
				saveFile();
				break;
			}
		}
		return cont;
	}

    
    /**
     * Refresh all views.
     * @throws MedleyException
     */
    public void refreshViews() throws MedleyException {
    	for (Iterator i = views.iterator(); i.hasNext();) {
    		((View) i.next()).refresh();
    	}
    }
    
    /**
     * 
     * @param newView the view which is to be activated
     */
    protected void switchViewTo(View newView) {
    	if (newView == activeView) {
    		// switching to the same view
    		return;
    	}

    	// select appropriate tab
    	tabFolder.setSelection((CTabItem) viewTabMap.get(newView));
    	
    	activeView.deactivate();
    	activeView = newView;
    	activeView.activate();
    }

	/**
	 * @return Returns the menuBar.
	 */
	public Menu getMenuBar() {
		return menuBar;
	}
	
	/**
	 * @see com.mmakowski.medley.data.events.DataSourceListener#modifiedStateChanged(com.mmakowski.medley.data.events.DataSourceEvent)
	 */
	public void modifiedStateChanged(DataSourceEvent e) {
		updateTitleBar();
	}

	/**
	 * Update title bar. This might be called from other threads.
	 *
	 */
	protected void updateTitleBar() {
		final DataSource ds = (DataSource) DataSource.get(); 
		// all UI update needs to be done in UI thread or through
		// syncExec()/asyncExec().
		display.syncExec(new Runnable() {
			public void run() {
				// make sure shell is not disposed between the check and
				// trying to update the title
				synchronized (this) {
					if (shell.isDisposed()) {
						return;
					}
					try {
						if (ds.isModified()) {
							shell.setText(Resources.formatStr(this, "titleFileUnsaved", new Object[] {ds.getShortName()}));
						} else {
							shell.setText(Resources.formatStr(this, "titleFile", new Object[] {ds.getShortName()}));
						}
					} catch (ResourceException ex) {
			        	(new ExceptionWindow(display, ex)).show();
					}
				}
			}
		});
	}
	
	/**
	 * @return Returns the shell.
	 */
	public Shell getShell() {
		return shell;
	}

	/**
	 * @see com.mmakowski.events.ProgressListener#taskProgressed(com.mmakowski.events.ProgressEvent)
	 */
	public void taskProgressed(ProgressEvent e) {
		// assumption is made that we are notified synchronously,
		// i.e. from UI thread
		if (e.isCompleted()) {
			progress.setVisible(false);
		} else {
			progress.setVisible(true);
			try {
				progress.setStatus(Resources.getStr(this, "tp." + e.getTag()));
			} catch (ResourceException ex) {
				// ignore exception
			}
			progress.setProgressRange(e.getMinValue(), e.getMaxValue());
			progress.setProgressValue(e.getCurValue());
		}
	}

}
