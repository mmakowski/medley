/*
 * Created on 22-Jan-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * A group of ratings.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.6 $ $Date: 2005/04/24 16:02:44 $
 */
public class RatingGroup extends DataObject implements RatingGroupElement {

    /** logger */
    private static final Logger log = Logger.getLogger(RatingGroup.class.getName());

    /** the hierarchy of album ratings */
	private static HierarchyNode albumRatingHierarchy = null;
	/** the hierarchy of record ratings */
	private static HierarchyNode recordRatingHierarchy = null;
	/** the hierarchy of track ratings */
	private static HierarchyNode trackRatingHierarchy = null;
	/** the hierarchy of artist ratings */
	private static HierarchyNode artistRatingHierarchy = null;
	
	/**
	 * @param type type of ratable for which tag hierarchy should be returned.
	 * @return tag hierarchy for given Ratable type.
	 * @throws DataSourceException
	 */
	public static HierarchyNode getRatingHierarchy(int type) throws DataSourceException {
		switch (type) {
		case Ratable.ALBUM:
			if (albumRatingHierarchy == null) {
				albumRatingHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateRatingHierarchy(type, albumRatingHierarchy);
				albumRatingHierarchy.removeEmptySubgroups();
			}
			return albumRatingHierarchy;
		case Ratable.RECORD:
			if (recordRatingHierarchy == null) {
				recordRatingHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateRatingHierarchy(type, recordRatingHierarchy);
				recordRatingHierarchy.removeEmptySubgroups();
			}
			return recordRatingHierarchy;
		case Ratable.TRACK:
			if (trackRatingHierarchy == null) {
				trackRatingHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateRatingHierarchy(type, trackRatingHierarchy);
				trackRatingHierarchy.removeEmptySubgroups();
			}
			return trackRatingHierarchy;
		case Ratable.ARTIST:
			if (artistRatingHierarchy == null) {
				artistRatingHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateRatingHierarchy(type, artistRatingHierarchy);
				artistRatingHierarchy.removeEmptySubgroups();
			}
			return artistRatingHierarchy;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * Refresh tag hierarchy for given type
	 * @param type Ratable type
	 */
	public static void refreshRatingHierarchy(int type) throws DataSourceException {
		switch (type) {
		case Ratable.ALBUM:
			albumRatingHierarchy = null;
			break;
		case Ratable.RECORD:
			recordRatingHierarchy = null;
			break;
		case Ratable.TRACK:
			trackRatingHierarchy = null;
			break;
		case Ratable.ARTIST:
			artistRatingHierarchy = null;
			break;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * Refresh all tag hierarchies.
	 * @throws DataSourceException
	 */
	public static void refreshRatingHierarchies() throws DataSourceException {
		refreshRatingHierarchy(Ratable.ALBUM);
		refreshRatingHierarchy(Ratable.RECORD);
		refreshRatingHierarchy(Ratable.TRACK);
		refreshRatingHierarchy(Ratable.ARTIST);
	}
	
	/**
	 * Generate tag hierarchy for given Ratable type
	 * @param type Ratable type
	 * @param parentNode the node for which subtree is to be generated
	 * @throws DataSourceException
	 */ 
	private static void generateRatingHierarchy(int type, HierarchyNode parentNode) throws DataSourceException {
		log.finest("generateRatingHierarchy(" + type + ", \"" + parentNode.getLabel() + "\")");
		if (parentNode.getType() != HierarchyNode.GROUP) {
			return;
		}
		
		DataSource ds = getCurrentDataSource();
		
		// read data from the database
		Connection conn = null;
		try {
			// get groups with given parent id			
			conn = ds.connect();
			PreparedStatement pstmt = null;
			if (parentNode.getId() == 0) {
				// select top-level groups
				pstmt = conn.prepareStatement("SELECT ratingGroupId, rgr_name FROM RATING_GROUPS " +
						                      "WHERE rgr_parent IS NULL " +
					 				          "ORDER BY rgr_name");
			} else { 
				pstmt = conn.prepareStatement("SELECT ratingGroupId, rgr_name FROM RATING_GROUPS " +
											  "WHERE rgr_parent = ? " +
				          					  "ORDER BY rgr_name");
				pstmt.setInt(1, parentNode.getId());
			}
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				HierarchyNode node = new HierarchyNode(HierarchyNode.GROUP, res.getInt("ratingGroupId"), res.getString("rgr_name"));
				parentNode.addChild(node);
			}
			res.close();
			pstmt.close();
			// for each group add subgroups and tags
			for (Iterator i = parentNode.getChildren().iterator(); i.hasNext();) {
				generateRatingHierarchy(type, (HierarchyNode) i.next());
			}
			// get ratings with given parent id
			if (parentNode.getId() == 0) {
				// select top-level ratings
				pstmt = conn.prepareStatement("SELECT ratingId, rat_name FROM RATINGS " +
						  					  "WHERE rat_ratingGroup IS NULL AND rat_appliesTo = ? " +
						  					  "ORDER BY rat_name");
			} else { 
				pstmt = conn.prepareStatement("SELECT ratingId, rat_name FROM RATINGS " +
	  					  "WHERE rat_appliesTo = ? AND rat_ratingGroup = ? " +
	  					  "ORDER BY rat_name");
				pstmt.setInt(2, parentNode.getId());
			}
			pstmt.setString(1, Rating.itemTypeToString(type));
			res = pstmt.executeQuery();
			while (res.next()) {
				HierarchyNode node = new HierarchyNode(HierarchyNode.ELEMENT, res.getInt("ratingId"), res.getString("rat_name"));
				parentNode.addChild(node);
			}
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting rating groups and ratings from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}		
	}
	
	/**
	 * @return all rating groups
	 * @throws DataSourceException
	 */
	public static Vector getAllRatingGroups() throws DataSourceException {
		log.finest("getAllRatingGroups()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RATING_GROUPS " +
					 				  "ORDER BY rgr_name");
			ResultSet res = pstmt.executeQuery();
			Vector ratingGroups = new Vector();
			while (res.next()) {
				RatingGroup rg = new RatingGroup(
						res.getInt("ratingGroupId"),
						res.getString("rgr_name"),
						res.getInt("rgr_parent")
				);
				ratingGroups.add(rg);
			}
			res.close();
			pstmt.close();
			return ratingGroups;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error getting rating groups from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the rating group with given id from the database.
	 * @param id the id of rating group to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		RatingGroup rg = RatingGroup.load(id);
		int parentId = rg.getRatingGroupId();
		rg.dispose();
		
		// delete the tag group
		Connection conn = null;
		try {
			conn = ds.connect();
			// move all ratings from this group to parent group
			log.finer("moving all ratings from rating group " + id + " to " + parentId);
			PreparedStatement pstmt =
				conn.prepareStatement("UPDATE RATINGS SET rat_ratingGroup = ? WHERE rat_ratingGroup = ?");
			if (parentId == 0) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, parentId);
			}
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
			// move all rating groups from this group to parent group
			log.finer("moving all rating groups from rating group " + id + " to " + parentId);
			pstmt =	conn.prepareStatement("UPDATE RATING_GROUPS SET rgr_parent = ? WHERE rgr_parent = ?");
			if (parentId == 0) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, parentId);
			}
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
			
			log.finer("deleting rating group " + id);
			pstmt = conn.prepareStatement("DELETE FROM RATING_GROUPS WHERE ratingGroupId = ?");
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
			getCurrentDataSource().setModified();
			refreshRatingHierarchies();
			notifyObjectDeleted(RatingGroup.class, id);
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error deleting rating group " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	
	/**
	 * Add a new rating group to the database.
	 * @throws DataSourceException
	 */
	public static RatingGroup create() throws DataSourceException {
		log.finest("create()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO RATING_GROUPS (rgr_name) VALUES (?)");
			pstmt.setString(1, getSafeNewName("RATING_GROUPS", "rgr_name", "New Rating Group"));
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(ratingGroupId) AS id FROM RATING_GROUPS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"RatingGroup"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			RatingGroup rgr = RatingGroup.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			refreshRatingHierarchies();
			notifyObjectCreated(rgr);
			return rgr;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new rating group", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Load rating group with given id from database
	 * @param id rating group's id
	 * @return loaded rating group
	 * @throws DataSourceException
	 */
	public static RatingGroup load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		RatingGroup rg = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RATING_GROUPS WHERE ratingGroupId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"RatingGroup", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			rg = new RatingGroup(
					id,
					res.getString("rgr_name"),
					res.getInt("rgr_parent")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading rating group " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return rg;
	}

	/** id of rating group */
	private int id;
	/** name of rating group */
	private String name;
	/** id of parent rating group */
	private int parentId;
	
	/**
	 * @throws DataSourceException
	 */
	private RatingGroup(int id, String name, int parentId) throws DataSourceException {
		log.finest("RatingGroup(" + id + ", \"" + name + "\", " + parentId + ")");
        this.id = id;
		this.name = name;
		this.parentId = parentId;
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#save()
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		if (!isModified()) {
			return;
		}

		log.finer("saving rating group " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE RATING_GROUPS SET " + 
									  "rgr_name = ?, " +
									  "rgr_parent = ? " +
							          "WHERE ratingGroupId = ?");
			pstmt.setString(1, name);
			if (parentId == 0) {
				pstmt.setNull(2, Types.INTEGER);
			} else {
				pstmt.setInt(2, parentId);
			}
			pstmt.setInt(3, id);
			pstmt.executeUpdate();
			clearModified();
			refreshRatingHierarchies();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving rating group" + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
        log.finest("deleteSelf()");
		delete(id);
	}

	/**
	 * @see com.mmakowski.medley.data.RatingGroupElement#getRatingGroup()
	 */
	public RatingGroup getRatingGroup() throws DataSourceException {
		if (parentId == 0) {
			return null;
		} else {
			return RatingGroup.load(parentId);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.RatingGroupElement#setRatingGroupId(int)
	 */
	public void setRatingGroupId(int id) throws DataSourceException {
		if (id != NO_GROUP) {
			// check that this does not create cycle.
			RatingGroup parent = RatingGroup.load(id);
			boolean allowed = parent.canBeSubgroup(this);
			parent.dispose();
			if (!allowed) {
				log.warning("rating group " + this.id + " cannot be a subgroup of rating group " + id);
                throw new DataSourceException(Errors.RATING_GROUP_CANT_BE_A_SUBGROUP, new Object[] {new Integer(this.id), new Integer(id)});
			}
		}
		parentId = id;
		attributeChanged();
	}
	
	/**
	 * Check if given rating group can belong to this group
	 * @param rgroup potential subgroup
	 * @return true if tgroup can belong to this group, false otherwise
	 * @throws DataSourceException
	 */
	public boolean canBeSubgroup(RatingGroup rgroup) throws DataSourceException {
		// check that rgroup does not appear up in the tree:
		if (parentId == 0) {
			return true;
		} else if (parentId == rgroup.getId()) {
			return false;
		} else {
			RatingGroup parent = RatingGroup.load(parentId);
			boolean allowed = parent.canBeSubgroup(rgroup);
			parent.dispose();
			return allowed;
		}
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) throws DataSourceException {
		this.name = name;
		attributeChanged();
	}
	
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @see com.mmakowski.medley.data.RatingGroupElement#getRatingGroupId()
	 */
	public int getRatingGroupId() {
		return parentId;
	}
	
}
