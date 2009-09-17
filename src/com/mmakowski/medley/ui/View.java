/*
 * Created on 26-Dec-2004
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;

/**
 * An abstract class representing a view. A view comprises
 * main window pane, view-specific menu and view-specific toolbar.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/01/22 20:18:04 $
 */
public abstract class View {
	/** data key for field idicating owner of menu/toolbar item */
	protected static final String VIEW = "view";
	protected static final String REMOVE = "remove";
	// TODO: toolbar customisation
	
	/** view pane */
	protected ViewPane pane;
	/** main window */
	protected MainWindow mainWindow;
	/** view menu entry */
	protected MenuItem viewMenuItem;
	
	/**
	 * Default constructor.
	 */
	public View(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		addViewMenuEntry();
	}
	
	/**
	 * Activate this view.
	 *
	 */
	public void activate() {
    	try {
    		addMenuItems(mainWindow.getMenuBar());
    	} catch (MedleyException ex) {
	    	(new ExceptionWindow(Display.getCurrent(), ex)).show();
    	}
		viewMenuItem.setSelection(true);
	}
	
	/**
	 * Deactivate this view.
	 *
	 */
	public void deactivate() {
		viewMenuItem.setSelection(false);
		removeMenuItems(mainWindow.getMenuBar());
	}
	
 	/**
	 * @return Returns the pane.
	 */
	public ViewPane getPane() {
		return pane;
	}
	
	/**
	 * @param pane The pane to set.
	 */
	public void setPane(ViewPane pane) {
		this.pane = pane;
	}

	/**
	 * Create view's pane.
	 * @param parent the parent widget for the pane
	 * @return pane created
	 */
	public abstract ViewPane createPane(Composite parent) throws MedleyException;

	/**
	 * @return string tag for this view
	 */
	protected abstract String getTag();
	
	/**
	 * Refresh the contents of view's pane.
	 * @throws MedleyException
	 */
	public void refresh() throws MedleyException {
		if (pane != null) {
			pane.refresh();
		}
	}
	
	/**
	 * @return relative path to view's icon
	 */
	public String getIconPath(int size) {
		return "img/viewIcon-" + getTag() + "-" + String.valueOf(size) + ".gif";
	}
	
	/**
	 * @return the title of this view
	 * @throws MedleyException
	 */
	public String getTitle() throws MedleyException {
		return Resources.getStr(this, "title");
	}
	
	/**
	 * @return the tooltip text for this view
	 * @throws MedleyException
	 */
	public String getTooltip() throws MedleyException {
		return Resources.getStr(this, "toolTip");
	}
	
	/**
	 * Remove menu items specific to this view.
	 * @param menu menu to remove items from
	 */
	public void removeMenuItems(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(VIEW) == this && 
					items[i].getData(REMOVE) != null &&
					items[i].getData(REMOVE).equals(new Integer(1))) {
				items[i].dispose();
			} else {
				Menu submenu = items[i].getMenu();
				if (submenu != null) {
					removeMenuItems(submenu);
				}
			}
		}
	}
	
	/**
	 * Add view-specific menu items
	 * @param menu the menu bar
	 */
	public abstract void addMenuItems(Menu menu) throws MedleyException;
	
	/**
	 * Add menu item for this view to View menu.
	 *
	 */
	protected void addViewMenuEntry() {
		MenuItem[] items = mainWindow.getMenuBar().getItems();
		for (int i = 0; i < items.length; i++) {
			String s = (String) items[i].getData(MainWindow.MENU_ID);
			if (s != null && s.equals(MainWindow.MENU_VIEW)) {
		        // view menu
				Menu submenu = items[i].getMenu();
				// -- this view
				final View me = this;
		        viewMenuItem = new MenuItem(submenu, SWT.RADIO);
		        viewMenuItem.addListener(SWT.Selection, new Listener () {
		            public void handleEvent(Event e) {
		            	mainWindow.switchViewTo(me);
		            }
		        });
		        try {
		        	viewMenuItem.setText(Resources.getStr(this, "menu.view.this"));
		        } catch (ResourceException ex) {
                	(new ExceptionWindow(Display.getCurrent(), ex)).show();
		        }
		        viewMenuItem.setData(VIEW, this);
			}
		}
	}
}
