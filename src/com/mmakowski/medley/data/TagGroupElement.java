/*
 * Created on 05-Jan-2005
 */
package com.mmakowski.medley.data;

/**
 * Interface for objects that can be elements of TagGroups.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/09 13:32:52 $
 */
public interface TagGroupElement {
	/** value indicating that this element does not belong to any group */
	public static final int NO_GROUP = 0;
	/**
	 * @return this element's tag group or null if it does not belong to any tag group
	 * @throws DataSourceException
	 */
	TagGroup getTagGroup() throws DataSourceException;
	/**
	 * @return id of tag group for this item or 0 if it has not got a tag group assigned.
	 */
	int getTagGroupId();
	/**
	 * @param id the id of tag group for this element
	 */
	void setTagGroupId(int id) throws DataSourceException;
}
