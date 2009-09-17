/*
 * Created on 05-Jan-2005
 */
package com.mmakowski.medley.data;

/**
 * Interface for items that can have tags assigned.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/02/17 22:40:35 $
 */
public interface Taggable {
	// types of taggable items
	public static final int ALBUM = DataObject.ALBUM;
	public static final int RECORD = DataObject.RECORD;
	public static final int TRACK = DataObject.TRACK;
	public static final int ARTIST = DataObject.ARTIST;
	
	/**
	 * @return type of this taggable 
	 */
	int getTaggableType();
	
	/**
	 * @param tagId id of tag
	 * @param value value of tag
	 * @throws DataSourceException
	 */
	void setTagValue(int tagId, String value) throws DataSourceException;
	
	/**
	 * @param tagId id of tag
	 * @return value of given tag
	 * @throws DataSourceException
	 */
	String getTagValue(int tagId) throws DataSourceException;
}
