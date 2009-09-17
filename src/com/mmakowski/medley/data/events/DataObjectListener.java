/*
 * Created on 09-Jan-2005
 */
package com.mmakowski.medley.data.events;

import java.util.EventListener;

/**
 * An interface for objects that listen to data object events.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/04/10 20:55:52 $
 */
public interface DataObjectListener extends EventListener {

	/**
	 * Called when a value of object's attribute changes
	 * @param e
	 */
	void attributeChanged(DataObjectEvent e);
	
	/**
	 * Called when object has been created
	 * @param e
	 */
	void objectCreated(DataObjectEvent e);
	
	/**
	 * Called when object has been saved
	 * @param e
	 */
	void objectSaved(DataObjectEvent e);

	/**
	 * Called when object has been deleted
	 * @param e
	 */
	void objectDeleted(DataObjectEvent e);
	
}
