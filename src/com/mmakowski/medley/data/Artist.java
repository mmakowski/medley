/*
 * Created on 2004-08-06
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * An artist object that uses JDBC data source for accessing
 * the stored data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.19 $  $Date: 2005/04/24 16:02:44 $
 */
public class Artist extends DataObject implements Visible, Taggable, Ratable {
	
	// possible artist types
	public static final int UNKNOWN = -1;
	public static final int INDIVIDUAL = 0;
	public static final int ENSEMBLE = 1;
	
	// strings for artist types
	private static final String STR_UNKNOWN = "";
	private static final String STR_INDIVIDUAL = "individual";
	private static final String STR_ENSEMBLE = "ensemble";

	/** logger */
	private static final Logger log = Logger.getLogger(Artist.class.getName());
	
	/**
	 * @return a list of all the artists in the database
	 */
	public static Vector getAllArtists() throws DataSourceException {
		log.finest("getAllArtists()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM ARTISTS " +
					 				  "ORDER BY art_sortName");
			ResultSet res = pstmt.executeQuery();
			Vector artists = new Vector();
			while (res.next()) {
				Artist art = new Artist(
						res.getInt("artistId"),
						res.getString("art_name"),
						res.getString("art_sortName"),
						stringToArtistType(res.getString("art_type")),
						res.getString("art_comments")
				);
				artists.add(art);
			}
			res.close();
			pstmt.close();
			return artists;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting all artists from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Delete the artist with given id from the database.
	 * @param id the id of album to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(Artist.class, "ARTISTS", "artistId", id);
	}
	
	/**
	 * Add a new artist to the database.
	 * @throws DataSourceException
	 */
	public static Artist create() throws DataSourceException {
		log.finest("create()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO ARTISTS (art_name, art_sortName, art_type) " +
									  "VALUES (?, ? ,?)");
			String name = getSafeNewName("ARTISTS", "art_name", "New artist");
			pstmt.setString(1, name);
			pstmt.setString(2, name);
			pstmt.setString(3, STR_INDIVIDUAL);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(artistId) AS id FROM ARTISTS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Artist"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Artist art = Artist.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			notifyObjectCreated(art);
			return art;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new artist", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Return type value for given type string
	 * @param typeStr
	 * @return
	 * @throws DataSourceException 
	 */
	private static int stringToArtistType(String typeStr) throws DataSourceException {
		log.finest("stringToArtistType(\"" + typeStr + "\")");
		int type = UNKNOWN;
		typeStr = typeStr.trim();
		if (typeStr.equals(STR_INDIVIDUAL)) {
			type = INDIVIDUAL; 
		} else if (typeStr.equals(STR_ENSEMBLE)) {
			type = ENSEMBLE;
		} else {
			throw new DataSourceException(Errors.UNSUPPORTED_ARTIST_TYPE_STRING, new Object[] {typeStr});
		}
		return type;
	}
	
	/**
	 * Load artist with given id from database.
	 * @param id artist id
	 * @return loaded artist
	 * @throws DataSourceException
	 */
	public static Artist load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		Artist art = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM ARTISTS WHERE artistId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Artist", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			art = new Artist(
					id,
					res.getString("art_name"),
					res.getString("art_sortName"),
					stringToArtistType(res.getString("art_type")),
					res.getString("art_comments")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading artist " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return art;
	}
	
	/**
	 * Load artist with given name from database.
	 * @param name artist name
	 * @return loaded artist
	 * @throws DataSourceException
	 */
	public static Artist load(String name) throws DataSourceException {
		log.finest("load(\"" + name + "\")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		Artist art = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM ARTISTS WHERE art_name = ?");
			pstmt.setString(1, name);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Artist", name};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			art = new Artist(
					res.getInt("artistId"),
					name,
					res.getString("art_sortName"),
					stringToArtistType(res.getString("art_type")),
					res.getString("art_comments")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading artist \"" + name + "\" from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return art;
	}

	private int id;
	private String name;
	private String sortName;
	private int type;
	private String comments;
	/** visible delegate */
	private VisibleDelegate visible;
	/** taggable delegate */
	private TaggableDelegate taggable;
	/** ratable delegate */
	private RatableDelegate ratable;
	/** 
	 * flag saying if cached strings for musical items that depend
	 * on this artist should be recalculated when this item is saved 
	 */
	private boolean forceCacheRecalc;
	
	/**
	 * Construct an artist object
	 * @throws DataSourceException
	 */
	private Artist(int id, String name, String sortName, int type, String comments) throws DataSourceException {
		log.finest("Artist(" + id + ",...)");
		this.id = id;
		this.name = name;
		this.sortName = sortName;
		this.type = type;
		this.comments = comments;
		ratable = null;
		taggable = null;
		visible = new VisibleDelegate(getCurrentDataSource(), getTag());
		forceCacheRecalc = false;
	}

