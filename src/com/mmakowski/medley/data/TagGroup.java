/*
 * Created on 05-Jan-2005
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
 * A group of tags.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $ $Date: 2005/04/24 16:02:43 $
 */
public class TagGroup extends DataObject implements TagGroupElement {

    /** logger */
    private static final Logger log = Logger.getLogger(TagGroup.class.getName());

    /** the hierarchy of album tags */
	private static HierarchyNode albumTagHierarchy = null;
	/** the hierarchy of record tags */
	private static HierarchyNode recordTagHierarchy = null;
	/** the hierarchy of track tags */
	private static HierarchyNode trackTagHierarchy = null;
	/** the hierarchy of artist tags */
	private static HierarchyNode artistTagHierarchy = null;
	
	/**
	 * @param type type of taggable for which tag hierarchy should be returned.
	 * @return tag hierarchy for given Taggable type.
	 * @throws DataSourceException
	 */
	public static HierarchyNode getTagHierarchy(int type) throws DataSourceException {
		switch (type) {
		case Taggable.ALBUM:
			if (albumTagHierarchy == null) {
				albumTagHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateTagHierarchy(type, albumTagHierarchy);
				albumTagHierarchy.removeEmptySubgroups();
			}
			return albumTagHierarchy;
		case Taggable.RECORD:
			if (recordTagHierarchy == null) {
				recordTagHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateTagHierarchy(type, recordTagHierarchy);
				recordTagHierarchy.removeEmptySubgroups();
			}
			return recordTagHierarchy;
		case Taggable.TRACK:
			if (trackTagHierarchy == null) {
				trackTagHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateTagHierarchy(type, trackTagHierarchy);
				trackTagHierarchy.removeEmptySubgroups();
			}
			return trackTagHierarchy;
		case Taggable.ARTIST:
			if (artistTagHierarchy == null) {
				artistTagHierarchy = new HierarchyNode(HierarchyNode.GROUP, 0, "root");
				generateTagHierarchy(type, artistTagHierarchy);
				artistTagHierarchy.removeEmptySubgroups();
			}
			return artistTagHierarchy;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * Refresh tag hierarchy for given type
	 * @param type Taggable type
	 */
	public static void refreshTagHierarchy(int type) throws DataSourceException {
		switch (type) {
		case Taggable.ALBUM:
			albumTagHierarchy = null;
			break;
		case Taggable.RECORD:
			recordTagHierarchy = null;
			break;
		case Taggable.TRACK:
			trackTagHierarchy = null;
			break;
		case Taggable.ARTIST:
			artistTagHierarchy = null;
			break;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * Refresh all tag hierarchies.
	 * @throws DataSourceException
	 */
	public static void refreshTagHierarchies() throws DataSourceException {
		refreshTagHierarchy(Taggable.ALBUM);
		refreshTagHierarchy(Taggable.RECORD);
		refreshTagHierarchy(Taggable.TRACK);
		refreshTagHierarchy(Taggable.ARTIST);
	}
	
	/**
	 * Generate tag hierarchy for given Taggable type
	 * @param type Taggable type
	 * @param parentNode the node for which subtree is to be generated
	 * @throws DataSourceException
	 */ 
	private static void generateTagHierarchy(int type, HierarchyNode parentNode) throws DataSourceException {
		log.finest("generateTagHierarchy(" + type + ", \"" + parentNode.getLabel() + "\")");
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
				pstmt = conn.prepareStatement("SELECT tagGroupId, tgr_name FROM TAG_GROUPS " +
						                      "WHERE tgr_parent IS NULL " +
					 				          "ORDER BY tgr_name");
			} else { 
				pstmt = conn.prepareStatement("SELECT tagGroupId, tgr_name FROM TAG_GROUPS " +
											  "WHERE tgr_parent = ? " +
				          					  "ORDER BY tgr_name");
				pstmt.setInt(1, parentNode.getId());
			}
			ResultSet res = pstmt.executeQuery();
			while (res.next()) {
				HierarchyNode node = new HierarchyNode(HierarchyNode.GROUP, res.getInt("tagGroupId"), res.getString("tgr_name"));
				parentNode.addChild(node);
			}
			res.close();
			pstmt.close();
			// for each group add subgroups and tags
			for (Iterator i = parentNode.getChildren().iterator(); i.hasNext();) {
				generateTagHierarchy(type, (HierarchyNode) i.next());
			}
			// get tags with given parent id
			if (parentNode.getId() == 0) {
				// select top-level tags
				pstmt = conn.prepareStatement("SELECT tagId, tag_name FROM TAGS " +
						  					  "WHERE tag_tagGroup IS NULL AND tag_appliesTo = ? " +
						  					  "ORDER BY tag_name");
			} else { 
				pstmt = conn.prepareStatement("SELECT tagId, tag_name FROM TAGS " +
	  					  "WHERE tag_appliesTo = ? AND tag_tagGroup = ? " +
	  					  "ORDER BY tag_name");
				pstmt.setInt(2, parentNode.getId());
			}
			pstmt.setString(1, Tag.itemTypeToString(type));
			res = pstmt.executeQuery();
			while (res.next()) {
				HierarchyNode node = new HierarchyNode(HierarchyNode.ELEMENT, res.getInt("tagId"), res.getString("tag_name"));
				parentNode.addChild(node);
			}
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting tag groups and tags from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}		
	}
	
	/**
	 * @return all tag groups
	 * @throws DataSourceException
	 */
	public static Vector getAllTagGroups() throws DataSourceException {
		log.finest("getAllTagGroups()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TAG_GROUPS " +
					 				  "ORDER BY tgr_name");
			ResultSet res = pstmt.executeQuery();
			Vector tagGroups = new Vector();
			while (res.next()) {
				TagGroup tg = new TagGroup(
						res.getInt("tagGroupId"),
						res.getString("tgr_name"),
						res.getInt("tgr_parent")
					);
				tagGroups.add(tg);
			}
			res.close();
			pstmt.close();
			return tagGroups;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting tag groups from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the tag group with given id from the database.
	 * @param id the id of tag group to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		TagGroup tg = TagGroup.load(id);
		int parentId = tg.getTagGroupId();
		tg.dispose();
		
		// delete the tag group
		Connection conn = null;
		try {
			conn = ds.connect();
			// move all tags from this group to parent group
			log.finer("moving all tags from tag group " + id + " to " + parentId);
			PreparedStatement pstmt =
				conn.prepareStatement("UPDATE TAGS SET tag_tagGroup = ? WHERE tag_tagGroup = ?");
			if (parentId == 0) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, parentId);
			}
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
			// move all tag groups from this group to parent group
			log.finer("moving all tag groups from tag group " + id + " to " + parentId);
			pstmt =	conn.prepareStatement("UPDATE TAG_GROUPS SET tgr_parent = ? WHERE tgr_parent = ?");
			if (parentId == 0) {
				pstmt.setNull(1, Types.INTEGER);
			} else {
				pstmt.setInt(1, parentId);
			}
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
			
			log.finer("deleting tag group " + id);
			pstmt = conn.prepareStatement("DELETE FROM TAG_GROUPS WHERE tagGroupId = ?");
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
			getCurrentDataSource().setModified();
			refreshTagHierarchies();
			notifyObjectDeleted(TagGroup.class, id);
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error deleting tag group " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	
	/**
	 * Add a new tag group to the database.
	 * @throws DataSourceException
	 */
	public static TagGroup create() throws DataSourceException {
		log.finest("create()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO TAG_GROUPS (tgr_name) VALUES (?)");
			pstmt.setString(1, getSafeNewName("TAG_GROUPS", "tgr_name", "New Tag Group"));
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(tagGroupId) AS id FROM TAG_GROUPS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"TagGroup"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			TagGroup tgr = TagGroup.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			refreshTagHierarchies();
			notifyObjectCreated(tgr);
			return tgr;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new tag group", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Load tag group with given id from database
	 * @param id group's id
	 * @return loaded tag group
	 * @throws DataSourceException
	 */
	public static TagGroup load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		TagGroup tg = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TAG_GROUPS WHERE tagGroupId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"TagGroup", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			tg = new TagGroup(
				id,
				res.getString("tgr_name"),
				res.getInt("tgr_parent")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading tag group " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return tg;
	}
	
	/** id of tag group */
	private int id;
	/** name of tag group */
	private String name;
	/** id of parent tag group */
	private int parentId;
	
	/**
	 * @throws DataSourceException
	 */
	private TagGroup(int id, String name, int parentId) throws DataSourceException {
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

		log.finer("saving tag group " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE TAG_GROUPS SET " + 
									  "tgr_name = ?, " +
									  "tgr_parent = ? " +
							          "WHERE tagGroupId = ?");
			pstmt.setString(1, name);
			if (parentId == 0) {
				pstmt.setNull(2, Types.INTEGER);
			} else {
				pstmt.setInt(2, parentId);
			}
			pstmt.setInt(3, id);
			pstmt.executeUpdate();
			clearModified();
			refreshTagHierarchies();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error saving tag group" + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
		delete(id);

	}

	/**
	 * @see com.mmakowski.medley.data.TagGroupElement#getTagGroup()
	 */
	public TagGroup getTagGroup() throws DataSourceException {
		if (parentId == 0) {
			return null;
		} else {
			return TagGroup.load(parentId);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.TagGroupElement#setTagGroupId(int)
	 */
	public void setTagGroupId(int id) throws DataSourceException {
		if (id != NO_GROUP) {
			// check that this does not create cycle.
			TagGroup parent = TagGroup.load(id);
			boolean allowed = parent.canBeSubgroup(this);
			parent.dispose();
			if (!allowed) {
			    log.warning("tag group " + this.id + " can't be a subgroup of tag group " + id);
                throw new DataSourceException(Errors.TAG_GROUP_CANT_BE_A_SUBGROUP, new Object[] {new Integer(this.id), new Integer(id)});
			}
		}
		parentId = id;
		attributeChanged();
	}
	
	/**
	 * Check if given tag group can belong to this group
	 * @param tgroup potential subgroup
	 * @return true if tgroup can belong to this group, false otherwise
	 * @throws DataSourceException
	 */
	public boolean canBeSubgroup(TagGroup tgroup) throws DataSourceException {
		// check that tgroup does not appear up in the tree:
		if (parentId == 0) {
			return true;
		} else if (parentId == tgroup.getId()) {
			return false;
		} else {
			TagGroup parent = TagGroup.load(parentId);
			boolean allowed = parent.canBeSubgroup(tgroup);
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
	 * @see com.mmakowski.medley.data.TagGroupElement#getTagGroupId()
	 */
	public int getTagGroupId() {
		return parentId;
	}
	
}
