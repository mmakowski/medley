/*
 * Created on 23-Jan-2005
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
 * A class of objects to which other objects can delegate their
 * Taggable behaviour.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/04/24 16:02:44 $
 */
class TaggableDelegate implements Taggable {

	/** taggable type */
	private int type;
	/** mapping of tag id to tag value */
	private Hashtable tagMap;

    /** logger */
    private static final Logger log = Logger.getLogger(TaggableDelegate.class.getName());

    /**
	 * Create a taggable delegate. 
	 * @param type Taggable type
	 */
	public TaggableDelegate(int type) {
        log.finest("TaggableDelegate(" + type + ")");
		this.type = type;
		this.tagMap = new Hashtable();
	}
	
	/**
	 * @see com.mmakowski.medley.data.Taggable#getTaggableType()
	 */
	public int getTaggableType() {
		return type;
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#setTagValue(int, java.lang.String)
	 */
	public void setTagValue(int tagId, String value) throws DataSourceException {
		tagMap.put(new Integer(tagId), value);
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#getTagValue(int)
	 */
	public String getTagValue(int tagId) throws DataSourceException {
		String v = (String) tagMap.get(new Integer(tagId)); 
		return v == null ? "" : v;
	}

	/**
	 * Load tag values from database.
	 * @throws DataSourceException
	 */
	public void loadTags(DataSource ds, String colPrefix, String tablePrefix, String key, int id) throws DataSourceException {
		log.finest("loadTags(" + ds + ", \"" + colPrefix + "\", \"" + tablePrefix + "\", \"" + key + "\", " + id + ")");
		tagMap = new Hashtable();
		
		// read data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT " + colPrefix + "_tag, " + colPrefix + "_value FROM " + tablePrefix + "_TAGS " +
						"WHERE " + colPrefix + "_" + key + " = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				tagMap.put(new Integer(res.getInt(colPrefix + "_tag")),
						res.getString(colPrefix + "_value"));
			}
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading tags for " + key + " " + id + "\" from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Save tag values to the database.
	 * @throws DataSourceException
	 */
	public void saveTags(DataSource ds, String colPrefix, String tablePrefix, String keyName, int id) throws DataSourceException {
        log.finest("saveTags(" + ds + ", \"" + colPrefix + "\", \"" + tablePrefix + "\", \"" + keyName + "\", " + id + ")");

		Connection conn = null;
		try {
			conn = ds.connect();
			for (Enumeration e = tagMap.keys(); e.hasMoreElements();) {
				Integer key = (Integer) e.nextElement();
				String value = (String) tagMap.get(key);
				// check if value for this tag exists
				PreparedStatement pstmt = 
					conn.prepareStatement("SELECT " + keyName + "TagId FROM " + tablePrefix + "_TAGS " +
							"WHERE " + colPrefix + "_" + keyName + " = ? AND " + colPrefix + "_tag = ?"); 
				pstmt.setInt(1, id);
				pstmt.setInt(2, key.intValue());
				ResultSet res = pstmt.executeQuery();
				int atId = 0;
				if (res.next()) {
					atId = res.getInt(keyName + "TagId");
				}
				res.close();
				pstmt.close();
				if (atId == 0) {
					// create new value
					pstmt = 
						conn.prepareStatement("INSERT INTO " + tablePrefix + "_TAGS " +
								"(" + colPrefix + "_" + keyName + ", " + colPrefix + "_tag, " + colPrefix + "_value) " +
								"VALUES (?, ?, ?)");
					pstmt.setInt(1, id);
					pstmt.setInt(2, key.intValue());
					pstmt.setString(3, value);
				} else {
					// update existing value
					pstmt = 
						conn.prepareStatement("UPDATE " + tablePrefix + "_TAGS SET " + 
											  colPrefix + "_value = ? " +
									          "WHERE " + keyName + "TagId = ?");
					pstmt.setString(1, value);
					pstmt.setInt(2, atId);
				}
				pstmt.executeUpdate();
				pstmt.close();
			}
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving tags for " + keyName + " " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	
}
