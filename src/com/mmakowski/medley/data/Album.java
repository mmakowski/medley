/*
 * Created on 2004-04-09
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * An album object that uses JDBC data source for accessing
 * the stored data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.30 $  $Date: 2005/08/19 17:57:06 $
 */
public class Album extends MusicalItem {
	
    /** logger */
	private static final Logger log = Logger.getLogger(Album.class.getName());
	
	/**
	 * @return a list of all the albums in the database
	 */
	public static Vector getAllAlbums() throws DataSourceException {
		log.finest("getAllAlbums()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM ALBUMS WHERE alb_removed IS NULL " +
					 				  "ORDER BY alb_int_artistSortString, alb_originalReleaseYear, alb_title");
			ResultSet res = pstmt.executeQuery();
			Vector albums = new Vector();
			while (res.next()) {
				Album alb = new Album(
						res.getInt("albumId"),
						res.getString("alb_title"),		
						res.getInt("alb_originalReleaseYear"),
						res.getInt("alb_releaseYear"),
						res.getString("alb_label"),
						res.getTime("alb_length"),
						res.getTimestamp("alb_removed"),
						res.getString("alb_int_artistCache"),
						res.getString("alb_int_artistSortString"),
						res.getString("alb_comments")
					);
				albums.add(alb);
			}
			res.close();
			pstmt.close();
			return albums;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting albums from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @return all the labels entered so far
	 */
	public static Vector getLabels() throws DataSourceException {
		log.finest("getLabels()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT DISTINCT alb_label FROM ALBUMS " +
									  "WHERE alb_label IS NOT NULL " + 
									  "ORDER BY alb_label");
			ResultSet res = pstmt.executeQuery();
			Vector labels = new Vector();
			while (res.next()) {
				labels.add(res.getString("alb_label"));
			}
			res.close();
			pstmt.close();
			return labels;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting labels from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the album with given id from the database.
	 * @param id the id of album to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("Album.delete(" + id + ")");
		delete(Album.class, "ALBUMS", "albumId", id);
	}
	
	/**
	 * Add a new album to the database.
	 * @throws DataSourceException
	 */
	public static Album create() throws DataSourceException {
		log.finest("create()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO ALBUMS (alb_title) VALUES (?)");
			pstmt.setString(1, getSafeNewName("ALBUMS", "alb_title", "New Album"));
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(albumId) AS id FROM ALBUMS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Album"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Album alb = Album.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			notifyObjectCreated(alb);
			return alb;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new album", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Load album with given id from database
	 * @param id album id
	 * @return loaded album
	 * @throws DataSourceException
	 */
	public static Album load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		Album alb = null;
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Album", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			alb = new Album(
					id,
					res.getString("alb_title"),		
					res.getInt("alb_originalReleaseYear"),
					res.getInt("alb_releaseYear"),
					res.getString("alb_label"),
					res.getTime("alb_length"),
					res.getTimestamp("alb_removed"),
					res.getString("alb_int_artistCache"),
					res.getString("alb_int_artistSortString"),
					res.getString("alb_comments")
				);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading album " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return alb;
	}
	
	private int id;
	private String title;
	private int originalReleaseYear;
	private int releaseYear;
	private String label;
	private Date length;
	private Date removed;
	private String comments;
	
	/**
	 * Construct an album object
	 * @throws DataSourceException
	 */
	private Album(int id, String title, int originalReleaseYear,
			int releaseYear, String label, Date length, Date removed, 
			String artistCache, String artistSortString, String comments) throws DataSourceException {
		log.finest("Album(" + id + ",...)");
		this.id = id;
		this.title = title;
		this.originalReleaseYear = originalReleaseYear;
		this.releaseYear = releaseYear;
		this.label = label;
		this.length = length;
		this.removed = removed;
		this.artistCache = artistCache;
		this.artistSortString = artistSortString;
		this.comments = comments;
		this.ratable = null;
		this.taggable = null;
		this.visible = new VisibleDelegate(getCurrentDataSource(), this.getTag());
	}

    /**
     * Remove this album from the collection.
     * @param removalDateTime date and time of removal; if <code>null</code>, then current time is used.
     * @throws DataSourceException
     */
    public void remove(Date removalDateTime) throws DataSourceException {
        if (removalDateTime == null) {
            removalDateTime = new Date();
        }
        // remove all records for this album
        Vector recs = Record.getAllForAlbum(id);
        for (Iterator i = recs.iterator(); i.hasNext();) {
            ((Record) i.next()).remove(removalDateTime);
        }
        disposeAll(recs);
        removed = removalDateTime;
        attributeChanged();
        notifyObjectDeleted(Album.class, id);
    }
    
