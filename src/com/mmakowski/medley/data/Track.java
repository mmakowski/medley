/*
 * Created on 30-Dec-2004
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * A track object that uses JDBC data source for accessing
 * stored data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.14 $ $Date: 2005/08/19 18:03:09 $
 */
public class Track extends MusicalItem {

    /** logger */
    private static final Logger log = Logger.getLogger(Track.class.getName());
    
	/**
	 * @return a list of all the tracks in the database
	 */
	public static Vector getAllTracks() throws DataSourceException {
		log.finest("getAllTracks()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT trackId, trk_record, trk_title, trk_number, " +
						"trk_length, trk_removed, trk_int_artistCache, trk_int_artistSortString, " +
						"trk_comments FROM TRACKS t " +
						"LEFT JOIN RECORDS r ON t.trk_record = r.recordId " + 
						"LEFT JOIN ALBUMS a ON r.rec_album = a.albumId " +
                        "WHERE trk_removed IS NULL " +
					 	"ORDER BY trk_title, trk_int_artistSortString, a.alb_originalReleaseYear");
			ResultSet res = pstmt.executeQuery();
			Vector tracks = new Vector();
			while (res.next()) {
				Track trk = new Track(
						res.getInt("trackId"),
						res.getInt("trk_record"),
						res.getString("trk_title"),
						res.getInt("trk_number"),
						res.getTime("trk_length"),
                        res.getTimestamp("trk_removed"),
						res.getString("trk_int_artistCache"),
						res.getString("trk_int_artistSortString"),
						res.getString("trk_comments")
				);
				tracks.add(trk);
			}
			res.close();
			pstmt.close();
			return tracks;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading tracks from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @recordId id of a record
	 * @return a list of all the tracks under given record
	 */
	public static Vector getAllForRecord(int recordId) throws DataSourceException {
		log.finest("getAllForRecord(" + recordId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TRACKS " +
									  "WHERE trk_record = ? AND trk_removed IS NULL " +
					 				  "ORDER BY trk_number, trk_title");
			pstmt.setInt(1, recordId);
			ResultSet res = pstmt.executeQuery();
			Vector tracks = new Vector();
			while (res.next()) {
				Track trk = new Track(
						res.getInt("trackId"),
						res.getInt("trk_record"),
						res.getString("trk_title"),
						res.getInt("trk_number"),
						res.getTime("trk_length"),
                        res.getTimestamp("trk_removed"),
						res.getString("trk_int_artistCache"),
						res.getString("trk_int_artistSortString"),
						res.getString("trk_comments")
				);
				tracks.add(trk);
			}
			res.close();
			pstmt.close();
			return tracks;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading tracks for record " + recordId + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Delete the track with given id from the database.
	 * @param id the id of track to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(Track.class, "TRACKS", "trackId", id);
	}
	
	/**
	 * Add a new track to the database.
	 * @param recordId an id of a record to which the created track belongs
	 * @throws DataSourceException
	 */
	public static Track create(int recordId) throws DataSourceException {
		log.finest("create(" + recordId + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			// by default track number should be max track num for this record + 1
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT MAX(trk_number) AS maxnum FROM TRACKS WHERE trk_record = ?");
			pstmt.setInt(1, recordId);
			ResultSet res = pstmt.executeQuery();
			int num = 1;
			if (res.next()) {
				num = res.getInt("maxnum") + 1;
			}
			res.close();
			pstmt.close();
			// create new record
			pstmt = conn.prepareStatement("INSERT INTO TRACKS (trk_record, trk_number) VALUES (?, ?)");
			pstmt.setInt(1, recordId);
			pstmt.setInt(2, num);
			pstmt.executeUpdate();
			pstmt.close();
			// find out the id of new track
			pstmt = conn.prepareStatement("SELECT MAX(trackId) AS id FROM TRACKS");
			res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Track"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Track trk = Track.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			notifyObjectCreated(trk);
			return trk;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error creating new track", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Load track with given id from database
	 * @param id track id
	 * @return loaded track
	 * @throws DataSourceException
	 */
	public static Track load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		Track trk = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM TRACKS WHERE trackId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Track", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			trk = new Track(
					id,
					res.getInt("trk_record"),
					res.getString("trk_title"),
					res.getInt("trk_number"),
					res.getTime("trk_length"),
                    res.getTimestamp("trk_removed"),
					res.getString("trk_int_artistCache"),
					res.getString("trk_int_artistSortString"),
					res.getString("trk_comments")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error reading track " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return trk;
	}

	private int id;
	private int recordId;
	private String title;
	private int number;
	private Date length;
    private Date removed;
	private String comments;
	
	/**
	 * Construct a track object
	 * @throws DataSourceException
	 */
	public Track(int id, int recordId, String title, int number, Date length, Date removed,
			String artistCache, String artistSortString, String comments) throws DataSourceException {
		this.id = id;
		this.recordId = recordId;
		this.title = title;
		this.number = number;
		this.length = length;
        this.removed = removed;
		this.artistCache = artistCache;
		this.artistSortString = artistSortString;
		this.comments = comments;
		this.taggable = null;
		this.ratable = null;
		this.visible = new VisibleDelegate(getCurrentDataSource(), getTag());
	}

    /**
     * Remove this track from the collection.
     * @param removalDateTime date and time of removal; if <code>null</code>, then current time is used.
     * @throws DataSourceException
     */
    public void remove(Date removalDateTime) throws DataSourceException {
        if (removalDateTime == null) {
            removalDateTime = new Date();
        }
        removed = removalDateTime;
        attributeChanged();
        notifyObjectDeleted(Track.class, id);
    }

    /**
	 * Load tag values
	 * @throws DataSourceException
	 */
	protected void loadTags() throws DataSourceException {
		log.finest("loadTags()");
		DataSource ds = getCurrentDataSource();
		taggable = new TaggableDelegate(getType());
		taggable.loadTags(ds, "ttg", "TRACK", "track", id);
	}

	/**
	 * Load rating scores
	 * @throws DataSourceException
	 */
	protected void loadRatings() throws DataSourceException {
		log.finest("loadRatings()");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		ratable = new RatableDelegate(getType(), ds, "tra", "TRACK", "track", id);
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
		log.finer("saving track " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE TRACKS SET " +
									  "trk_record = ?, " + 
									  "trk_title = ?, " +
									  "trk_number = ?, " +
									  "trk_length = ?, " +
                                      "trk_removed = ?, " +
									  "trk_int_artistCache = ?, " +
									  "trk_int_artistSortString = ?, " +
									  "trk_comments = ? " + 
							          "WHERE trackId = ?");
			pstmt.setInt(1, recordId);
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
			log.log(Level.SEVERE, "error saving track " + id + " to database", ex);
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
		taggable.saveTags(ds, "ttg", "TRACK", "track", id);
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
	 * @see return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the track title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Set the track title.
	 * @param title the track title
	 */
	public void setTitle(String title) throws DataSourceException {
		this.title = title;
		attributeChanged();
	}

	/**
	 * @return track number
	 */
	public int getNumber() {
		return number;
	}
	
	/**
	 * Set the track number
	 * @param num track number
	 */
	public void setNumber(int num) throws DataSourceException {
		this.number = num;
		attributeChanged();
	}


	/**
	 * @return the track length
	 */
	public Date getLength() {
		return length;
	}

	/**
	 * Set the track length.
	 * @param length the trakc length
	 */
	public void setLength(Date length) throws DataSourceException {
		this.length = length;
		attributeChanged();
	}

	/**
	 * @return the track comments
	 */
	public String getComments() {
		return (comments == null) ? "" : comments;
	}
	
	/**
	 * Set the track comments.
	 * @param comments the comments
	 */
	public void setComments(String comments) throws DataSourceException {
		this.comments = comments;
		attributeChanged();
	}

	/**
	 * @return the TRACK type constant
	 */
	public int getType() {
		return MusicalItem.TRACK;
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsUp()
	 */
	public void importArtistsUp() throws DataSourceException {
		// There are no subitems so don't do anything
	}

	/**
	 * @see com.mmakowski.medley.data.MusicalItem#importArtistsDown()
	 */
	public void importArtistsDown() throws DataSourceException {
		Record r = Record.load(recordId);
		importArtists(r);
		r.dispose();
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataObject#deleteSelf()
	 */
	protected void deleteSelf() throws DataSourceException {
		delete(getId());
	}
	
	/**
	 * @return the Record this track belongs to
	 * @throws DataSourceException
	 */
	public Record getRecord() throws DataSourceException {
		return Record.load(recordId);
	}

}
