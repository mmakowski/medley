/*
 * Created on 22-Jan-2005
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
 * A rating.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $ $Date: 2005/04/24 19:00:33 $
 */
public class Rating extends DataObject implements RatingGroupElement {
	// TODO: better handling of bounds adjustment when switching rating types
	
	// available tag types
	public static final int TYPE_DECIMAL = 0;
	public static final int TYPE_LETTER = 1;
	public static final int TYPE_PERCENTAGE = 2;
	public static final int TYPE_WHOLE_NUMBER = 3;
	
	// available internal score values
	public static final int INT_MIN_VALUE = -9999;
	public static final int INT_MAX_VALUE = 9999;
	public static final int INT_VALUE_RANGE = INT_MAX_VALUE - INT_MIN_VALUE;

    /** logger */
    private static final Logger log = Logger.getLogger(Rating.class.getName());
    
	/**
	 * @param type Ratable type
	 * @return all tags for given item type
	 * @throws DataSourceException
	 */
	public static Vector getAllRatings(int type) throws DataSourceException {
		log.finest("getAllRatings(" + type + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RATINGS " +
									  "WHERE rat_appliesTo = ? " +
					 				  "ORDER BY rat_name");
			pstmt.setString(1, itemTypeToString(type));
			ResultSet res = pstmt.executeQuery();
			Vector ratings = new Vector();
			while (res.next()) {
				Rating rat = new Rating(
						res.getInt("ratingId"),
						res.getString("rat_name"),
						stringToItemType(res.getString("rat_appliesTo")),
						stringToRatingType(res.getString("rat_type")),
						res.getInt("rat_minValue"),
						res.getInt("rat_maxValue"),
						res.getInt("rat_ratingGroup")
				);
				ratings.add(rat);
			}
			res.close();
			pstmt.close();
			return ratings;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error getting ratings from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * Delete the rating with given id from the database.
	 * @param id the id of rating to be deleted
	 * @throws DataSourceException
	 */
	public static void delete(int id) throws DataSourceException {
		log.finest("delete(" + id + ")");
		delete(Rating.class, "RATINGS", "ratingId", id);
		RatingGroup.refreshRatingHierarchies();
	}

	/**
	 * Add a new tag to the database.
	 * @throws DataSourceException
	 */
	public static Rating create(int ratableType) throws DataSourceException {
		log.finest("create(" + ratableType + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// create a new record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("INSERT INTO RATINGS (rat_name, rat_appliesTo, rat_type, rat_minValue, rat_maxValue) VALUES (?, ?, ?, ?, ?)");
			pstmt.setString(1, getSafeNewName("RATINGS", "rat_name", "New Rating"));
			pstmt.setString(2, itemTypeToString(ratableType));
			pstmt.setString(3, ratingTypeToString(TYPE_DECIMAL));
			pstmt.setInt(4, 0);
			pstmt.setInt(5, 1000);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("SELECT MAX(ratingId) AS id FROM RATINGS");
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Rating"};
				throw new DataSourceException(Errors.DATA_OBJECT_INSERT_NOT_SUCCESSFUL, params); 
			}
			
			Rating rat = Rating.load(res.getInt("id"));
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			getCurrentDataSource().setModified();
			RatingGroup.refreshRatingHierarchy(ratableType);
			notifyObjectCreated(rat);
			return rat;
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error creating new rating", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * @param type rating type value
	 * @return rating type string
	 * @throws DataSourceException
	 */
	private static String ratingTypeToString(int type) throws DataSourceException {
		switch (type) {
		case TYPE_DECIMAL: return "decimal";
		case TYPE_LETTER: return "letter";
		case TYPE_PERCENTAGE: return "percentage";
		case TYPE_WHOLE_NUMBER: return "whole number";
		default: throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}

	/**
	 * @param str rating type string
	 * @return rating type value for given string
	 * @throws DataSourceException
	 */
	private static int stringToRatingType(String str) throws DataSourceException {
		String s = str.toLowerCase().trim();
		if (s.equals("decimal")) {
			return TYPE_DECIMAL;
		} else if (s.equals("letter")) {
			return TYPE_LETTER;
		} else if (s.equals("percentage")) {
			return TYPE_PERCENTAGE;
		} else if (s.equals("whole number")) {
			return TYPE_WHOLE_NUMBER;
		} else {
			throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_STRING, new Object[] {str});
		}
	}
	
	/**
	 * @param type rating type
	 * @return true if min value is fixed for given rating type, false otherwise
	 */
	public static boolean minValueFixed(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return true;
		default: return false;
		}
	}
	
	/**
	 * @param type rating type
	 * @return true if max value is fixed for given rating type, false otherwise
	 */
	public static boolean maxValueFixed(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return true;
		default: return false;
		}
	}

