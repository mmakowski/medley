/*
 * Created on 05-Jan-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * A custom tag.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $ $Date: 2005/04/24 16:02:45 $
 */
public class Tag extends DataObject implements TagGroupElement {
	// available tag types
	public static final int TYPE_TEXT = 0;
	public static final int TYPE_ENUM = 1;
	public static final int TYPE_LIST = 2;

    /** logger */
    private static final Logger log = Logger.getLogger(Tag.class.getName());
    
	/**
	 * @param type Taggable type
	 * @return all tags for given item type
	 * @throws DataSourceException
	 */
	public static Vector getAllTags(int type) throws DataSourceException {
		log.finest("getAllTags(" + type + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TAGS " +
									  "WHERE tag_appliesTo = ? " +
					 				  "ORDER BY tag_name");
			pstmt.setString(1, itemTypeToString(type));
			ResultSet res = pstmt.executeQuery();
			Vector tags = new Vector();
			while (res.next()) {
				Tag t = new Tag(
						res.getInt("tagId"),
						res.getString("tag_name"),
						stringToItemType(res.getString("tag_appliesTo")),
						stringToTagType(res.getString("tag_type")),
						res.getInt("tag_tagGroup")
				);
				tags.add(t);
			}
			res.close();
			pstmt.close();
			return tags;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting tags from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Delete the tag with given id from the database.
	 * @param id the id of tag to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(Tag.class, "TAGS", "tagId", id);
		TagGroup.refreshTagHierarchies();
	}

