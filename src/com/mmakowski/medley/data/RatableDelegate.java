/*
 * Created on 22-Jan-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A class of objects to which other object can delegate their Ratable behaviour.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $ $Date: 2005/04/24 16:02:44 $
 */
class RatableDelegate implements Ratable {

    /** logger */
    private static final Logger log = Logger.getLogger(RatableDelegate.class.getName());

    /** ratable type */
	private int type;
	/** data source */
	private DataSource ds;
	/** column prefix */
	private String colPrefix;
	/** rating table prefix */
	private String tablePrefix;
	/** name of key column */
	private String keyName;
	/** id of ratable */
	private int id;
	/** mapping of rating id to rating score */
	private Hashtable ratingMap;
	/** mapping of rating id to rating score string */
	private Hashtable ratingStringMap;
	
	/**
	 * Construct a ratable delegate
	 * @param type ratable type
	 */
	public RatableDelegate(int type, DataSource ds, String colPrefix, String tablePrefix, String key, int id) {
	    log.finest("RatableDelegate(" + type + ",...)");
        this.type = type;
		this.ds = ds;
		this.colPrefix = colPrefix;
		this.tablePrefix = tablePrefix;
		this.keyName = key;
		this.id = id;
		this.ratingMap = new Hashtable();
		this.ratingStringMap = new Hashtable();
	}
	
	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatableType()
	 */
	public int getRatableType() {
		return type;
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#setRatingScore(int, int)
	 */
	public void setRatingScore(int ratingId, String score)
			throws DataSourceException {
		ratingStringMap.put(new Integer(ratingId), score);
		Rating r = Rating.load(ratingId);
		ratingMap.put(new Integer(ratingId), new Integer(r.toIntScore(score)));
		r.dispose();
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScore(int)
	 */
	public Integer getRatingScore(int ratingId) throws DataSourceException {
		return (Integer) ratingMap.get(new Integer(ratingId)); 
	}
	
	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScoreString(int)
	 */
	public String getRatingScoreString(int ratingId) throws DataSourceException {
		String v = (String) ratingStringMap.get(new Integer(ratingId)); 
		return v == null ? "" : v;
	}

	/**
	 * Set rating score based on int score value
	 * @param ratingId id of rating
	 * @param score int score value
	 * @throws DataSourceException
	 */
	private void setRatingScore(int ratingId, int score) throws DataSourceException {
		ratingMap.put(new Integer(ratingId), new Integer(score));
		Rating r = Rating.load(ratingId);
		ratingStringMap.put(new Integer(ratingId), r.toStringScore(score));
		r.dispose();
	}

	/**
	 * Load rating scores
	 * @throws DataSourceException
	 */
	public void loadRatings() throws DataSourceException {
		log.finest("loadRatings()");

		// read data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT " + colPrefix + "_rating, " + colPrefix + "_score " +
						"FROM " + tablePrefix + "_RATINGS " +
						"WHERE " + colPrefix + "_" + keyName + " = ? ORDER BY " + colPrefix + "_dateTime");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				int ratingId = res.getInt(colPrefix + "_rating");
				int score = res.getInt(colPrefix + "_score");
				setRatingScore(ratingId, score);
			}
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading ratings for " + keyName + " " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Save rating scores to the database.
	 * @throws DataSourceException
	 */
	public void saveRatings() throws DataSourceException {
		log.finest("saveRatings()");

		Connection conn = null;
		try {
			conn = ds.connect();
			for (Enumeration e = ratingMap.keys(); e.hasMoreElements();) {
				Integer key = (Integer) e.nextElement();
				Integer score = (Integer) ratingMap.get(key);
				// check if score for this rating exists and is the same as new one
				PreparedStatement pstmt = 
					conn.prepareStatement("SELECT " + colPrefix + "_score FROM " + tablePrefix + "_RATINGS " +
							"WHERE " + colPrefix + "_" + keyName + " = ? AND " + colPrefix + "_rating = ? " +
							"ORDER BY " + colPrefix + "_dateTime DESC"); 
				pstmt.setInt(1, id);
				pstmt.setInt(2, key.intValue());
				ResultSet res = pstmt.executeQuery();
				boolean scoreChanged = true;
				if (res.next()) {
					if (res.getInt(colPrefix + "_score") == score.intValue()) {
						scoreChanged = false;
					}
				}
				res.close();
				pstmt.close();
				if (scoreChanged) {
					// create new score
					pstmt = 
						conn.prepareStatement("INSERT INTO " + tablePrefix + "_RATINGS " +
								"(" + colPrefix + "_" + keyName + ", " + colPrefix + "_rating, " + colPrefix + "_score) " +
								"VALUES (?, ?, ?)");
					pstmt.setInt(1, id);
					pstmt.setInt(2, key.intValue());
					pstmt.setInt(3, score.intValue());
					pstmt.executeUpdate();
					pstmt.close();
				}
			}
			ds.setModified();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving ratings for " + keyName + " " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#deleteLatestRatingScore(int)
	 */
	public void deleteLatestRatingScore(int ratingId) throws DataSourceException {
		log.finest("deleteLatestScore(" + ratingId + ")");

		DataSource ds = DataSource.getNotNull();
		// read data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			// NOTE: this SQL query might not work in simpler DBMSs
			PreparedStatement pstmt = 
				conn.prepareStatement("DELETE " +
						"FROM " + tablePrefix + "_RATINGS " +
						"WHERE " + colPrefix + "_" + keyName + " = ? AND " + colPrefix + "_rating = ? " +
						"AND " + colPrefix + "_dateTime = " +
								"(SELECT MAX(" + colPrefix + "_dateTime) FROM " + tablePrefix + "_RATINGS " +
								"WHERE " + colPrefix + "_" + keyName + " = ? AND " + colPrefix + "_rating = ?)");
			pstmt.setInt(1, id);
			pstmt.setInt(2, ratingId);
			pstmt.setInt(3, id);
			pstmt.setInt(4, ratingId);
			
			pstmt.executeUpdate();
			pstmt.close();
			ds.setModified();
			// remove score from maps
			ratingMap.remove(new Integer(ratingId));
			ratingStringMap.remove(new Integer(ratingId));
			// reload latest score for given rating
			loadRating(ratingId);
			
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error deleting latest rating " + ratingId + " score for " + keyName + " " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Load latest score for given ranking to score maps.
	 * @param ratingId id of rating for which score should be loaded
	 * @throws DataSourceException 
	 */
	private void loadRating(int ratingId) throws DataSourceException {
		log.finest("loadRating(" + ratingId + ")");

		// read data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT " + colPrefix + "_score " +
						"FROM " + tablePrefix + "_RATINGS " +
						"WHERE " + colPrefix + "_" + keyName + " = ? AND " + colPrefix + "_rating = ? " +
						"ORDER BY " + colPrefix + "_dateTime");
			pstmt.setInt(1, id);
			pstmt.setInt(2, ratingId);
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				int score = res.getInt(colPrefix + "_score");
				setRatingScore(ratingId, score);
			}
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading rating " + ratingId + " for " + keyName + " " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	
}