	/**
	 * Load rating with given id from database
	 * @param id rating's id
	 * @return loaded rating
	 * @throws DataSourceException
	 */
	public static Rating load(int id) throws DataSourceException {
		log.finest("load(" + id + ")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		Rating rat = null;
		// read the data from the database
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT * FROM RATINGS WHERE ratingId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				Object[] params = {"Rating", new Integer(id)};
				throw new DataSourceException(Errors.NO_DATA_FOR_ID, params); 
			}
			rat = new Rating(
					id,
					res.getString("rat_name"),
					stringToItemType(res.getString("rat_appliesTo")),
					stringToRatingType(res.getString("rat_type")),
					res.getInt("rat_minValue"),
					res.getInt("rat_maxValue"),
					res.getInt("rat_ratingGroup")
			);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error reading rating " + id + " from database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		return rat;
	}
	
	/** id of rating */
	private int id;
	/** name of rating */
	private String name;
	/** type of Ratable this rating applies to */
	private int appliesTo;
	/** type of rating */
	private int type;
	/** min score */
	private int minValue;
	/** max score */
	private int maxValue;
	/** tag group id */
	private int groupId;
	
	/**
	 * @throws DataSourceException
	 */
	private Rating(int id, String name, int appliesTo, int type,
			int minValue, int maxValue, int groupId) throws DataSourceException {
		log.finest("Rating(" + id + ",...)");
        this.id = id;
		this.name = name;
		this.appliesTo = appliesTo;
		this.type = type;
		this.minValue = minValue;
		this.maxValue = maxValue;
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

		log.finer("saving rating " + id);
		DataSource ds = getCurrentDataSource();
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("UPDATE RATINGS SET " + 
									  "rat_name = ?, " +
									  "rat_appliesTo = ?, " +
									  "rat_type = ?, " +
									  "rat_minValue = ?, " +
									  "rat_maxValue = ?, " +
									  "rat_ratingGroup = ? " +
							          "WHERE ratingId = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, itemTypeToString(appliesTo));
			pstmt.setString(3, ratingTypeToString(type));
			pstmt.setInt(4, minValue);
			pstmt.setInt(5, maxValue);
			if (groupId == 0) {
				pstmt.setNull(6, Types.INTEGER);
			} else {
				pstmt.setInt(6, groupId);
			}
			pstmt.setInt(7, id);
			pstmt.executeUpdate();
			clearModified();
			pstmt.close();
			RatingGroup.refreshRatingHierarchy(type);
			notifyObjectSaved();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error saving rating " + id + " to database", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}
	
	/**
	 * 
	 * @param type rating type
	 * @return default min value for given rating type
	 */
	public int getDefaultMinValue(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return 0;
		case TYPE_LETTER:
			if (minValue < letterToInt('Z')) {
				return letterToInt('Z');
			} else {
				return minValue;
			}
		default: return minValue;
		}
	}
	
	/**
	 * 
	 * @param type rating type
	 * @return default max value for given rating type
	 */
	public int getDefaultMaxValue(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return 100;
		case TYPE_LETTER:
			if (maxValue > letterToInt('A')) {
				return letterToInt('A');
			} else {
				return maxValue;
			}
		default: return maxValue;
		}
	}