	/**
	 * Add a new tag to the database.
	 * @throws DataSourceException
	 */
	public static Tag create(int taggableType) throws DataSourceException {
		log.finest("create(" + taggableType + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO TAGS (tag_name, tag_appliesTo, tag_type) VALUES (?, ?, ?)");
			pstmt.setString(1, getSafeNewName("TAGS", "tag_name", "New Tag"));
			pstmt.setString(2, itemTypeToString(taggableType));
			pstmt.setString(3, tagTypeToString(TYPE_TEXT));
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(tagId) AS id FROM TAGS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Tag"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Tag tag = Tag.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			TagGroup.refreshTagHierarchy(taggableType);
			notifyObjectCreated(tag);
			return tag;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new tag", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @param type tag type value
	 * @return tag type string
	 * @throws DataSourceException
	 */
	private static String tagTypeToString(int type) throws DataSourceException {
		switch (type) {
		case TYPE_TEXT: return "text";
		case TYPE_ENUM: return "enum";
		case TYPE_LIST: return "list";
		default: throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}

	/**
	 * @param str tag type string
	 * @return tag type value for given string
	 * @throws DataSourceException
	 */
	private static int stringToTagType(String str) throws DataSourceException {
		String s = str.toLowerCase().trim();
		if (s.equals("text")) {
			return TYPE_TEXT;
		} else if (s.equals("enum")) {
			return TYPE_ENUM;
		} else if (s.equals("list")) {
			return TYPE_LIST;
		} else {
			throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_STRING, new Object[] {str});
		}
	}
	
	/**
	 * Load tag with given id from database
	 * @param id tag's id 
	 * @return loaded tag
	 * @throws DataSourceException
	 */
	public static Tag load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		Tag t = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TAGS WHERE tagId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Tag", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			t = new Tag(
					id,
					res.getString("tag_name"),
					stringToItemType(res.getString("tag_appliesTo")),
					stringToTagType(res.getString("tag_type")),
					res.getInt("tag_tagGroup")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading tag " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return t;
	}
	
	/** id of tag */
	private int id;
	/** name of tag */
	private String name;
	/** type of Taggable this tag applies to */
	private int appliesTo;
	/** type of tag */
	private int type;
	/** tag group id */
	private int groupId;
	
	/**
	 * @throws DataSourceException
	 */
	private Tag(int id, String name, int appliesTo, int type, int groupId) throws DataSourceException {
		log.finest("Tag(" + id + ",...)");
        this.id = id;
		this.name = name;
		this.appliesTo = appliesTo;
		this.type = type;
		this.groupId = groupId;
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#save()
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		if (!isModified()) {
			return;
		}

		log.finer("saving tag " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE TAGS SET " + 
									  "tag_name = ?, " +
									  "tag_appliesTo = ?, " +
									  "tag_type = ?, " +
									  "tag_tagGroup = ? " +
							          "WHERE tagId = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, itemTypeToString(appliesTo));
			pstmt.setString(3, tagTypeToString(type));
			if (groupId == 0) {
				pstmt.setNull(4, Types.INTEGER);
			} else {
				pstmt.setInt(4, groupId);
			}
			pstmt.setInt(5, id);
			pstmt.executeUpdate();
			clearModified();
			pstmt.close();
			TagGroup.refreshTagHierarchy(type);
			notifyObjectSaved();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving tag " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @return a list of TagValues for enum tag, a list of Strings for list tag or null for text tag
	 * @throws DataSourceException
	 */
	public Vector getValues() throws DataSourceException {
		Vector vals = null;
		switch (type) {
		case TYPE_TEXT: break; // no fixed list of values for this type of tag
		case TYPE_LIST:
			// obtain a list of all the values entered so far for this tag
			DataSource ds = getCurrentDataSource();
			Connection conn = null;
			try {
				conn = ds.connect();
				PreparedStatement pstmt = null;
				switch (appliesTo) {
				case Taggable.ALBUM:
					pstmt = 
						conn.prepareStatement("SELECT DISTINCT ltg_value AS val FROM ALBUM_TAGS " +
								"WHERE ltg_tag = ? " +
								"ORDER BY ltg_value");
					break;
				case Taggable.RECORD:
					pstmt = 
						conn.prepareStatement("SELECT DISTINCT rtg_value AS val FROM RECORD_TAGS " +
								"WHERE rtg_tag = ? " +
								"ORDER BY rtg_value");
					break;
				case Taggable.TRACK:
					pstmt = 
						conn.prepareStatement("SELECT DISTINCT ttg_value AS val FROM TRACK_TAGS " +
								"WHERE ttg_tag = ? " +
								"ORDER BY ttg_value");
					break;
				case Taggable.ARTIST:
					pstmt = 
						conn.prepareStatement("SELECT DISTINCT atg_value AS val FROM ARTIST_TAGS " +
								"WHERE atg_tag = ? " +
								"ORDER BY atg_value");
					break;
				default:
					throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_VALUE, new Object[] {new Integer(appliesTo)});
				}
				pstmt.setInt(1, id);
				ResultSet res = pstmt.executeQuery();
				vals = new Vector();
				while (res.next()) {
					vals.add(res.getString("val"));
				}
				res.close();
				pstmt.close();
			} catch (SQLException ex) {
                log.log(Level.SEVERE, "error saving tag " + id + " to database", ex);
				throw new DataSourceException(ex);
			} finally {
				ds.disconnect(conn);
			}
			break;
		case TYPE_ENUM:
			vals = TagValue.getAllStringsForTag(id);
			break;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_TAG_TYPE_VALUE, new Object[] {new Integer(type)});	
		}
		return vals;
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
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(int type) throws DataSourceException {
		this.type = type;
		attributeChanged();
		notifyAttributeChanged();
	}
	
	/**
	 * @return Returns the appliesTo.
	 */
	public int getAppliesTo() {
		return appliesTo;
	}
	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
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
		if (groupId == 0) {
			return null;
		} else {
			return TagGroup.load(groupId);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.TagGroupElement#setTagGroupId(int)
	 */
	public void setTagGroupId(int id) throws DataSourceException {
		groupId = id;
		TagGroup.refreshTagHierarchy(getType());
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.TagGroupElement#getTagGroupId()
	 */
	public int getTagGroupId() {
		return groupId;
	}

}
