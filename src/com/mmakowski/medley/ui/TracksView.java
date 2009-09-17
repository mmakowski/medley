/*
 * Created on 01-Jan-2005
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
 * View presenting the list of all tracks.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/01/23 20:53:31 $
 */
public class TracksView extends View {

	/**
	 * @param mainWindow the main window
	 */
	public TracksView(MainWindow mainWindow) {
		super(mainWindow);
	}

	/**
	 * @see com.mmakowski.medley.ui.View#createPane(org.eclipse.swt.widgets.Composite)
	 */
	public ViewPane createPane(Composite parent) throws MedleyException {
		pane = new TrackListPane(parent, SWT.NONE);
		return pane;
	}

	/**
	 * @see com.mmakowski.medley.ui.View#getTag()
	 */
	protected String getTag() {
		return "tracks";
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
	
				// -- custom tags
		        MenuItem item = new MenuItem(submenu, SWT.PUSH);
		        item.addListener(SWT.Selection, new Listener () {
		            public void handleEvent(Event e) {
		                try {
		                    TagsWindow tagsWin = new TagsWindow(mainWindow.getShell(), Taggable.TRACK);
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
		                    RatingsWindow ratingsWin = new RatingsWindow(mainWindow.getShell(), Ratable.TRACK);
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
