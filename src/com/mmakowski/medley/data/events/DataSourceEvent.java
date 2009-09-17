/*
 * Created on 03-Jan-2005
 */
package com.mmakowski.medley.data.events;

import java.util.EventObject;

import com.mmakowski.medley.data.DataSource;

/**
 * A class representing data source events.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/03 11:13:39 $
 */
public class DataSourceEvent extends EventObject {
	// constants for event types
	public static int MODIFIED_STATE_CHANGED = 1;
	
	/**	The type of event */
	protected int type;
	
	/**
	 * Construct a DataSourceEvent.
	 * @param source the source of event
	 * @param type the type of event
	 */
	public DataSourceEvent(DataSource source, int type) {
		super(source);
		this.type = type;
	}
	
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
}
