/*
 * Created on 09-Jan-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * A value for enum tag. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.6 $ $Date: 2005/04/24 16:02:43 $
 */
public class TagValue extends DataObject {

    /** logger */
    private static final Logger log = Logger.getLogger(TagValue.class.getName());

    /**
	 * @param tagId id of tag
	 * @return all values for given tag
	 * @throws DataSourceException
	 */
	public static Vector getAllForTag(int tagId) throws DataSourceException {
		log.finest("getAllForTag(" + tagId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT tagValueId FROM TAG_VALUES " +
									  "WHERE tvl_tag = ? " +
					 				  "ORDER BY tvl_value");
			pstmt.setInt(1, tagId);
			ResultSet res = pstmt.executeQuery();
			Vector vals = new Vector();
			while (res.next()) {
				vals.add(new TagValue(res.getInt("tagValueId")));
			}
			res.close();
			pstmt.close();
			return vals;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting tag values from database for tag id " + tagId, ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @param tagId id of tag
	 * @return all values (as strings) for given tag
	 * @throws DataSourceException
	 */
	public static Vector getAllStringsForTag(int tagId) throws DataSourceException {
		Vector vals = getAllForTag(tagId);
		Vector strs = new Vector();
		for (Iterator i = vals.iterator(); i.hasNext();) {
			TagValue v = (TagValue) i.next();
			strs.add(v.getValue());
		}
		disposeAll(vals);
		return strs;
	}
	
	/**
	 * Delete the tag value with given id from the database.
	 * @param id the id of tag value to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(TagValue.class, "TAG_VALUES", "tagValueId", id);
	}

	/**
	 * Add a new tag value to the database.
	 * @throws DataSourceException
	 */
	public static TagValue create(int tagId) throws DataSourceException {
		log.finest("create(" + tagId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO TAG_VALUES (tvl_tag, tvl_value) VALUES (?, ?)");
			pstmt.setInt(1, tagId);
			pstmt.setString(2, getSafeNewName("TAG_VALUES", "tvl_value", "New Tag Value"));
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(tagValueId) AS id FROM TAG_VALUES");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"TagValue"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			TagValue tvl = new TagValue(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			notifyObjectCreated(tvl);
			return tvl;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new tag", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	

	/** id of tag */
	private int id;
	/** tag id */
	private int tagId;
	/** the value */
	private String value;
	
	/**
	 * @throws DataSourceException
	 */
	public TagValue(int id) throws DataSourceException {
		log.finest("TagValue(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TAG_VALUES WHERE tagValueId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"TagValue", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			this.id = id;
			this.tagId = res.getInt("tvl_tag");
			this.value = res.getString("tvl_value");
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading tag value " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#save()
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		if (!isModified()) {
			return;
		}

		log.finer("saving tag value " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE TAG_VALUES SET " + 
									  "tvl_tag = ?, " +
									  "tvl_value = ? " +
							          "WHERE tagValueId = ?");
			pstmt.setInt(1, tagId);
			pstmt.setString(2, value);
			pstmt.setInt(3, id);
			pstmt.executeUpdate();
			clearModified();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error saving tag value " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @return Returns the tagId.
	 */
	public int getTagId() {
		return tagId;
	}
	/**
	 * @param tagId The tagId to set.
	 */
	public void setTagId(int tagId) throws DataSourceException {
		this.tagId = tagId;
		attributeChanged();
	}
	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) throws DataSourceException {
		this.value = value;
		attributeChanged();
	}
	/**
	 * @see com.mmakowski.medley.data.DataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
        checkDisposed();
		delete(id);
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}
}
