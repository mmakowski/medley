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
import com.mmakowski.medley.data.Ratable;
import com.mmakowski.medley.data.Taggable;
import com.mmakowski.medley.resources.Resources;

/**
 * The view which presents all the artists in the database.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/01/23 20:53:30 $
 */
public class ArtistsView extends View {

	/**
	 * @param mainWindow the main window
	 */
	public ArtistsView(MainWindow mainWindow) {
		super(mainWindow);
	}

	
	/**
	 * @see com.mmakowski.medley.ui.View#createPane(org.eclipse.swt.widgets.Composite)
	 */
	public ViewPane createPane(Composite parent) throws MedleyException {
		pane = new ArtistListPane(parent, SWT.NONE);
		return pane;
	}

	/**
	 * @see com.mmakowski.medley.ui.View#getTag()
	 */
	protected String getTag() {
		return "artists";
	}

	/**
	 * @see com.mmakowski.medley.ui.View#addMenuItems(org.eclipse.swt.widgets.Menu)
	 */
	public void addMenuItems(Menu menu) throws MedleyException {
		MenuItem[] items = menu.getItems();
		for (int i = 0; i < items.length; i++) {
			String s = (String) items[i].getData(MainWindow.MENU_ID);
			if (s != null && s.equals(MainWindow.MENU_EDIT)) {
		        // edit menu
				Menu submenu = items[i].getMenu();
		        // -- new artist
		        MenuItem item = new MenuItem(submenu, SWT.PUSH);
		        item.addListener(SWT.Selection, new Listener () {
		            public void handleEvent(Event e) {
		                try {
		                    ArtistWindow artistWin = new ArtistWindow(mainWindow.getShell(), true);
		                	artistWin.show();
		                	mainWindow.refreshViews();
		                } catch (MedleyException ex) {
		                	(new ExceptionWindow(Display.getCurrent(), ex)).show();
		                }
		            }
		        });
		        item.setText(Resources.getStr(this, "menu.edit.newArtist"));
		        item.setAccelerator(SWT.CTRL + 'N');
		        item.setData(VIEW, this);
		        item.setData(REMOVE, new Integer(1));
		        
		        // -- custom tags
		        item = new MenuItem(submenu, SWT.PUSH);
		        item.addListener(SWT.Selection, new Listener () {
		            public void handleEvent(Event e) {
		                try {
		                    TagsWindow tagsWin = new TagsWindow(mainWindow.getShell(), Taggable.ARTIST);
		                    tagsWin.show();
		                } catch (MedleyException ex) {
		                	(new ExceptionWindow(Display.getCurrent(), ex)).show();
		                }
		            }
		        });
		        item.setText(Resources.getStr(this, "menu.edit.customTags"));
		        item.setAccelerator(SWT.CTRL + 'T');
		        item.setData(VIEW, this);
		        item.setData(REMOVE, new Integer(1));

		        // -- ratings
		        item = new MenuItem(submenu, SWT.PUSH);
		        item.addListener(SWT.Selection, new Listener () {
		            public void handleEvent(Event e) {
		                try {
		                    RatingsWindow ratingsWin = new RatingsWindow(mainWindow.getShell(), Ratable.ARTIST);
		                    ratingsWin.show();
		                } catch (MedleyException ex) {
		                	(new ExceptionWindow(Display.getCurrent(), ex)).show();
		                }
		            }
		        });
		        item.setText(Resources.getStr(this, "menu.edit.ratings"));
		        item.setAccelerator(SWT.CTRL + 'R');
		        item.setData(VIEW, this);
		        item.setData(REMOVE, new Integer(1));
			}
		}
	}
	
}
