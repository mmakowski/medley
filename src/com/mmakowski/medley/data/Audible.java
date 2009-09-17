/*
 * Created on 10-Apr-2005
 */
package com.mmakowski.medley.data;

import java.util.Date;
import java.util.Vector;

/**
 * An interface for items that can have auditions assigned to them.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/05/22 00:07:38 $
 */
public interface Audible {

	// types of audible items
	int ALBUM = DataObject.ALBUM;
	int RECORD = DataObject.RECORD;
	int TRACK = DataObject.TRACK;

	/**
	 * @return type of this Audible
	 */
	int getAudibleType();
	
	/**
	 * Record audition in the database.
	 * @param dateTime date and time of audition. If <code>null</code>, then current date and time is used.
	 * @param subItemCount number of sub-items auditioned. If less than 1 then total number of subitems for this Audible is used.
	 * @return the new audition
	 * @throws DataSourceException
	 */
	Audition recordAudition(Date dateTime, int subItemCount) throws DataSourceException;
	
	/**
	 * @return a Vector containing all auditions for this Audible
	 * @throws DataSourceException
	 */
	Vector getAuditions() throws DataSourceException;
    
    /**
     * @return <code>true</code> if this Audible can have subitems, <code>false</code> otherwise
     */
    boolean hasSubitems() throws DataSourceException;
}
