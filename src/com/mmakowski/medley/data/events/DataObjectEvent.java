/*
 * Created on 09-Jan-2005
 */
package com.mmakowski.medley.data.events;

import java.util.EventObject;

/**
 * Event object for data object event.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/10 20:55:52 $
 */
public class DataObjectEvent extends EventObject {
	public static final int ATTRIBUTE_CHANGED = 0;
	public static final int OBJECT_CREATED = 1;
	public static final int OBJECT_SAVED = 2;
	public static final int OBJECT_DELETED = 3;
	
	/** event type */
	protected int type;
	/** object id */
	protected int id;
	
	/**
	 * Construct a DataObjectEvent.
	 * @param source the source of event
	 */
	public DataObjectEvent(Object source, int type, int id) {
		super(source);
		this.type = type;
		this.id = id;
	}

	/**
	 * @return event type
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * @return object id
	 */
	public int getId() {
		return id;
	}
}
