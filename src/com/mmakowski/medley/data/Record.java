/*
 * Created on 2004-04-15
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
 * A record object that uses JDBC data source for accessing
 * the stored data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.20 $  $Date: 2005/08/19 18:02:25 $
 */
public class Record extends MusicalItem {
	
    /** logger */
    private static final Logger log = Logger.getLogger(Record.class.getName());

    /**
	 * @return a list of all the records in the database. The list will be
     * sorted by artist sort string, album original release year, album title and record number
	 */
	public static Vector getAllRecords() throws DataSourceException {
		log.finest("getAllRecords()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT recordId, rec_album, rec_title, " +
						"rec_number, rec_length, rec_removed, rec_int_artistCache, " +
						"rec_int_artistSortString, rec_comments " +
						"FROM RECORDS, ALBUMS " +
						"WHERE rec_album = albumId AND rec_removed IS NULL " +
					 	"ORDER BY rec_int_artistSortString, alb_originalReleaseYear, alb_title, rec_number");
			ResultSet res = pstmt.executeQuery();
			Vector records = new Vector();
			while (res.next()) {
				Record rec = new Record(
						res.getInt("recordId"),
						res.getInt("rec_album"),
						res.getString("rec_title"),
						res.getInt("rec_number"),
						res.getTime("rec_length"),
						res.getTimestamp("rec_removed"),
						res.getString("rec_int_artistCache"),
						res.getString("rec_int_artistSortString"),
						res.getString("rec_comments")
				);
				records.add(rec);
			}
			res.close();
			pstmt.close();
			return records;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading records from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @albumId id of an album
	 * @return a list of all the records under given album
	 */
	public static Vector getAllForAlbum(int albumId) throws DataSourceException {
		log.finest("getAllForAlbum(" + albumId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			// NOTE: this ordering causes 0 to be at the very end. Why?
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RECORDS " +
									  "WHERE rec_album = ? AND rec_removed IS NULL " +
					 				  "ORDER BY rec_number");
			pstmt.setInt(1, albumId);
			ResultSet res = pstmt.executeQuery();
			Vector records = new Vector();
			while (res.next()) {
				Record rec = new Record(
						res.getInt("recordId"),
						res.getInt("rec_album"),
						res.getString("rec_title"),
						res.getInt("rec_number"),
						res.getTime("rec_length"),
						res.getTimestamp("rec_removed"),
						res.getString("rec_int_artistCache"),
						res.getString("rec_int_artistSortString"),
						res.getString("rec_comments")
				);
				records.add(rec);
			}
			res.close();
			pstmt.close();
			return records;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading records for album " + albumId + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the record with given id from the database.
	 * @param id the id of record to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(Record.class, "RECORDS", "recordId", id);
	}
	
	/**
	 * Add a new album to the database.
	 * @param albumId an id of an album to which the created record belongs
	 * @throws DataSourceException
	 */
	public static Record create(int albumId) throws DataSourceException {
		log.finest("create(" + albumId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			// by default record number should be max record num for this album + 1
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT MAX(rec_number) AS maxnum FROM RECORDS WHERE rec_album = ?");
			pstmt.setInt(1, albumId);
			ResultSet res = pstmt.executeQuery();
			int num = 1;
			if (res.next()) {
				num = res.getInt("maxnum") + 1;
			}
			res.close();
			pstmt.close();
			// create new record
			pstmt = conn.prepareStatement("INSERT INTO RECORDS (rec_album, rec_number) VALUES (?, ?)");
			pstmt.setInt(1, albumId);
			pstmt.setInt(2, num);
			pstmt.executeUpdate();
			pstmt.close();
			// find out the id of new record
			pstmt = conn.prepareStatement("SELECT MAX(recordId) AS id FROM RECORDS");
			res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Record"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Record rec = Record.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			notifyObjectCreated(rec);
			return rec;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new record", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Load record with given id from database
	 * @param id record id
	 * @return loaded record
	 * @throws DataSourceException
	 */
	public static Record load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
	
		Record rec = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RECORDS WHERE recordId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Record", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			rec = new Record(
					id,
					res.getInt("rec_album"),
					res.getString("rec_title"),
					res.getInt("rec_number"),
					res.getTime("rec_length"),
					res.getTimestamp("rec_removed"),
					res.getString("rec_int_artistCache"),
					res.getString("rec_int_artistSortString"),
					res.getString("rec_comments")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading record " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return rec;
	}

	private int id;
	private int albumId;
	private String title;
	private int number;
	private Date length;
	private Date removed;
	private String comments;
	
	/**
	 * Construct a record object
	 * @throws DataSourceException
	 */
	private Record(int id, int albumId, String title, int number, Date length,
			Date removed, String artistCache, String artistSortString, 
			String comments) throws DataSourceException {
        log.finest("Record(" + id + ",...)");
		this.id = id;
		this.albumId = albumId;
		this.title = title;
		this.number = number;
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
     * Remove this record from the collection.
     * @param removalDateTime date and time of removal; if <code>null</code>, then current time is used.
     * @throws DataSourceException
     */
    public void remove(Date removalDateTime) throws DataSourceException {
        if (removalDateTime == null) {
            removalDateTime = new Date();
        }
        // remove all records for this album
        Vector trks = Track.getAllForRecord(id);
        for (Iterator i = trks.iterator(); i.hasNext();) {
            ((Track) i.next()).remove(removalDateTime);
        }
        disposeAll(trks);
        removed = removalDateTime;
        attributeChanged();
        notifyObjectDeleted(Record.class, id);
    }

    /**
	 * Load tag values
	 * @throws DataSourceException
	 */
	protected void loadTags() throws DataSourceException {
		log.finest("loadTags()");
		DataSource ds = getCurrentDataSource();
		taggable = new TaggableDelegate(getType());
		taggable.loadTags(ds, "rtg", "RECORD", "record", id);
	}
	
	/**
	 * Load rating scores
	 * @throws DataSourceException
	 */
	protected void loadRatings() throws DataSourceException {
		log.finest("loadRatings()");
		DataSource ds = getCurrentDataSource();
		ratable = new RatableDelegate(getType(), ds, "rra", "RECORD", "record", id);
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
		
		refreshMainArtistCache();
		log.finer("saving record " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE RECORDS SET " +
									  "rec_album = ?, " + 
									  "rec_title = ?, " +
									  "rec_number = ?, " +
									  "rec_length = ?, " +
									  "rec_removed = ?, " +
									  "rec_int_artistCache = ?, " +
									  "rec_int_artistSortString = ?, " +
									  "rec_comments = ? " + 
							          "WHERE recordId = ?");
			pstmt.setInt(1, albumId);
			if (title == null) {
				pstmt.setNull(2, Types.VARCHAR);
			} else {
				pstmt.setString(2, title);
			}
			if (number == 0) {
				pstmt.setNull(3, Types.INTEGER);
			} else {
				pstmt.setInt(3, number);
			} 
			if (length == null) {
				pstmt.setNull(4, Types.TIME);
			} else {
				pstmt.setTime(4, new java.sql.Time(length.getTime()));
			}
			if (removed == null) {
				pstmt.setNull(5, Types.TIMESTAMP);
			} else {
				pstmt.setTimestamp(5, new java.sql.Timestamp(removed.getTime()));
			}
			if (artistCache == null) {
				pstmt.setNull(6, Types.VARCHAR);
			} else {
				pstmt.setString(6, artistCache);
			}
			if (artistSortString == null) {
				pstmt.setNull(7, Types.VARCHAR);
			} else {
				pstmt.setString(7, artistSortString);
			}
			if (comments == null) {
				pstmt.setNull(8, Types.VARCHAR);
			} else {
				pstmt.setString(8, comments);
			}
			pstmt.setInt(9, id);
			pstmt.executeUpdate();
			saveTags();
			saveRatings();
			clearModified();
			pstmt.close();
			notifyObjectSaved();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving record " + id + " to database", ex);
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
		DataSource ds = getCurrentDataSource();
		taggable.saveTags(ds, "rtg", "RECORD", "record", id);
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
		DataSource ds = getCurrentDataSource();
		ratable.saveRatings();
	}
	
	
	/**
	 * @see return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the record title
	 */
	public String getTitle() {
		return title == null ? "" : title;
	}

    /**
     * @return record title including album title
     */
    public String getCompositeTitle() throws DataSourceException {
        // TODO: optimise by caching album title in RECORDS table
        Album a = Album.load(albumId);
        String cTitle = a.getTitle();
        a.dispose();
        cTitle += " [" + number + (title == null ? "" : ": " + title) + "]";
        return cTitle;
    }
    
	/**
	 * Set the record title.
	 * @param title the record title
	 */
	public void setTitle(String title) throws DataSourceException {
		this.title = title;
		attributeChanged();
	}

	/**
	 * @return record number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Set the record number
	 * @param num record number
	 */
	public void setNumber(int num) throws DataSourceException {
		this.number = num;
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
	 * Set the album remove date.
	 * @param removed the album remove date
	 */
	public void setRemoved(Date removed) throws DataSourceException {
		this.removed = removed;
		attributeChanged();
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
	 * @return the tracks this record includes
	 */
	public Vector getTracks() throws DataSourceException {
		return Track.getAllForRecord(id);
	}

    /**
     * @return number of tracks on this record
     * @throws DataSourceException
     */
    public int getTrackCount() throws DataSourceException {
        // TODO: optimise by caching the count in RECORDS table
        Vector t = getTracks();
        int count = t.size();
        disposeAll(t);
        return count;
    }
    
    
	/**
	 * @return the RECORD type constant
	 */
	public int getType() {
		return MusicalItem.RECORD;
	}

	/**
	 * @return album to which this record belongs
	 * @throws DataSourceException
	 */
	public Album getAlbum() throws DataSourceException {
		return Album.load(albumId);
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsUp()
	 */
	public void importArtistsUp() throws DataSourceException {
	    log.finest("importArtistsUp()");
        // import artists from tracks
		Vector trks = getTracks();
		importArtists(trks);
		disposeAll(trks);
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsDown()
	 */
	public void importArtistsDown() throws DataSourceException {
	    log.finest("importArtistsDown()");
        Album a = Album.load(albumId);
		importArtists(a);
		a.dispose();
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
	    log.finest("deleteSelf()");
        delete(getId());
	}

}
