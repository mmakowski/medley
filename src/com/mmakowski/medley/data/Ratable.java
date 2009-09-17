/*
 * Created on 22-Jan-2005
 */
package com.mmakowski.medley.data;

/**
 * Interface for items that can have ratings assinged.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/10 20:55:48 $
 */
public interface Ratable {
	// types of ratable items
	public static final int ALBUM = DataObject.ALBUM;
	public static final int RECORD = DataObject.RECORD;
	public static final int TRACK = DataObject.TRACK;
	public static final int ARTIST = DataObject.ARTIST;

	/**
	 * @return type of this taggable 
	 */
	int getRatableType();
	
	/**
	 * @param ratingId id of rating
	 * @param score assigned score
	 * @throws DataSourceException
	 */
	void setRatingScore(int ratingId, String score) throws DataSourceException;
	
	/**
	 * @param ratingId id of rating
	 * @return score in given rating
	 * @throws DataSourceException
	 */
	Integer getRatingScore(int ratingId) throws DataSourceException;
	
	/**
	 * @param ratingId id of rating
	 * @return score in given rating
	 * @throws DataSourceException
	 */
	String getRatingScoreString(int ratingId) throws DataSourceException;
	
	/**
	 * Delete latest score in given rating.
	 * @param ratingId id of rating for which latest score should be deleted
	 * @throws DataSourceException
	 */
	void deleteLatestRatingScore(int ratingId) throws DataSourceException;
}
