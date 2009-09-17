/*
 * Created on 22-Jan-2005
 */
package com.mmakowski.medley.data;

/**
 * Interface for objects that can be elements of rating groups.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/23 20:53:27 $
 */
public interface RatingGroupElement {
	/** value indicating that this element does not belong to any group */
	public static final int NO_GROUP = 0;
	/**
	 * @return this element's rating group or null if it does not belong to any tag group
	 * @throws DataSourceException
	 */
	RatingGroup getRatingGroup() throws DataSourceException;
	/**
	 * @return id of rating group for this item or 0 if it has not got a rating group assigned.
	 */
	int getRatingGroupId();
	/**
	 * @param id the id of rating group for this element
	 */
	void setRatingGroupId(int id) throws DataSourceException;
}
