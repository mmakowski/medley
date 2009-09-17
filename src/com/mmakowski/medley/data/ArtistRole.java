/*
 * Created on 2004-08-07
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
 * Artist that has a role connected with Album, Record or Track.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.18 $  $Date: 2005/04/24 16:02:44 $
 */
public class ArtistRole extends DataObject {

	/** logger */
	private static Logger log = Logger.getLogger(ArtistRole.class.getName());

	/**
	 * @return a list of all the roles entered so far
	 */
	public static Vector getAllRoles(int type) throws DataSourceException {
		log.finest("getAllRoles(" + type + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = null;
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT DISTINCT lar_role AS r FROM ALBUM_ARTISTS " +
											  "ORDER BY lar_role");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT DISTINCT rar_role AS r FROM RECORD_ARTISTS " +
											  "ORDER BY rar_role");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT DISTINCT tar_role AS r FROM TRACK_ARTISTS " +
											  "ORDER BY tar_role");
				break;
			}
			ResultSet res = pstmt.executeQuery();
			Vector roles = new Vector();
			while (res.next()) {
				roles.add(res.getString("r"));
			}
			res.close();
			pstmt.close();
			return roles;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting all roles of type " + type + " from database", ex);			
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @return a list of all the artists involved in given album/record/track
	 */
	public static Vector getAllFor(int type, int id) throws DataSourceException {
		log.finest("getAllFor(" + type + ", " + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = null;
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT albumArtistId AS id " +
											  "FROM ALBUM_ARTISTS " +
											  "WHERE lar_album = ? " +
		 				  					  "ORDER BY lar_main DESC, lar_role");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT recordArtistId AS id " +
											  "FROM RECORD_ARTISTS " +
											  "WHERE rar_record = ? " +
		 				  					  "ORDER BY rar_main DESC, rar_role");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT trackArtistId AS id " +
											  "FROM TRACK_ARTISTS " +
											  "WHERE tar_track = ? " +
		 				  					  "ORDER BY tar_main DESC, tar_role");
				break;
			}
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Vector artists = new Vector();
			while (res.next()) {
				artists.add(ArtistRole.load(type, res.getInt("id")));
			}
			res.close();
			pstmt.close();
			return artists;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting all roles of type " + type + " for item " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @return a list of the main artists involved in given album/record/track
	 */
	public static Vector getMainFor(int type, int id) throws DataSourceException {
		log.finest("getMainFor(" + type + ", " + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = null;
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT albumArtistId AS id " +
											  "FROM ALBUM_ARTISTS " +
											  "WHERE lar_album = ? AND lar_main = 1 " +
		 				  					  "ORDER BY lar_role");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT recordArtistId AS id " +
											  "FROM RECORD_ARTISTS " +
											  "WHERE rar_record = ? AND rar_main = 1 " +
		 				  					  "ORDER BY rar_role");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT trackArtistId AS id " +
											  "FROM TRACK_ARTISTS " +
											  "WHERE tar_track = ? AND tar_main = 1 " +
		 				  					  "ORDER BY tar_role");
				break;
			}
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Vector artists = new Vector();
			while (res.next()) {
				artists.add(ArtistRole.load(type, res.getInt("id")));
			}
			res.close();
			pstmt.close();
			return artists;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting main artist roles of type " + type + " for item " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the artist+role with given id from the database.
	 * @param id the id of artist+role to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int type, int id) throws DataSourceException {
		log.finest("delete(" + type + ", " + id + ")");
		switch (type) {
		case MusicalItem.ALBUM:
			delete(ArtistRole.class, "ALBUM_ARTISTS", "albumArtistId", id);
			break;
		case MusicalItem.RECORD:
			delete(ArtistRole.class, "RECORD_ARTISTS", "recordArtistId", id);
			break;
		case MusicalItem.TRACK:
			delete(ArtistRole.class, "TRACK_ARTISTS", "trackArtistId", id);
			break;
		}
	}

	/**
	 * Add a new artist role to the database.
	 * @throws DataSourceException
	 */
	public static ArtistRole create(int type, int id) throws DataSourceException {
		log.finest("create(" + type + ", " + id +")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			// find the first artist in the list
			PreparedStatement pstmt = conn.prepareStatement("SELECT artistId FROM ARTISTS ORDER BY art_sortName");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				throw new DataSourceException(Errors.DATA_OBJECT_NO_INITIAL_VALUE,
											  new Object[] {"Artist", "Artist Role (" + type + ")"});
			}
			int artistId = res.getInt("artistId");
			res.close();
			pstmt.close();
			
			// insert new artist role
			String role = "";
			switch (type) {
			case MusicalItem.ALBUM: 
				pstmt = conn.prepareStatement("INSERT INTO ALBUM_ARTISTS " +
											  "(lar_album, lar_artist, lar_role, lar_main) " +
			  								  "VALUES (?, ? ,?, ?)");
				role = getSafeNewName("ALBUM_ARTISTS", "lar_role", "New role");
				break;
			case MusicalItem.RECORD: 
				pstmt = conn.prepareStatement("INSERT INTO RECORD_ARTISTS " +
											  "(rar_record, rar_artist, rar_role, rar_main) " +
			  								  "VALUES (?, ? ,?, ?)");
				role = getSafeNewName("RECORD_ARTISTS", "rar_role", "New role");
				break;
			case MusicalItem.TRACK: 
				pstmt = conn.prepareStatement("INSERT INTO TRACK_ARTISTS " +
											  "(tar_track, tar_artist, tar_role, tar_main) " +
			  								  "VALUES (?, ? ,?, ?)");
				role = getSafeNewName("TRACK_ARTISTS", "tar_role", "New role");
				break;
			}

			pstmt.setInt(1, id);
			pstmt.setInt(2, artistId);
			pstmt.setString(3, role);
			pstmt.setInt(4, 0);
			pstmt.executeUpdate();
			pstmt.close();
			
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT MAX(albumArtistId) AS id FROM ALBUM_ARTISTS");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT MAX(recordArtistId) AS id FROM RECORD_ARTISTS");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT MAX(trackArtistId) AS id FROM TRACK_ARTISTS");
				break;
			}
			
			res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Artist Role (" + type + ")"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			ArtistRole ar = ArtistRole.load(type, res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			return ar;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new artist role of type " + type + " for item " + id, ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}

	}

	/**
	 * Load an artist role object for given type and id.
	 * @param type the type of Artist Role to be created
	 * @param id the id of the Artist Role to be created
	 * @throws DataSourceException
	 */
	public static ArtistRole load(int type, int id) throws DataSourceException {
		log.finest("load(" + type + ", " + id +")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = null; 
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT * FROM ALBUM_ARTISTS WHERE albumArtistId = ?");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT * FROM RECORD_ARTISTS WHERE recordArtistId = ?");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT * FROM TRACK_ARTISTS WHERE trackArtistId = ?");
				break;
			default:
				log.severe("Unknown artist role type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_ARTIST_ROLE_TYPE, new Object[] {new Integer(type)});
			}
			
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Artist Role (" + type + ")", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			int itemId = 0;
			int artistId = 0;
			String role = null;
			boolean main = false;
			switch (type) {
			case MusicalItem.ALBUM:
				itemId = res.getInt("lar_album");
				artistId = res.getInt("lar_artist");
				role = res.getString("lar_role");
				main = (res.getInt("lar_main") == 1);
				break;
			case MusicalItem.RECORD:
				itemId = res.getInt("rar_record");
				artistId = res.getInt("rar_artist");
				role = res.getString("rar_role");
				main = (res.getInt("rar_main") == 1);
				break;
			case MusicalItem.TRACK:
				itemId = res.getInt("tar_track");
				artistId = res.getInt("tar_artist");
				role = res.getString("tar_role");
				main = (res.getInt("tar_main") == 1);
				break;
			default:
                log.severe("Unknown artist role type " + type);
			    throw new DataSourceException(Errors.UNSUPPORTED_ARTIST_ROLE_TYPE, new Object[] {new Integer(type)});
            }
			res.close();
			pstmt.close();
			return new ArtistRole(type, id, itemId, artistId, role, main);
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading artist role " + id + " of type " + type + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	private int type;
	private int id;
	private int itemId;
	private int artistId;
	private String role;
	private boolean main;
	
	/**
	 * Construct ArtistRole object.
	 * @param type type of artist role
	 * @param id id of artist role
	 * @param itemId the item on which artist plays a role
	 * @param artistId id of artist
	 * @param role description of role
	 * @param main is this one of the main roles in the item?
	 * @throws DataSourceException
	 */
	public ArtistRole(int type, int id, int itemId, int artistId, String role, boolean main) throws DataSourceException {
		log.finest("ArtistRole(" + type + ", " + id + ", " + itemId + ", " + artistId + ", \"" + role + " \", " + String.valueOf(main) + ")");
		this.type = type;
		this.id = id;
		this.itemId = itemId;
		this.artistId = artistId;
		this.role = role;
		this.main = main;
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#save()
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		if (!isModified()) {
			return;
		}

		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = null;
			switch (type) {
			case MusicalItem.ALBUM:
				log.finer("saving album artist " + id);
				pstmt = conn.prepareStatement("UPDATE ALBUM_ARTISTS SET " + 
											  "lar_album = ?, " +
											  "lar_artist = ?, " +
											  "lar_role = ?, " +
											  "lar_main = ? " +
											  "WHERE albumArtistId = ?");
				break;
			case MusicalItem.RECORD:
				log.finer("saving record artist " + id);
				pstmt = conn.prepareStatement("UPDATE RECORD_ARTISTS SET " + 
											  "rar_record = ?, " +
											  "rar_artist = ?, " +
											  "rar_role = ?, " +
											  "rar_main = ? " +
											  "WHERE recordArtistId = ?");
				break;
			case MusicalItem.TRACK:
				log.finer("saving track artist " + id);
				pstmt = conn.prepareStatement("UPDATE TRACK_ARTISTS SET " + 
											  "tar_track = ?, " +
											  "tar_artist = ?, " +
											  "tar_role = ?, " +
											  "tar_main = ? " +
											  "WHERE trackArtistId = ?");
				break;
            default:
                log.severe("Unknown artist role type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_ARTIST_ROLE_TYPE, new Object[] {new Integer(type)});
			}
			pstmt.setInt(1, itemId);
			pstmt.setInt(2, artistId);
			pstmt.setString(3, role);
			pstmt.setInt(4, main ? 1 : 0);
			pstmt.setInt(5, id);
			pstmt.executeUpdate();
			clearModified();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
			// NOTE: this is not too nice...
			// don't report error here as it might be expected UNIQUE violation; this will be chacked by the caller
			// Log.println(Log.VERB_ERROR, "error saving artist role " + id + " of type " + type + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}

		updateItemCache();
		
	}

    /**
     * Update artist cache of the item to which this role belongs.
     * @throws DataSourceException
     */
    private void updateItemCache() throws DataSourceException {
        log.finest("updateItemCache()");
        // update main artist cache for item affected
		MusicalItem item = null;
        boolean updated = false; // has the cache been updated in memory?
        // regenerate cache in affected data objects
		// we need to iterate through a copy of global data objects list, since
		// it is modified during call to refreshMainArtistCache so
		// ConcurrentModificationException would be thrown if we tried to iterate
		// through the "official" list.
		Vector dataObjects = (Vector) getCurrentDataSource().getDataObjects().clone();
        for (Iterator i = dataObjects.iterator(); i.hasNext();) {
			DataObject obj = (DataObject) i.next();
			
			if (obj instanceof MusicalItem) {
				item = (MusicalItem) obj;
				if (item.getType() == type && item.getId() == id) {
					item.refreshMainArtistCache();
					updated = true;
				}
			}
		}
        if (!updated) {
			// no object referring to item affected has been found in memory, so
			// regenerate artist cache in affected database item
			switch (type) {
			case MusicalItem.ALBUM:
				item = Album.load(itemId);
				break;
			case MusicalItem.RECORD:
				item = Record.load(itemId);
				break;
			case MusicalItem.TRACK:
				item = Track.load(itemId); 
				break;
            default:
                log.severe("Unknown artist role type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_ARTIST_ROLE_TYPE, new Object[] {new Integer(type)});
			}
			item.refreshMainArtistCache();
			item.dispose();
		}
    }

	/**
	 * @return Returns the artistId.
	 */
	public int getArtistId() {
		return artistId;
	}

	/**
	 * @return the name of the artist
	 * @throws DataSourceException
	 */
	public String getArtistName() throws DataSourceException {
        Artist a = Artist.load(artistId);
		String n = a.getName();
		a.dispose();
		return n;
	}
	
	/**
	 * @return the sort name of the artist
	 * @throws DataSourceException
	 */
	public String getArtistSortName() throws DataSourceException {
		Artist a = Artist.load(artistId);
		String n = a.getSortName();
		a.dispose();
		return n;
	}
	
	/**
	 * @param artistId The artistId to set.
	 */
	public void setArtistId(int artistId) throws DataSourceException {
		this.artistId = artistId;
		attributeChanged();
	}

	/**
	 * @return Returns the main.
	 */
	public boolean isMain() {
		return main;
	}

	/**
	 * @param main The main to set.
	 */
	public void setMain(boolean main) throws DataSourceException {
		this.main = main;
		attributeChanged();
	}

	/**
	 * @return Returns the role.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role The role to set.
	 */
	public void setRole(String role) throws DataSourceException {
		this.role = role;
		attributeChanged();
	}

	/**
	 * @return Returns the id.
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return Returns the itemId.
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Create a copy of this artist role for given item.
	 * @param newItem item for which copy should be created
	 * @return newly created ArtistRole or null if this role duplicates already existing one
	 * @throws DataSourceException
	 */
	public ArtistRole createCopy(MusicalItem newItem) throws DataSourceException {
	    log.finest("createCopy(" + newItem + ")");
        ArtistRole copy = ArtistRole.create(newItem.getType(), newItem.getId());
		copy.setArtistId(artistId);
		copy.setMain(main);
		copy.setRole(role);
		try {
			copy.save();
		} catch (DataSourceException ex) {
			// NOTE: this is not a very reliable way to handle errors...
			if (ex.getErrorCode() == Errors.GENERAL_SQL_ERROR &&
				(ex.getMessage().indexOf("UNIQUE") >= 0 ||
				 ex.getMessage().indexOf("Unique") >= 0)) {
				// this is a UNIQUE violation, so newly created role is a duplicate
				copy.delete();
				return null;
			} else {
				throw ex;
			}
		}
		return copy;
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
	    log.finest("deleteSelf()");
        delete(getType(), getId());
	}
	
}