	/**
	 * Load tag values
	 * @throws DataSourceException
	 */
	private void loadTags() throws DataSourceException {
		log.finest("loadTags()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		taggable = new TaggableDelegate(getType());
		taggable.loadTags(ds, "atg", "ARTIST", "artist", id);
	}
	
	/**
	 * Load rating scores
	 * @throws DataSourceException
	 */
	private void loadRatings() throws DataSourceException {
		log.finest("loadRatings()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		ratable = new RatableDelegate(getType(), ds, "ara", "ARTIST", "artist", id);
		ratable.loadRatings();
	}
	
	/**
	 * Update the data in the database with values stored in this object.
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		if (!isModified()) {
			return;
		}

		log.finer("saving artist " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE ARTISTS SET " + 
									  "art_name = ?, " +
									  "art_sortName = ?, " +
									  "art_type = ?, " +
									  "art_comments = ? " + 
							          "WHERE artistId = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, sortName);
			String typeStr = STR_UNKNOWN;
			switch (type) {
			case INDIVIDUAL: typeStr = STR_INDIVIDUAL; break;
			case ENSEMBLE: typeStr = STR_ENSEMBLE; break;
			}
			pstmt.setString(3, typeStr);
			if (comments == null) {
				pstmt.setNull(4, Types.VARCHAR);
			} else {
				pstmt.setString(4, comments);
			}
			pstmt.setInt(5, id);
			pstmt.executeUpdate();
			saveTags();
			saveRatings();
			clearModified();
			pstmt.close();
			if (forceCacheRecalc) {
				refreshItemCache();
			}
			notifyObjectSaved();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error saving artist " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Recalculate artist strings for all items that have this artist as a main artist.
	 */
	private void refreshItemCache() throws DataSourceException {
		log.finest("refreshItemCache()");
		// recalculate albums
		Vector v = getAllMusicalItems(MusicalItem.ALBUM, true, null);
		for (Iterator i = v.iterator(); i.hasNext();) {
			((MusicalItem) i.next()).refreshMainArtistCache();
		}
		disposeAll(v);
		// recalculate records
		v = getAllMusicalItems(MusicalItem.RECORD, true, null);
		for (Iterator i = v.iterator(); i.hasNext();) {
			((MusicalItem) i.next()).refreshMainArtistCache();
		}
		disposeAll(v);
		// recalculate tracks
		v = getAllMusicalItems(MusicalItem.TRACK, true, null);
		for (Iterator i = v.iterator(); i.hasNext();) {
			((MusicalItem) i.next()).refreshMainArtistCache();
		}
		disposeAll(v);
	}

	/**
	 * Save tag values
	 * @throws DataSourceException
	 */
	private void saveTags() throws DataSourceException {
		log.finest("saveTags()");
		if (taggable == null) {
			return;
		}
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		taggable.saveTags(ds, "atg", "ARTIST", "artist", id);
	}
	
	/**
	 * Save rating scores
	 * @throws DataSourceException
	 */
	private void saveRatings() throws DataSourceException {
		log.finest("saveRatings()");
		if (ratable == null) {
			return;
		}
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		ratable.saveRatings();
	}
	
	
	/**
	 * @return the id of this artist
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return Returns the comments.
	 */
	public String getComments() {
		return (comments == null) ? "" : comments;
	}
	
	/**
	 * @param comments The comments to set.
	 */
	public void setComments(String comments) throws DataSourceException {
		this.comments = comments;
		attributeChanged();
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
		forceCacheRecalc = true;
		attributeChanged();
	}
	
	/**
	 * @return Returns the sortName.
	 */
	public String getSortName() {
		return sortName;
	}
	
	/**
	 * @param sortName The sortName to set.
	 */
	public void setSortName(String sortName) throws DataSourceException {
		this.sortName = sortName;
		forceCacheRecalc = true;
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
	}
	
	/**
	 * @param type the type of items to return (ALBUM/RECORD/TRACK)
	 * @param mainOnly return only items where this artist is one of the main artists
	 * @param role if not null the return only albums on which artist plays role specified
	 * @return a list of all musical items for this artist
	 * @throws DataSourceException
	 */
	public Vector getAllMusicalItems(int type, boolean mainOnly, String role) throws DataSourceException {
		log.finest("getAllMusicalItems(" + type + ", " + String.valueOf(mainOnly) + ", " + (role == null ? "null" : "\"" + role + "\"") + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt;
			switch (type) {
			case MusicalItem.ALBUM:
				pstmt = conn.prepareStatement("SELECT albumId AS id FROM ALBUM_ARTISTS " +
		 				  					  "LEFT JOIN ALBUMS ON lar_album = albumId " +
		 				  					  "WHERE lar_artist = ? " +
		 				  					  (mainOnly ? "AND lar_main = 1 " : "") +
		 				  					  (role != null ? "AND lar_role = ? " : "" ) +
						  					  "ORDER BY alb_int_artistSortString, alb_originalReleaseYear, alb_title");
				break;
			case MusicalItem.RECORD:
				pstmt = conn.prepareStatement("SELECT recordId AS id FROM RECORD_ARTISTS " +
		 				  					  "LEFT JOIN RECORDS ON rar_record = recordId " +
		 				  					  "LEFT JOIN ALBUMS ON rec_album = albumId " +
		 				  					  "WHERE rar_artist = ? " +
		 				  					  (mainOnly ? "AND rar_main = 1 " : "") +
		 				  					  (role != null ? "AND rar_role = ? " : "" ) +
						  					  "ORDER BY rec_int_artistSortString, alb_originalReleaseYear, alb_title, rec_number");
				break;
			case MusicalItem.TRACK:
				pstmt = conn.prepareStatement("SELECT trackId AS id FROM TRACK_ARTISTS " +
	  					  "LEFT JOIN TRACKS ON tar_track = trackId " +
	  					  "LEFT JOIN RECORDS ON trk_record = recordId " +
	  					  "LEFT JOIN ALBUMS ON rec_album = albumId " +
	  					  "WHERE tar_artist = ? " +
	  					  (mainOnly ? "AND tar_main = 1 " : "") +
	  					  (role != null ? "AND tar_role = ? " : "" ) +
	  					  "ORDER BY trk_int_artistSortString, trk_title, alb_originalReleaseYear, alb_title");
				break;
			default:
				log.log(Level.SEVERE, "Unsupported musical item type value: " + type);
				return null;
			}
			pstmt.setInt(1, id);
			if (role != null) {
				pstmt.setString(2, role);
			}

			ResultSet res = pstmt.executeQuery();
			Vector items = new Vector();
			while (res.next()) {
				items.add(MusicalItem.load(type, res.getInt("id")));
			}
			res.close();
			pstmt.close();
			return items;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting musical items from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.DataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
		delete(getId());
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#getTag()
	 */
	public String getTag() {
		DecimalFormat df = new DecimalFormat("0000000");
		return "artist-" + df.format(getId());
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#getTaggableType()
	 */
	public int getTaggableType() {
		return Taggable.ARTIST;
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#setTagValue(int, java.lang.String)
	 */
	public void setTagValue(int tagId, String value) throws DataSourceException {
		if (taggable == null) {
			loadTags();
		}
		taggable.setTagValue(tagId, value);
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#getTagValue(int)
	 */
	public String getTagValue(int tagId) throws DataSourceException {
		if (taggable == null) {
			loadTags();
		}
		return taggable.getTagValue(tagId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatableType()
	 */
	public int getRatableType() {
		return Ratable.ARTIST;
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#setRatingScore(int, java.lang.String)
	 */
	public void setRatingScore(int ratingId, String score) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		ratable.setRatingScore(ratingId, score);
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScore(int)
	 */
	public Integer getRatingScore(int ratingId) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		return ratable.getRatingScore(ratingId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScoreString(int)
	 */
	public String getRatingScoreString(int ratingId) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		return ratable.getRatingScoreString(ratingId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#deleteLatestRatingScore(int)
	 */
	public void deleteLatestRatingScore(int ratingId) throws DataSourceException {
		log.finest("deleteLastRatingScore(" + ratingId + ")");
		if (ratable == null) {
			loadRatings();
		}
		ratable.deleteLatestRatingScore(ratingId);
	}
	
	/**
	 * @see com.mmakowski.medley.data.Visible#getImages()
	 */
	public Vector getImages() throws DataSourceException {
		return visible.getImages();
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#addImage(com.mmakowski.medley.data.ImageData)
	 */
	public ImageData addImage(ImageData imageData) throws DataSourceException {
		log.finest("addImage(" + imageData + ")");
		return visible.addImage(imageData);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#removeImage(com.mmakowski.medley.data.ImageData)
	 */
	public void removeImage(ImageData imageData) throws DataSourceException {
		log.finest("removeImage(" + imageData + ")");
		visible.removeImage(imageData);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#addImage(java.lang.String)
	 */
	public ImageData addImage(String path) throws DataSourceException {
		log.finest("addImage(\"" + path + "\")");
		return visible.addImage(path);
	}

}
