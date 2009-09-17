/*
 * Created on 03-Jan-2005
 */
package com.mmakowski.medley.data.events;

import java.util.EventListener;

/**
 * An interface for objects that listen to data source events.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/03 11:13:39 $
 */
public interface DataSourceListener extends EventListener {
	/**
	 * Called when "modified" state of data source changes. 
	 * @param e
	 */
	void modifiedStateChanged(DataSourceEvent e);
}
