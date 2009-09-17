/*
 * Created on 13-May-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.resources.Errors;

/**
 * An object representing audible item's audition.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/05/22 00:07:38 $
 */
public class Audition extends DataObject implements Comparable {

    /** logger */
    private static Logger log = Logger.getLogger(ArtistRole.class.getName());

    /**
     * @return a list of all the auditions of given audible
     */
    public static Vector getAllFor(int type, int audibleId) throws DataSourceException {
        log.finest("getAllFor(" + type + ", " + audibleId + ")");
        DataSource ds = getCurrentDataSource();
        
        // read the data from the database
        Connection conn = null;
        try {
            conn = ds.connect();
            PreparedStatement pstmt = null;
            switch (type) {
            case Audible.ALBUM:
                pstmt = conn.prepareStatement("SELECT albumAuditionId AS id, " +
                          "lau_auditionDate AS auditionDate, " +
                          "lau_auditionTime AS auditionTime, " +
                          "lau_recordCount AS subitemCount " +
                          "FROM ALBUM_AUDITIONS " +
                          "WHERE lau_album = ? " +
                          "ORDER BY auditionDate, auditionTime");
                break;
            case Audible.RECORD:
                pstmt = conn.prepareStatement("SELECT recordAuditionId AS id, " +
                        "rau_auditionDate AS auditionDate, " +
                        "rau_auditionTime AS auditionTime, " +
                        "rau_trackCount AS subitemCount " +
                        "FROM RECORD_AUDITIONS " +
                        "WHERE rau_record = ? " +
                        "ORDER BY auditionDate, auditionTime");
                break;
            case Audible.TRACK:
                pstmt = conn.prepareStatement("SELECT trackAuditionId AS id, " +
                        "tau_auditionDate AS auditionDate, " +
                        "tau_auditionTime AS auditionTime, " +
                        "NULL AS subitemCount " +
                        "FROM TRACK_AUDITIONS " +
                        "WHERE tau_track = ? " +
                        "ORDER BY auditionDate, auditionTime");
                break;
            default:
                log.severe("Unknown audition type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
            }
            pstmt.setInt(1, audibleId);
            ResultSet res = pstmt.executeQuery();
            Vector auditions = new Vector();
            while (res.next()) {
                int id = res.getInt("id");
                Date d = res.getDate("auditionDate");
                Time t = res.getTime("auditionTime");
                Date dateTime = combineDateTime(d, t);
                int subitemCount = res.getInt("subitemCount");
                auditions.add(new Audition(type, id, audibleId, dateTime, subitemCount));
            }
            res.close();
            pstmt.close();
            return auditions;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "error getting all auditions of type " + type + " for item " + audibleId + " from database", ex);
            throw new DataSourceException(ex);
        } finally {
            ds.disconnect(conn);
        }
    }

    /**
     * Delete the audition with given type and id from the database.
     * @param type audition type
     * @param id audition id
     * @throws DataSourceException
     */
    public static void delete(int type, int id) throws DataSourceException {
        log.finest("delete(" + type + ", " + id + ")");
        switch (type) {
        case MusicalItem.ALBUM:
            delete(Audition.class, "ALBUM_AUDITIONS", "albumAuditionId", id);
            break;
        case MusicalItem.RECORD:
            delete(Audition.class, "RECORD_AUDITIONS", "recordAuditionId", id);
            break;
        case MusicalItem.TRACK:
            delete(Audition.class, "TRACK_AUDITIONS", "trackAuditionId", id);
            break;
        default:
            log.severe("Unknown audition type " + type);
            throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
        }
    }
    