	/**
	 * @param type rating type
	 * @return max possible value for given rating type
	 */
	public static int getMaxTypeValue(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return 100;
		case TYPE_LETTER: return letterToInt('A');
		case TYPE_DECIMAL: return 100;
		case TYPE_WHOLE_NUMBER: return 9999;
		default: return 0;
		}
	}

	/**
	 * @param type rating type
	 * @return max possible value for given rating type
	 */
	public static int getMinTypeValue(int type) {
		switch (type) {
		case TYPE_PERCENTAGE: return 0;
		case TYPE_LETTER: return letterToInt('Z');
		case TYPE_DECIMAL: return -100;
		case TYPE_WHOLE_NUMBER: return -9999;
		default: return 0;
		}
	}
	
	/**
	 * Convert given character to integer value
	 * @param letter
	 * @return
	 */
	private static int letterToInt(char letter) {
		return (int) ('Z' - letter);
	}

	/**
	 * Convert given int to character score
	 * @param val
	 * @return
	 */
	private static char intToLetter(int val) {
		return (char) ('Z' - val);
	}
	
	/**
	 * Convert given string score to integer score
	 * @param score string score
	 * @return integer score
	 * @throws DataSourceException
	 */
	public int toIntScore(String score) throws DataSourceException {
		return toIntScore(score, true);
	}
	
	/**
	 * Convert given string score to integer score
	 * @param score string score
	 * @param boundsCheck perform range check?
	 * @return integer score
	 */
	private int toIntScore(String score, boolean boundsCheck) throws DataSourceException {
		switch (type) {
		case TYPE_LETTER:
			if (score.length() != 1) {
				throw new DataSourceException(Errors.CANT_PARSE_SCORE, new Object[] {score});
			}
			char s = score.toUpperCase().charAt(0);
			if (!Character.isLetter(s)) {
				throw new DataSourceException(Errors.SCORE_OUT_OF_RANGE, new Object[] {score});
			}
			int cScore = letterToInt(s);
			if (boundsCheck && (cScore > maxValue || cScore < minValue)) {
				throw new DataSourceException(Errors.SCORE_OUT_OF_RANGE, new Object[] {score});
			}
			return cScore;
		case TYPE_DECIMAL:
			int dScore;
			try {
				dScore = (int) (Double.parseDouble(score) * 100);
			} catch (NumberFormatException ex) {
				throw new DataSourceException(Errors.CANT_PARSE_SCORE, new Object[] {score}, ex);
			}
			if (boundsCheck && (dScore > maxValue || dScore < minValue)) {
				throw new DataSourceException(Errors.SCORE_OUT_OF_RANGE, new Object[] {score});
			}
			return dScore;
		case TYPE_WHOLE_NUMBER:
			int iScore;
			try {
				iScore = Integer.parseInt(score);
			} catch (NumberFormatException ex) {
				throw new DataSourceException(Errors.CANT_PARSE_SCORE, new Object[] {score}, ex);
			}
			if (boundsCheck && (iScore > maxValue || iScore < minValue)) {
				throw new DataSourceException(Errors.SCORE_OUT_OF_RANGE, new Object[] {score});
			}
			return iScore;
		case TYPE_PERCENTAGE:
			if (score.charAt(score.length() - 1) == '%') {
				score = score.substring(0, score.length() - 1);
			}
			int pScore;
			try {
				pScore = Integer.parseInt(score);
			} catch (NumberFormatException ex) {
				throw new DataSourceException(Errors.CANT_PARSE_SCORE, new Object[] {score}, ex);
			}
			if (boundsCheck && (pScore > maxValue || pScore < minValue)) {
				throw new DataSourceException(Errors.SCORE_OUT_OF_RANGE, new Object[] {score});
			}
			return pScore;
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * Convert given integer score to string score
	 * @param score int score
	 * @return string score
	 * @throws DataSourceException 
	 */
	public String toStringScore(int score) throws DataSourceException {
		switch (type) {
		case TYPE_LETTER:
			return String.valueOf(intToLetter(score));
		case TYPE_DECIMAL:
			return String.valueOf((double) score / (double) 100);
		case TYPE_WHOLE_NUMBER:
			return String.valueOf(score);
		case TYPE_PERCENTAGE:
			return String.valueOf(score) + "%";
		default:
			throw new DataSourceException(Errors.UNSUPPORTED_RATING_TYPE_VALUE, new Object[] {new Integer(type)});
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
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * @param type The type to set.
     * @param scaleScores should all the scores in this rating be scaled to new range?
	 */
	public void setType(int type, boolean scaleScores) throws DataSourceException {
		this.type = type;
		verifyBounds(scaleScores);
		attributeChanged();
		notifyAttributeChanged();
	}
	
	/**
	 * Ensure that max and min value match current rating type.
	 * @param scaleScores 
	 */
	private void verifyBounds(boolean scaleScores) throws DataSourceException {
		if (minValue < getMinTypeValue(type)) {
			setMinValue(getDefaultMinValue(type), scaleScores);
		}
		if (maxValue > getMaxTypeValue(type)) {
			setMaxValue(getDefaultMaxValue(type), scaleScores);
		}
	}

    /**
     * Scale all scores in this rating to new score range.
     * @param oldMin old min value
     * @param newMin new min value
     * @param oldMax old max value
     * @param newMax new max value
     */
    private void scaleScores(int oldMin, int newMin, int oldMax, int newMax) throws DataSourceException {
        log.finest("scaleScores(" + oldMin + ", " + newMin + ", " + oldMax + ", " + newMax + ")");
        
        log.finer("scaling scores");
        
        // calculate factor and offset for the linear transformation
        double factor = ((double) (newMax - newMin)) / (oldMax - oldMin);
        double offset = ((double) newMin) - ((double) oldMin) * factor;  
        log.finer("new_score := old_score * " + factor + " + " + offset);    
        
        // find out table name and column prefix for the appropriate score table
        String tableName = null; 
        String prefix = null;
        switch (appliesTo) {
        case Ratable.ALBUM:
            tableName = "ALBUM_RATINGS";
            prefix = "lra";
            break;
        case Ratable.ARTIST:
            tableName = "ARTIST_RATINGS";
            prefix = "ara";
            break;
        case Ratable.RECORD:
            tableName = "RECORD_RATINGS";
            prefix = "rra";
            break;
        case Ratable.TRACK:
            tableName = "TRACK_RATINGS";
            prefix = "tra";
            break;
        default:
            throw new DataSourceException(Errors.UNSUPPORTED_RATABLE_TYPE, new Object[] {new Integer(appliesTo)});
        }
        
        // perform the query
        DataSource ds = getCurrentDataSource();
        Connection conn = null;
        try {
            conn = ds.connect();
            PreparedStatement pstmt = 
                conn.prepareStatement("UPDATE " + tableName + " SET " + 
                                      prefix + "_score = " + prefix + "_score * ? + ? " +
                                      "WHERE " + prefix + "_rating  = ?");
            pstmt.setDouble(1, factor);
            pstmt.setDouble(2, offset);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, "error scaling scores for rating " + id, ex);
            throw new DataSourceException(ex);
        } finally {
            ds.disconnect(conn);
        }
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
	 * @see com.mmakowski.medley.data.RatingGroupElement#getRatingGroup()
	 */
	public RatingGroup getRatingGroup() throws DataSourceException {
		if (groupId == 0) {
			return null;
		} else {
			return RatingGroup.load(groupId);
		}
	}

	/**
	 * @see com.mmakowski.medley.data.RatingGroupElement#setRatingGroupId(int)
	 */
	public void setRatingGroupId(int id) throws DataSourceException {
		groupId = id;
		RatingGroup.refreshRatingHierarchy(getType());
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.TagGroupElement#getTagGroupId()
	 */
	public int getRatingGroupId() {
		return groupId;
	}

	/**
	 * @return Returns the maxValue.
	 */
	public int getMaxValue() {
		return maxValue;
	}
	
	/**
	 * 
	 * @return string representing max value
	 * @throws DataSourceException
	 */
	public String getMaxValueString() throws DataSourceException {
		return toStringScore(maxValue);
	}
	
	/**
	 * @param maxValue The maxValue to set.
	 */
	public void setMaxValue(int maxValue, boolean scaleScores) throws DataSourceException {
        if (scaleScores) {
            scaleScores(this.minValue, this.minValue, this.maxValue, maxValue);
        }
        this.maxValue = maxValue;
		attributeChanged();
	}
	
    /**
	 * 
	 * @param maxValue the string representing max value
	 * @throws DataSourceException
	 */
	public void setMaxValueString(String maxValue, boolean scaleScores) throws DataSourceException {
		setMaxValue(toIntScore(maxValue, false), scaleScores);
	}
	
	/**
	 * @return Returns the minValue.
	 */
	public int getMinValue() {
		return minValue;
	}
	
	/**
	 * 
	 * @return string representing min value
	 * @throws DataSourceException
	 */
	public String getMinValueString() throws DataSourceException {
		return toStringScore(minValue);
	}
	/**
	 * @param minValue The minValue to set.
     * @param scaleScores should all the scores be scaled to new range? 
	 */
	public void setMinValue(int minValue, boolean scaleScores) throws DataSourceException {
        if (scaleScores) {
            scaleScores(this.minValue, minValue, this.maxValue, this.maxValue);
        }
		this.minValue = minValue;
		attributeChanged();
	}
    
	/**
	 * 
	 * @param minValue new min value
     * @param scaleScores should all the scores be scaled to new range? 
	 * @throws DataSourceException
	 */
	public void setMinValueString(String minValue, boolean scaleScores) throws DataSourceException {
		setMinValue(toIntScore(minValue, false), scaleScores);
	}
	
}