	/**
	 * Load tag values
	 * @throws DataSourceException
	 */
	protected void loadTags() throws DataSourceException {
		log.finest("loadTags()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		taggable = new TaggableDelegate(getType());
		taggable.loadTags(ds, "ltg", "ALBUM", "album", id);
	}

	/**
	 * Load rating scores
	 * @throws DataSourceException
	 */
	protected void loadRatings() throws DataSourceException {
		log.finest("loadRatings()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		ratable = new RatableDelegate(getType(), ds, "lra", "ALBUM", "album", id);
		ratable.loadRatings();
	}
	
	/**
	 * Update the data in the database with values stored in this object.
	 */
	protected void save() throws DataSourceException {
		log.finest("save()");
		checkDisposed();
		if (!isModified()) {
			return;
		}

		refreshMainArtistCache();
		log.finer("saving album " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE ALBUMS SET " + 
									  "alb_title = ?, " +
									  "alb_originalReleaseYear = ?, " +
									  "alb_releaseYear = ?, " +
									  "alb_label = ?, " +
									  "alb_length = ?, " +
									  "alb_removed = ?, " +
									  "alb_int_artistCache = ?, " +
									  "alb_int_artistSortString = ?, " +
									  "alb_comments = ? " + 
							          "WHERE albumId = ?");
			pstmt.setString(1, title);
			if (originalReleaseYear == 0) {
				pstmt.setNull(2, Types.INTEGER);
			} else {
				pstmt.setInt(2, originalReleaseYear);
			}
			if (releaseYear == 0) {
				pstmt.setNull(3, Types.INTEGER);
			} else {
				pstmt.setInt(3, releaseYear);
			} 
			if (label == null) {
				pstmt.setNull(4, Types.VARCHAR);
			} else {
				pstmt.setString(4, label);
			}
			if (length == null) {
				pstmt.setNull(5, Types.TIME);
			} else {
				pstmt.setTime(5, new java.sql.Time(length.getTime()));
			}
			if (removed == null) {
				pstmt.setNull(6, Types.TIMESTAMP);
			} else {
				pstmt.setTimestamp(6, new java.sql.Timestamp(removed.getTime()));
			}
			if (artistCache == null) {
				pstmt.setNull(7, Types.VARCHAR);
			} else {
				pstmt.setString(7, artistCache);
			}
			if (artistSortString == null) {
				pstmt.setNull(8, Types.VARCHAR);
			} else {
				pstmt.setString(8, artistSortString);
			}
			if (comments == null) {
				pstmt.setNull(9, Types.VARCHAR);
			} else {
				pstmt.setString(9, comments);
			}
			pstmt.setInt(10, id);
			pstmt.executeUpdate();
			saveTags();
			saveRatings();
			clearModified();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error saving album " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
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
		taggable.saveTags(ds, "ltg", "ALBUM", "album", id);
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
		ratable.saveRatings();
	}
	
	/**
	 * @return the id of this album
	 * @throws DataSourceException 
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the album title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the album title.
	 * @param title the album title
	 */
	public void setTitle(String title) throws DataSourceException {
		this.title = title;
		attributeChanged();
	}

	/**
	 * @return original release year
	 */
	public int getOriginalReleaseYear() {
		return originalReleaseYear;
	}
	
	/**
	 * Set the original release year.
	 * @param ory original release year
	 */
	public void setOriginalReleaseYear(int ory) throws DataSourceException {
		this.originalReleaseYear = ory;
		attributeChanged();
	}

	/**
	 * @return album release year
	 */
	public int getReleaseYear() {
		return releaseYear;
	}
	
	/**
	 * Set the release year.
	 * @param ry release year
	 */
	public void setReleaseYear(int ry) throws DataSourceException {
		this.releaseYear = ry;
		attributeChanged();
	}

	/**
	 * @return the album label
	 */
	public String getLabel() {
		return (label == null) ? "" : label;	
	}
	
	/**
	 * Set the album label.
	 * @param label the label
	 */
	public void setLabel(String label) throws DataSourceException {
		this.label = label;
		attributeChanged();
	}

	/**
	 * @return the album length
	 */
	public Date getLength() {
		return length;
	}

	/**
	 * Set the album length.
	 * @param length the album length
	 */
	public void setLength(Date length) throws DataSourceException {
		this.length = length;
		attributeChanged();
	}

	/**
	 * @return the date on which the album was removed from the collection
	 */
	public Date getRemoved() {
		return removed;
	}

	/**
	 * @return the album comments
	 */
	public String getComments() {
		return (comments == null) ? "" : comments;
	}
	
	/**
	 * Set the album comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) throws DataSourceException {
		this.comments = comments;
		attributeChanged();
	}

	/**
	 * @return the records this album includes
	 */
	public Vector getRecords() throws DataSourceException {
		return Record.getAllForAlbum(id);
	}
	
    /**
     * @return number of records in this album
     * @throws DataSourceException
     */
    public int getRecordCount() throws DataSourceException {
        // TODO: optimise by caching the count in ALBUMS table
        Vector r = getRecords();
        int count = r.size();
        disposeAll(r);
        return count;
    }
    
	/**
	 * @return the ALBUM type constant
	 */
	public int getType() {
		return MusicalItem.ALBUM;
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsUp()
	 */
	public void importArtistsUp() throws DataSourceException {
		log.finest("importArtistUp()");
		checkDisposed();
		// import artists from records
		Vector recs = getRecords();
		importArtists(recs);
		disposeAll(recs);
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsDown()
	 */
	public void importArtistsDown() throws DataSourceException {
		log.finest("importArtistDown()");
		checkDisposed();
		// There is no superitem, so don't import anything
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
		log.finest("deleteSelf()");
		checkDisposed();
		delete(getId());
	}

}