    /**
     * Add a new audition to the database.
     * @throws DataSourceException
     */
    public static Audition create(int type, int id) throws DataSourceException {
        log.finest("create(" + type + ", " + id +")");
        DataSource ds = getCurrentDataSource();

        // create a new record
        Connection conn = null;
        try {
            conn = ds.connect();
            PreparedStatement pstmt = null;
            ResultSet res = null;
            // insert new audition
            switch (type) {
            case Audible.ALBUM: 
                pstmt = conn.prepareStatement("INSERT INTO ALBUM_AUDITIONS " +
                        "(lau_album, lau_auditionDate, lau_auditionTime, lau_recordCount) " +
                        "VALUES (?, ? ,?, ?)");
                break;
            case Audible.RECORD: 
                pstmt = conn.prepareStatement("INSERT INTO RECORD_AUDITIONS " +
                        "(rau_record, rau_auditionDate, rau_auditionTime, rau_trackCount) " +
                        "VALUES (?, ? ,?, ?)");
                break;
            case Audible.TRACK: 
                pstmt = conn.prepareStatement("INSERT INTO TRACK_AUDITIONS " +
                        "(tau_track, tau_auditionDate, tau_auditionTime) " +
                        "VALUES (?, ? ,?)");
                break;
            default:
                log.severe("Unknown audition type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
            }
            // This will give GMT time
            Calendar cal = Calendar.getInstance();
            long now = cal.getTimeInMillis();
            Time t = new Time(now);
            java.sql.Date d = new java.sql.Date(now);
            
            // find out the default number of subItems
            int subitemCount = 0;
            Vector v = null;
            switch (type) {
            case Audible.ALBUM: 
                v = Record.getAllForAlbum(id);
                subitemCount = v.size();
                DataObject.disposeAll(v);
                break;
            case Audible.RECORD: 
                v = Track.getAllForRecord(id);
                subitemCount = v.size();
                DataObject.disposeAll(v);
                break;
            case Audible.TRACK:
                // no subitems for track
            }

            pstmt.setInt(1, id);
            pstmt.setDate(2, d);
            pstmt.setTime(3, t);
            if (type != Audible.TRACK) {
                pstmt.setInt(4, subitemCount);
            }
            pstmt.executeUpdate();
            pstmt.close();
            
            // obtain the new audition
            switch (type) {
            case Audible.ALBUM:
                pstmt = conn.prepareStatement("SELECT MAX(albumAuditionId) AS id FROM ALBUM_AUDITIONS");
                break;
            case Audible.RECORD:
                pstmt = conn.prepareStatement("SELECT MAX(recordAuditionId) AS id FROM RECORD_AUDITIONS");
                break;
            case Audible.TRACK:
                pstmt = conn.prepareStatement("SELECT MAX(trackAuditionId) AS id FROM TRACK_AUDITIONS");
                break;
            }
            
            res = pstmt.executeQuery();
            if (!res.next()) {
                Object[] params = {"Audition (" + type + ")"};
                throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
            }
            
            Audition aud = Audition.load(type, res.getInt("id"));
            res.close();
            pstmt.close();
            ds.disconnect(conn);
            getCurrentDataSource().setModified();
            return aud;
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new audition of type " + type + " for item " + id, ex);
            throw new DataSourceException(ex);
        } finally {
            ds.disconnect(conn);
        }

    }
    
    /**
     * Load an audition object for given type and id.
     * @param type the type of audition to be loaded
     * @param id the id of the audition to be loaded
     * @throws DataSourceException
     */
    public static Audition load(int type, int id) throws DataSourceException {
        log.finest("load(" + type + ", " + id +")");
        DataSource ds = getCurrentDataSource();
        
        // read the data from the database
        Connection conn = null;
        try {
            conn = ds.connect();
            PreparedStatement pstmt = null; 
            switch (type) {
            case MusicalItem.ALBUM:
                pstmt = conn.prepareStatement("SELECT * FROM ALBUM_AUDITIONS WHERE albumAuditionId = ?");
                break;
            case MusicalItem.RECORD:
                pstmt = conn.prepareStatement("SELECT * FROM RECORD_AUDITIONS WHERE recordAuditionId = ?");
                break;
            case MusicalItem.TRACK:
                pstmt = conn.prepareStatement("SELECT * FROM TRACK_AUDITIONS WHERE trackAuditionId = ?");
                break;
            default:
                log.severe("Unknown audition type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
            }
            
            pstmt.setInt(1, id);
            ResultSet res = pstmt.executeQuery();
            if (!res.next()) {
                Object[] params = {"Audition (" + type + ")", new Integer(id)};
                throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
            }
            // load
            int audibleId = 0;
            Date d = null;
            Time t = null;
            int subitemCount = 0;
            switch (type) {
            case Audible.ALBUM:
                audibleId = res.getInt("lau_album");
                d = res.getDate("lau_auditionDate");
                t = res.getTime("lau_auditionTime");
                subitemCount = res.getInt("lau_recordCount");
                break;
            case Audible.RECORD:
                audibleId = res.getInt("rau_record");
                d = res.getDate("rau_auditionDate");
                t = res.getTime("rau_auditionTime");
                subitemCount = res.getInt("rau_trackCount");
                break;
            case Audible.TRACK:
                audibleId = res.getInt("tau_track");
                d = res.getDate("tau_auditionDate");
                t = res.getTime("tau_auditionTime");
                break;
            default:
                log.severe("Unknown audition type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
            }
            Date dateTime = combineDateTime(d, t);
            res.close();
            pstmt.close();
            return new Audition(type, id, audibleId, dateTime, subitemCount);
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading audition " + id + " of type " + type + " from database", ex);
            throw new DataSourceException(ex);
        } finally {
            ds.disconnect(conn);
        }
    }

    /**
     * @param date a date
     * @param time a time of day (might be <code>null</code>)
     * @return date and time combined into a single Date object
     */
    private static Date combineDateTime(Date date, Time time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        Calendar tcal = Calendar.getInstance();
        tcal.setTime(time);
        cal.add(Calendar.HOUR, tcal.get(Calendar.HOUR));
        cal.add(Calendar.MINUTE, tcal.get(Calendar.MINUTE));
        cal.add(Calendar.SECOND, tcal.get(Calendar.SECOND));
        return cal.getTime();
    }

    
    /** type of audible this audition refers to */
    private int type;
    /** id of this audition */
    private int id;
    /** id of the audible this audition referes to */
    private int audibleId;
    /** date and time of the audition */
    private Date dateTime;
    /** number of sub-items auditioned */
    private int subitemCount;
    
    /**
     * Construct Audition object.
     * @param type audible type
     * @param id audition id
     * @param audibleId id of audible
     * @param dateTime audition's date and time
     * @param subitemCount number of sub-items auditioned.
     */
    Audition(int type, int id, int audibleId, Date dateTime, int subitemCount) throws DataSourceException {
        log.finest("Audition(" + type + ", " + id + ", " + audibleId + ", " + dateTime + ", " + subitemCount + ")");
        this.type = type;
        this.id = id;
        this.audibleId = audibleId;
        this.dateTime = dateTime;
        this.subitemCount = subitemCount;
    }
    
    /**
     * @see com.mmakowski.medley.data.DataObject#save()
     */
    protected void save() throws DataSourceException {
        log.finest("save()");
        DataSource ds = getCurrentDataSource();

        // create a new record
        Connection conn = null;
        try {
            conn = ds.connect();
            PreparedStatement pstmt = null;
            // update audition
            switch (type) {
            case Audible.ALBUM: 
                pstmt = conn.prepareStatement("UPDATE ALBUM_AUDITIONS SET " +
                        "lau_album = ?, lau_auditionDate = ?, lau_auditionTime = ?, lau_recordCount = ? " +
                        "WHERE albumAuditionId = ?");
                break;
            case Audible.RECORD: 
                pstmt = conn.prepareStatement("UPDATE RECORD_AUDITIONS SET " +
                        "rau_record = ?, rau_auditionDate = ?, rau_auditionTime = ?, rau_trackCount = ? " +
                        "WHERE recordAuditionId = ?");
                break;
            case Audible.TRACK: 
                pstmt = conn.prepareStatement("UPDATE TRACK_AUDITIONS SET " +
                        "tau_track = ?, tau_auditionDate = ?, tau_auditionTime = ? " +
                        "WHERE trackAuditionId = ?");
                break;
            default:
                log.severe("Unknown audition type " + type);
                throw new DataSourceException(Errors.UNSUPPORTED_AUDITION_TYPE, new Object[] {new Integer(type)});
            }
            Time t = new Time(dateTime.getTime());
            java.sql.Date d = new java.sql.Date(dateTime.getTime());
            
            pstmt.setInt(1, audibleId);
            pstmt.setDate(2, d);
            pstmt.setTime(3, t);
            if (type != Audible.TRACK) {
                pstmt.setInt(4, subitemCount);
                pstmt.setInt(5, id);
            } else {
                pstmt.setInt(4, id);
            }
            pstmt.executeUpdate();
            pstmt.close();
            
            getCurrentDataSource().setModified();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new audition of type " + type + " for item " + id, ex);
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
        delete(getType(), getId());
    }

    /**
     * @see com.mmakowski.medley.data.DataObject#getId()
     */
    public int getId() {
        return id;
    }

    
    /**
     * @return Returns the audibleId.
     */
    public int getAudibleId() {
        return audibleId;
    }
    /**
     * @param audibleId The audibleId to set.
     */
    public void setAudibleId(int audibleId) {
        this.audibleId = audibleId;
    }
    /**
     * @return Returns the dateTime.
     */
    public Date getDateTime() {
        return dateTime;
    }
    /**
     * @param dateTime The dateTime to set.
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
    /**
     * @return Returns the subitemCount.
     */
    public int getSubitemCount() {
        return subitemCount;
    }
    /**
     * @param subitemCount The subitemCount to set.
     */
    public void setSubitemCount(int subitemCount) {
        this.subitemCount = subitemCount;
    }
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object objAud) {
        // order by dateTime; if dateTime are equal and objects are
        // different say that this object is less than objAud.
        Audition aud = (Audition) objAud;
        int dc = dateTime.compareTo(aud.getDateTime());
        if (dc != 0 ) {
            return dc;
        }
        if (!this.equals(objAud)) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
