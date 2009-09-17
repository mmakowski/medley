/*
 * Created on 16-Jan-2005
 */
package com.mmakowski.medley.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.events.ProgressDelegate;
import com.mmakowski.events.ProgressListener;
import com.mmakowski.medley.resources.Errors;

/**
 * A utility that allows to convert Medley data file to latest version.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/08/19 17:59:57 $
 */
public class FileConverter {
	// task tags
	private static final String TASK_CONVERT = "convert";
	// text data file tags
	private static final String TABLE_HEADING = "TABLE";
	private static final String ENDTABLE_HEADING = "ENDTABLE";
	private static final String COLUMN_HEADING = "COLUMN";
	private static final String ENDCOLUMN_HEADING = "ENDCOLUMN";
	private static final String SEPARATOR = ":";
	private static final String TERMINATOR = "\n";
	private static final String NULL_STR = "%NULL%";
	// data formats
	private static final String TIME_FORMAT = "HH:mm:ss";
	private static final String TIMESTAMP_FORMAT = "yyyyMMDDHHmmss";
	private static final String DATE_FORMAT = "yyyy-MM-DD";
	private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	// read from system properties
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    /** logger */
    private static final Logger log = Logger.getLogger(FileConverter.class.getName());
    
	/**
	 * @return current data source as a FileDataSource.	
	 */	
	private static FileDataSource getCurrentDataSource() throws DataSourceException {
		log.finest("getCurrentDataSource()");
		com.mmakowski.medley.data.DataSource tmpds =
			com.mmakowski.medley.data.DataSource.getNotNull();
		try {
			return (FileDataSource) tmpds;
		} catch (ClassCastException ex) {
			log.log(Level.SEVERE, "current data source is not a FileDataSource data source", ex);
			Object[] params = {tmpds};
			throw new DataSourceException(Errors.DATA_OBJECT_INCOMPATIBLE_WITH_SOURCE, params, ex);
		}
	}

	/** the progress delegate */
	private ProgressDelegate progress;
	/** current line in data file */
	private int curLine;
	
	/**
	 * Construct FileDataSource converter.
	 */
	public FileConverter() {
		progress = new ProgressDelegate(this);
	}

	/**
	 * Add progress listener
	 * @param listener listener to be added
	 */
	public void addProgressListener(ProgressListener listener) {
		progress.addProgressListener(listener);
	}
	
	/**
	 * Remove progress listener
	 * @param listener listener to be removed
	 * @return false if given listener has not been found in the list, true otherwise
	 */
	public boolean removeProgressListener(ProgressListener listener) {
		return progress.removeProgressListener(listener);
	}
	
	/**
	 * Checks if current data source can be converted to latest format version.
	 * @return true if current data source can be converted to latest format version, false otherwise
	 * @throws DataSourceException
	 */
	public boolean canConvert() throws DataSourceException {
		FileDataSource ds = getCurrentDataSource();
		if (ds == null) {
			return false;
		}
		if (ds.getFileVersion() > FileDataSource.LATEST_FILE_VERSION) {
			return false;
		}
		if (ds.getFileVersion() < FileDataSource.LATEST_FILE_VERSION) {
			return true;
		}
		if (!ds.getDatabaseType().equals(FileDataSource.DEFAULT_DB_TYPE)) {
			return true; 
		}
		JDBCConnectorFactory conFac = new JDBCConnectorFactory();
		if (ds.getDatabaseVersion() < conFac.createConnector(FileDataSource.DEFAULT_DB_TYPE).getLatestDatabaseVersion()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Convert current data source to latest version.
	 * @return data source representing newly created file
	 * @throws DataSourceException
	 */
	public FileDataSource convert() throws DataSourceException {
		log.finest("convert()");
		progress.setMinValue(0);
		progress.setMaxValue(100);
		progress.setTag(TASK_CONVERT);
		progress.notifyTaskProgressed(0);
		String fileName = getCurrentDataSource().fileName;
		// create temporary directory
		com.mmakowski.io.File tmpDir = new com.mmakowski.io.File(TEMP_DIR + "/medleyconvert." + Long.toHexString((new Date()).getTime()));
		tmpDir.mkdirs();
		String dataFile = tmpDir.getAbsolutePath() + "/data.txt";
		progress.notifyTaskProgressed(5);
		// export current data source
		exportDatabase(dataFile);
		progress.notifyTaskProgressed(40);
		exportImages(tmpDir);
		progress.notifyTaskProgressed(50);
		String backupFileName = fileName + ".old";
		getCurrentDataSource().save(backupFileName);
		progress.notifyTaskProgressed(55);
		
		// open new data source
		FileDataSource.createNew();
		FileDataSource newFile = getCurrentDataSource(); 
		progress.notifyTaskProgressed(60);
		
		// import data
		importDatabase(dataFile);
		progress.notifyTaskProgressed(75);
		importImages(tmpDir);
		progress.notifyTaskProgressed(90);
		
		// remove temporary dir
		tmpDir.deleteRecursively();
		progress.notifyTaskProgressed(95);
		// save current data source
		newFile.save(fileName);

		progress.notifyTaskProgressed(100);
		return newFile;
	}
	
	/**
	 * Copy images from current data source to temporary directory
	 * @param tmpDir
	 * @throws DataSourceException
	 */
	private void exportImages(File tmpDir) throws DataSourceException {
	    log.finest("exportImages(" + tmpDir + ")");
        File imagesDir = new File(tmpDir.getAbsolutePath() + "/img");
		File sourceDir = new File(getCurrentDataSource().tmpDir + "/img");
		imagesDir.mkdirs();
		File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			File dest = new File(imagesDir.getAbsolutePath() + "/" + files[i].getName());
			try {
				com.mmakowski.io.File.copy(files[i], dest);
			} catch (IOException ex) {
				log.log(Level.SEVERE, "Error while copying file " + files[i].getAbsolutePath() + " to " + dest.getAbsolutePath(), ex);
				throw new DataSourceException(Errors.CANT_COPY_FILE, new Object[] {files[i].getAbsolutePath(), dest.getAbsolutePath()}, ex);
			}
		}
	}

	/**
	 * Copy images from temporary directory to current data source
	 * @param tmpDir
	 * @throws DataSourceException
	 */
	private void importImages(File tmpDir) throws DataSourceException {
	    log.finest("importImages(" + tmpDir + ")");
        File imagesDir = new File(tmpDir.getAbsolutePath() + "/img");
		File targetDir = new File(getCurrentDataSource().tmpDir + "/img");
		targetDir.mkdirs();
		File[] files = imagesDir.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			File dest = new File(targetDir.getAbsolutePath() + "/" + files[i].getName());
			try {
				com.mmakowski.io.File.copy(files[i], dest);
			} catch (IOException ex) {
				log.log(Level.SEVERE, "Error while copying file " + files[i].getAbsolutePath() + " to " + dest.getAbsolutePath(), ex);
				throw new DataSourceException(Errors.CANT_COPY_FILE, new Object[] {files[i].getAbsolutePath(), dest.getAbsolutePath()}, ex);
			}
		}
	}
	
	/**
	 * @param tableName name of database table
	 * @param tableStructure structure of table
	 * @return SQL statement that selects all columns from given table
	 */
	private String buildSelectSQL(String tableName, Hashtable tableStructure) {
		log.finest("buildSelectSQL(\"" + tableName + "\", " + tableStructure + ")");
        StringBuffer sb = new StringBuffer();
		sb.append("SELECT");
		boolean first = true;
		for (Enumeration e = tableStructure.keys(); e.hasMoreElements();) {
			String colName = (String) e.nextElement();
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(" ");
			sb.append(colName);
		}
		sb.append(" FROM ");
		sb.append(tableName);
		return sb.toString();
	}

	/**
	 * Export all rows from given result set to text file
	 * @param res result set
	 * @param tableName table name
	 * @param tableStructure table structure
	 * @param writer text file writer
	 * @throws SQLException
	 * @throws IOException
	 * @throws DataSourceException
	 */
	private void exportRows(ResultSet res, String tableName, Hashtable tableStructure, BufferedWriter writer) throws SQLException, IOException, DataSourceException {
		log.finest("exportRows(" + res + ", \"" + tableName + "\", " + tableStructure + ", " + writer + ")");
        while (res.next()) {
			StringBuffer sb = new StringBuffer();
			sb.append(TABLE_HEADING);
			sb.append(SEPARATOR);
			sb.append(tableName);
			sb.append(TERMINATOR);
			writer.write(sb.toString());
			for (Enumeration e = tableStructure.keys(); e.hasMoreElements();) {
				String colName = (String) e.nextElement();
				int colType = ((Integer) tableStructure.get(colName)).intValue();
				writeColumn(writer, colName, colType, res);
			}
			writer.write(ENDTABLE_HEADING);
			writer.write(TERMINATOR);
		}
	}
	
	/**
	 * Export database table to text file.
	 * @param conn database connection
	 * @param tableName name of database table
	 * @param tableStructure the info about table columns as mapping of column names to column types (from java.sql.Types)
	 * @param writer BufferedWriter that writes to text file
	 * @throws DataSourceException
	 */
	private void exportTable(Connection conn, String tableName, Hashtable tableStructure, BufferedWriter writer) throws DataSourceException {
		log.finest("exportTable(" + conn + ", \"" + tableName + "\", " + tableStructure + ", " + writer + ")");
        try {
			PreparedStatement pstmt = conn.prepareStatement(buildSelectSQL(tableName, tableStructure));
			ResultSet res = pstmt.executeQuery();
			exportRows(res, tableName, tableStructure, writer);
			res.close();
			pstmt.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while exporting table " + tableName, ex);
			throw new DataSourceException(Errors.GENERAL_SQL_ERROR, new Object[] {ex.getMessage()}, ex);
		} catch (IOException ex) {
            log.log(Level.SEVERE, "error while exporting table " + tableName, ex);
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, new Object[] {ex.getMessage()}, ex);
		}
	}
	
	/**
	 * Export database table to text file, taking into account that there
	 * is a foregin key that references this table.
	 * @param conn database connection
	 * @param tableName name of database table
	 * @param tableStructure the info about table columns as mapping of column names to column types (from java.sql.Types)
	 * @param writer BufferedWriter that writes to text file
	 * @param keyColumn name of key column (must be of type int)
	 * @param fkColumn name of foreign key column (must be of type int)
	 * @throws DataSourceException
	 */
	private void exportHierarchyTable(Connection conn, String tableName, Hashtable tableStructure, BufferedWriter writer, String keyColumn, String fkColumn) throws DataSourceException {
        log.finest("exportTable(" + conn + ", \"" + tableName + "\", " + tableStructure + ", " + writer + ",\"" + keyColumn + "\", \"" + fkColumn +"\")");
        Vector goodIds = new Vector();
		String baseSQL = buildSelectSQL(tableName, tableStructure) + " WHERE " + fkColumn;
		try {
			String sql = baseSQL + " IS NULL";
			do {
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet res = pstmt.executeQuery();
				// store obtained ids in goodIds
				while (res.next()) {
					goodIds.add(new Integer(res.getInt(keyColumn)));
				}
				// rewind to beginning (res.first() does not work)
				res.close();
				pstmt.close();
				pstmt = conn.prepareStatement(sql);
				res = pstmt.executeQuery();
				exportRows(res, tableName, tableStructure, writer);
				res.close();
				pstmt.close();
				if (goodIds.isEmpty()) {
					break;
				} else {
					sql = baseSQL + " = " + ((Integer) goodIds.get(0)).toString();
					goodIds.remove(0);
				}
			} while (true); 
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while exporting hierarchy table " + tableName, ex);
			throw new DataSourceException(Errors.GENERAL_SQL_ERROR, new Object[] {ex.getMessage()}, ex);
		} catch (IOException ex) {
            log.log(Level.SEVERE, "error while exporting hierarchy table " + tableName, ex);
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, new Object[] {ex.getMessage()}, ex);
		}
	}
	
	/**
	 * Import data from text file to the database.
	 * @param tmpFileName name of text file to import data from
	 * @throws DataSourceException
	 */
	private void importDatabase(String tmpFileName) throws DataSourceException {
	    log.finest("importDatabase(\"" + tmpFileName + "\")");
        BufferedReader in = null;
	    
		try {
			FileInputStream fis = new FileInputStream(tmpFileName);
		    in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		} catch (IOException ex) {
            log.log(Level.SEVERE, "error while importing database from " + tmpFileName, ex);
			throw new DataSourceException(Errors.CANT_OPEN_FILE_FOR_IMPORT, new Object[] {tmpFileName}, ex);
		}
		
		FileDataSource ds = getCurrentDataSource();
		Connection conn = ds.connect();

		curLine = 0;
		while (importRow(in, conn));
		
		try {
			in.close();
		} catch (IOException ex) {
            log.log(Level.SEVERE, "error while importing database from " + tmpFileName, ex);
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, new Object[] {ex.getMessage()}, ex);
		}
		
		ds.disconnect(conn);
	}
	
	/**
	 * Import one row of data from a text file
	 * @param in
	 * @param conn
	 * @return true if row has been imported, false if end of file has been reached
	 * @throws DataSourceException
	 */
	private boolean importRow(BufferedReader in, Connection conn) throws DataSourceException {
		String line = null;
		String tmp;
		
		try {
			line = in.readLine();
			curLine++;
		} catch (IOException ex) {
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, ex);
		}
		if (line == null) {
			return false;
		}
		int pos = line.indexOf(SEPARATOR);
		tmp = line.substring(0, pos);
		if (!tmp.equals(TABLE_HEADING)) {
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), tmp, TABLE_HEADING});
		}
		String tableName = line.substring(pos + 1, line.length());
		
		// read in columns
		StringBuffer colNames = new StringBuffer();
		StringBuffer colValues = new StringBuffer();
		colNames.append("(");
		colValues.append("(");
		Vector values = new Vector();
		Vector types = new Vector();
		boolean colRead = true;
		boolean first = true;
		do {
			try {
				line = in.readLine();
				curLine++;
			} catch (IOException ex) {
                log.log(Level.SEVERE, "error while importing row", ex);
				throw new DataSourceException(Errors.GENERAL_IO_ERROR, ex);
			}
			if (line.equals(ENDTABLE_HEADING)) {
				// end of row info
				colRead = false;
			} else if (line.equals(COLUMN_HEADING)) {
				if (first) {
					first = false;
				} else {
					colNames.append(",");
					colValues.append(",");
				}
				importColumn(in, colNames, colValues, values, types);
			} else {
			    log.log(Level.SEVERE, "Incorrect format of data file");
                throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), line, COLUMN_HEADING});
			}
		} while (colRead == true);

		colNames.append(")");
		colValues.append(")");
		
		try {
			PreparedStatement pstmt =  
				conn.prepareStatement("INSERT INTO " + tableName + 
						colNames.toString() + " VALUES " +
						colValues.toString());
			for (int i = 0; i < values.size(); i++) {
				setColumnValue(pstmt, i + 1, ((Integer) types.get(i)).intValue(), (String) values.get(i));
			}
			pstmt.executeUpdate();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while importing row", ex);
			throw new DataSourceException(Errors.GENERAL_SQL_ERROR, new Object[] {ex.getMessage()}, ex);
		}
		
		return true;
	}
	
	/**
	 * Set value of given column
	 * @param pstmt
	 * @param i column index
	 * @param type column type
	 * @param value value to put in column
	 */
	private void setColumnValue(PreparedStatement pstmt, int i, int type, String value) throws DataSourceException {
		try {
			if (value.equals(NULL_STR)) {
				pstmt.setNull(i, type);
				return;
			}
			switch (type) {
			case Types.VARCHAR:
				pstmt.setString(i, parseString(value));
				return;
			case Types.INTEGER:
				pstmt.setInt(i, parseInt(value));
				return;
			case Types.TIME:
				pstmt.setTime(i, parseTime(value));
				break;
			case Types.TIMESTAMP:
				pstmt.setTimestamp(i, parseTimestamp(value));
				break;
			case Types.DATE:
				pstmt.setDate(i, new java.sql.Date(parseDate(value).getTime()));
				break;
			default:
				log.log(Level.SEVERE, "Unsupported data type value " + type + " for column " + i);
				throw new DataSourceException(Errors.UNSUPPORTED_DATA_TYPE_VALUE, new Object[] {new Integer(type)});
			}
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "Error setting column value", ex);
		}
	}
	
	/**
	 * Import one data column from a text file 
	 * @param in
	 * @param conn
	 * @param colNames
	 * @param colValues
	 * @param values
	 * @param types
	 * @throws DataSourceException
	 */
	private void importColumn(BufferedReader in, StringBuffer colNames, StringBuffer colValues, Vector values, Vector types) throws DataSourceException {
		String line = null;

		// read in column name
		try {
			line = in.readLine();
			curLine++;
		} catch (IOException ex) {
            log.log(Level.SEVERE, "Error while importing column", ex);
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, ex);
		}
		if (line == null) {
		    log.severe("Incorrect format of data file: expected <identifier>, found <eof>");
            throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), "<eof>", "<identifier>"});
		}
		if (line.trim().length() == 0) {
            log.severe("Incorrect format of data file: expected <identifier>, found empty line");
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), line, "<identifier>"});
		}
		colNames.append(line);
		colValues.append("?");
		// read in column type
		try {
			line = in.readLine();
			curLine++;
		} catch (IOException ex) {
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, ex);
		}
		if (line == null) {
            log.severe("Incorrect format of data file: expected <integer>, found <eof>");
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), "<eof>", "<integer>"});
		}
		try {
			types.add(new Integer(Integer.parseInt(line)));
		} catch (NumberFormatException ex) {
            log.severe("Incorrect format of data file: expected <integer>, found empty line");
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), line, "<integer>"}, ex);
		}
		// read in column value
		StringBuffer val = new StringBuffer();
		boolean endColumn = false;
		boolean first = true;
		do {
			try {
				line = in.readLine();
				curLine++;
			} catch (IOException ex) {
				throw new DataSourceException(Errors.GENERAL_IO_ERROR, ex);
			}
			if (line == null) {
                log.severe("Incorrect format of data file: expected " + ENDCOLUMN_HEADING + ", found <eof>");
				throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), "<eof>", ENDCOLUMN_HEADING});
			} else if (line.equals(ENDCOLUMN_HEADING)) {
				endColumn = true; 
			} else {
				if (first) {
					first = false;
				} else {
					val.append("\n");
				}
				val.append(line);
			}
		} while (!endColumn);
		values.add(val.toString());
	}
	
	/**
	 * Exports all the data from database to SQL statements
	 * @param tmpFileName name of file to which SQL statements should be written
	 * @throws DataSourceException
	 */
	private void exportDatabase(String tmpFileName) throws DataSourceException {
	    log.finest("exportDatabase(\"" + tmpFileName + "\")");
        BufferedWriter writer = null;
		String tmp = null;
	    
		try {
			FileOutputStream fos = new FileOutputStream(tmpFileName);
		    writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
		} catch (IOException ex) {
            log.log(Level.SEVERE, "Error while exporting database to " + tmpFileName, ex);
			throw new DataSourceException(Errors.CANT_CREATE_FILE_FOR_EXPORT, new Object[] {tmpFileName}, ex);
		}
		
		FileDataSource ds = getCurrentDataSource();
		Connection conn = ds.connect();
		
		// choose export procedure based on database format version
		int dbVersion = ds.getDatabaseVersion();
		if (dbVersion <= 10008) {
            log.info("Exporting database using version 1.0 compatible database format");
			exportDatabase10000(conn, writer);
		} else if (dbVersion <= 10010) {
            log.info("Exporting database using version 1.9 compatible database format");
            exportDatabase10009(conn, writer);
        } else {
			log.log(Level.SEVERE, "Unable to export database version " + dbVersion);
			throw new DataSourceException(Errors.CANT_EXPORT_THIS_DB_VERSION, new Object[] {new Integer(dbVersion)});
		}
		
		try {
			writer.flush();
			writer.close();
		} catch (IOException ex) {
            log.log(Level.SEVERE, "Error while exporting database to " + tmpFileName, ex);
			throw new DataSourceException(Errors.GENERAL_IO_ERROR, new Object[] {ex.getMessage()}, ex);
		}
		
		ds.disconnect(conn);
	}
	
	/**
	 * Export database version from 1.0 onwards
	 * @param conn database connection
	 * @param w writer
	 * @throws DataSourceException
	 */
	private void exportDatabase10000(Connection conn, BufferedWriter w) throws DataSourceException {
	    log.finest("exportDatabase10000(" + conn + ", " + w + ")");
        // ALBUMS
		Hashtable schema = new Hashtable();
		schema.put("albumId", new Integer(Types.INTEGER));
		schema.put("alb_title", new Integer(Types.VARCHAR));
		schema.put("alb_originalReleaseYear", new Integer(Types.INTEGER));
		schema.put("alb_releaseYear", new Integer(Types.INTEGER));
		schema.put("alb_label", new Integer(Types.VARCHAR));
		schema.put("alb_length", new Integer(Types.TIME));
		schema.put("alb_removed", new Integer(Types.TIMESTAMP));
		schema.put("alb_int_artistCache", new Integer(Types.VARCHAR));
		schema.put("alb_int_artistSortString", new Integer(Types.VARCHAR));
		schema.put("alb_comments", new Integer(Types.VARCHAR));
		exportTable(conn, "ALBUMS", schema, w);

		// RECORDS
		schema = new Hashtable();
		schema.put("recordId", new Integer(Types.INTEGER));
		schema.put("rec_album", new Integer(Types.INTEGER));
		schema.put("rec_title", new Integer(Types.VARCHAR));
		schema.put("rec_number", new Integer(Types.INTEGER));
		schema.put("rec_length", new Integer(Types.TIME));
		schema.put("rec_removed", new Integer(Types.TIMESTAMP));
		schema.put("rec_int_artistCache", new Integer(Types.VARCHAR));
		schema.put("rec_int_artistSortString", new Integer(Types.VARCHAR));
		schema.put("rec_comments", new Integer(Types.VARCHAR));
		exportTable(conn, "RECORDS", schema, w);

		// TRACKS
		schema = new Hashtable();
		schema.put("trackId", new Integer(Types.INTEGER));
		schema.put("trk_record", new Integer(Types.INTEGER));
		schema.put("trk_title", new Integer(Types.VARCHAR));
		schema.put("trk_number", new Integer(Types.INTEGER));
		schema.put("trk_length", new Integer(Types.TIME));
		schema.put("trk_int_artistCache", new Integer(Types.VARCHAR));
		schema.put("trk_int_artistSortString", new Integer(Types.VARCHAR));
		schema.put("trk_comments", new Integer(Types.VARCHAR));
		exportTable(conn, "TRACKS", schema, w);

		// ARTISTS
		schema = new Hashtable();
		schema.put("artistId", new Integer(Types.INTEGER));
		schema.put("art_name", new Integer(Types.VARCHAR));
		schema.put("art_sortName", new Integer(Types.VARCHAR));
		schema.put("art_type", new Integer(Types.VARCHAR));
		schema.put("art_comments", new Integer(Types.VARCHAR));
		exportTable(conn, "ARTISTS", schema, w);
		
		// ALBUM_ARTISTS
		schema = new Hashtable();
		schema.put("albumArtistId", new Integer(Types.INTEGER));
		schema.put("lar_album", new Integer(Types.INTEGER));
		schema.put("lar_artist", new Integer(Types.INTEGER));
		schema.put("lar_role", new Integer(Types.VARCHAR));
		schema.put("lar_main", new Integer(Types.INTEGER));
		exportTable(conn, "ALBUM_ARTISTS", schema, w);
		
		// RECORD_ARTISTS
		schema = new Hashtable();
		schema.put("recordArtistId", new Integer(Types.INTEGER));
		schema.put("rar_record", new Integer(Types.INTEGER));
		schema.put("rar_artist", new Integer(Types.INTEGER));
		schema.put("rar_role", new Integer(Types.VARCHAR));
		schema.put("rar_main", new Integer(Types.INTEGER));
		exportTable(conn, "RECORD_ARTISTS", schema, w);

		// TRACK_ARTISTS
		schema = new Hashtable();
		schema.put("trackArtistId", new Integer(Types.INTEGER));
		schema.put("tar_track", new Integer(Types.INTEGER));
		schema.put("tar_artist", new Integer(Types.INTEGER));
		schema.put("tar_role", new Integer(Types.VARCHAR));
		schema.put("tar_main", new Integer(Types.INTEGER));
		exportTable(conn, "TRACK_ARTISTS", schema, w);

		// TAG_GROUPS
		schema = new Hashtable();
		schema.put("tagGroupId", new Integer(Types.INTEGER));
		schema.put("tgr_name", new Integer(Types.VARCHAR));
		schema.put("tgr_parent", new Integer(Types.INTEGER));
		exportHierarchyTable(conn, "TAG_GROUPS", schema, w, "tagGroupId", "tgr_parent");
		
		// TAGS
		schema = new Hashtable();
		schema.put("tagId", new Integer(Types.INTEGER));
		schema.put("tag_name", new Integer(Types.VARCHAR));
		schema.put("tag_appliesTo", new Integer(Types.VARCHAR));
		schema.put("tag_type", new Integer(Types.VARCHAR));
		schema.put("tag_tagGroup", new Integer(Types.INTEGER));
		exportTable(conn, "TAGS", schema, w);
		
		// TAG_VALUES
		schema = new Hashtable();
		schema.put("tagValueId", new Integer(Types.INTEGER));
		schema.put("tvl_tag", new Integer(Types.INTEGER));
		schema.put("tvl_value", new Integer(Types.VARCHAR));
		exportTable(conn, "TAG_VALUES", schema, w);
		
		// ALBUM_TAGS
		schema = new Hashtable();
		schema.put("albumTagId", new Integer(Types.INTEGER));
		schema.put("ltg_album", new Integer(Types.INTEGER));
		schema.put("ltg_tag", new Integer(Types.INTEGER));
		schema.put("ltg_value", new Integer(Types.VARCHAR));
		exportTable(conn, "ALBUM_TAGS", schema, w);

		// RECORD_TAGS
		schema = new Hashtable();
		schema.put("recordTagId", new Integer(Types.INTEGER));
		schema.put("rtg_record", new Integer(Types.INTEGER));
		schema.put("rtg_tag", new Integer(Types.INTEGER));
		schema.put("rtg_value", new Integer(Types.VARCHAR));
		exportTable(conn, "RECORD_TAGS", schema, w);
		
		// TRACK_TAGS
		schema = new Hashtable();
		schema.put("trackTagId", new Integer(Types.INTEGER));
		schema.put("ttg_track", new Integer(Types.INTEGER));
		schema.put("ttg_tag", new Integer(Types.INTEGER));
		schema.put("ttg_value", new Integer(Types.VARCHAR));
		exportTable(conn, "TRACK_TAGS", schema, w);

		// ARTIST_TAGS
		schema = new Hashtable();
		schema.put("artistTagId", new Integer(Types.INTEGER));
		schema.put("atg_artist", new Integer(Types.INTEGER));
		schema.put("atg_tag", new Integer(Types.INTEGER));
		schema.put("atg_value", new Integer(Types.VARCHAR));
		exportTable(conn, "ARTIST_TAGS", schema, w);
		
		// RATING_GROUPS
		schema = new Hashtable();
		schema.put("ratingGroupId", new Integer(Types.INTEGER));
		schema.put("rgr_name", new Integer(Types.VARCHAR));
		schema.put("rgr_parent", new Integer(Types.INTEGER));
		exportHierarchyTable(conn, "RATING_GROUPS", schema, w, "ratingGroupId", "rgr_parent");
		
		// RATINGS
		schema = new Hashtable();
		schema.put("ratingId", new Integer(Types.INTEGER));
		schema.put("rat_name", new Integer(Types.VARCHAR));
		schema.put("rat_appliesTo", new Integer(Types.VARCHAR));
		schema.put("rat_type", new Integer(Types.VARCHAR));
		schema.put("rat_minValue", new Integer(Types.INTEGER));
		schema.put("rat_maxValue", new Integer(Types.INTEGER));
		schema.put("rat_ratingGroup", new Integer(Types.INTEGER));
		exportTable(conn, "RATINGS", schema, w);
		
		// ALBUM_RATINGS
		schema = new Hashtable();
		schema.put("albumRatingId", new Integer(Types.INTEGER));
		schema.put("lra_album", new Integer(Types.INTEGER));
		schema.put("lra_rating", new Integer(Types.INTEGER));
		schema.put("lra_dateTime", new Integer(Types.TIMESTAMP));
		schema.put("lra_score", new Integer(Types.INTEGER));
		exportTable(conn, "ALBUM_RATINGS", schema, w);
		
		// RECORD_RATINGS
		schema = new Hashtable();
		schema.put("recordRatingId", new Integer(Types.INTEGER));
		schema.put("rra_record", new Integer(Types.INTEGER));
		schema.put("rra_rating", new Integer(Types.INTEGER));
		schema.put("rra_dateTime", new Integer(Types.TIMESTAMP));
		schema.put("rra_score", new Integer(Types.INTEGER));
		exportTable(conn, "RECORD_RATINGS", schema, w);
		
		// TRACK_RATINGS
		schema = new Hashtable();
		schema.put("trackRatingId", new Integer(Types.INTEGER));
		schema.put("tra_track", new Integer(Types.INTEGER));
		schema.put("tra_rating", new Integer(Types.INTEGER));
		schema.put("tra_dateTime", new Integer(Types.TIMESTAMP));
		schema.put("tra_score", new Integer(Types.INTEGER));
		exportTable(conn, "TRACK_RATINGS", schema, w);
		
		// ARTIST_RATINGS
		schema = new Hashtable();
		schema.put("artistRatingId", new Integer(Types.INTEGER));
		schema.put("ara_artist", new Integer(Types.INTEGER));
		schema.put("ara_rating", new Integer(Types.INTEGER));
		schema.put("ara_dateTime", new Integer(Types.TIMESTAMP));
		schema.put("ara_score", new Integer(Types.INTEGER));
		exportTable(conn, "ARTIST_RATINGS", schema, w);

		// ALBUM_AUDITIONS
		schema = new Hashtable();
		schema.put("albumAuditionId", new Integer(Types.INTEGER));
		schema.put("lau_album", new Integer(Types.INTEGER));
		schema.put("lau_auditionDate", new Integer(Types.DATE));
		schema.put("lau_auditionTime", new Integer(Types.TIME));
		schema.put("lau_recordCount", new Integer(Types.INTEGER));
		exportTable(conn, "ALBUM_AUDITIONS", schema, w);
		
		// RECORD_AUDITIONS
		schema = new Hashtable();
		schema.put("recordAuditionId", new Integer(Types.INTEGER));
		schema.put("rau_record", new Integer(Types.INTEGER));
		schema.put("rau_auditionDate", new Integer(Types.DATE));
		schema.put("rau_auditionTime", new Integer(Types.TIME));
		schema.put("rau_trackCount", new Integer(Types.INTEGER));
		exportTable(conn, "RECORD_AUDITIONS", schema, w);
		
		// TRACK_AUDITIONS
		schema = new Hashtable();
		schema.put("trackAuditionId", new Integer(Types.INTEGER));
		schema.put("tau_track", new Integer(Types.INTEGER));
		schema.put("tau_auditionDate", new Integer(Types.DATE));
		schema.put("tau_auditionTime", new Integer(Types.TIME));
		exportTable(conn, "TRACK_AUDITIONS", schema, w);

		// BORROWERS
		schema = new Hashtable();
		schema.put("borrowerId", new Integer(Types.INTEGER));
		schema.put("bor_firstName", new Integer(Types.VARCHAR));
		schema.put("bor_surname", new Integer(Types.VARCHAR));
		schema.put("bor_email", new Integer(Types.VARCHAR));
		exportTable(conn, "BORROWERS", schema, w);

		// loans table were missing in HSQLDB 1.0 
        FileDataSource ds = getCurrentDataSource();
        if (!(ds.dbType.equals(JDBCConnectorFactory.HSQLDB) && ds.getDatabaseVersion() == 10000)) {
    		// ALBUM_LOANS
    		schema = new Hashtable();
    		schema.put("albumLoanId", new Integer(Types.INTEGER));
    		schema.put("lln_album", new Integer(Types.INTEGER));
    		schema.put("lln_borrower", new Integer(Types.INTEGER));
    		schema.put("lln_dateLent", new Integer(Types.DATE));
    		schema.put("lln_dateReturned", new Integer(Types.DATE));
    		exportTable(conn, "ALBUM_LOANS", schema, w);
    		
    		// RECORD_LOANS
    		schema = new Hashtable();
    		schema.put("recordLoanId", new Integer(Types.INTEGER));
    		schema.put("rln_record", new Integer(Types.INTEGER));
    		schema.put("rln_borrower", new Integer(Types.INTEGER));
    		schema.put("rln_dateLent", new Integer(Types.DATE));
    		schema.put("rln_dateReturned", new Integer(Types.DATE));
    		exportTable(conn, "RECORD_LOANS", schema, w);
        }
    }
	
    /**
     * Export database version from 1.9 onwards
     * @param conn database connection
     * @param w writer
     * @throws DataSourceException
     */
    private void exportDatabase10009(Connection conn, BufferedWriter w) throws DataSourceException {
        log.finest("exportDatabase10009(" + conn + ", " + w + ")");
        // ALBUMS
        Hashtable schema = new Hashtable();
        schema.put("albumId", new Integer(Types.INTEGER));
        schema.put("alb_title", new Integer(Types.VARCHAR));
        schema.put("alb_originalReleaseYear", new Integer(Types.INTEGER));
        schema.put("alb_releaseYear", new Integer(Types.INTEGER));
        schema.put("alb_label", new Integer(Types.VARCHAR));
        schema.put("alb_length", new Integer(Types.TIME));
        schema.put("alb_removed", new Integer(Types.TIMESTAMP));
        schema.put("alb_int_artistCache", new Integer(Types.VARCHAR));
        schema.put("alb_int_artistSortString", new Integer(Types.VARCHAR));
        schema.put("alb_comments", new Integer(Types.VARCHAR));
        exportTable(conn, "ALBUMS", schema, w);

        // RECORDS
        schema = new Hashtable();
        schema.put("recordId", new Integer(Types.INTEGER));
        schema.put("rec_album", new Integer(Types.INTEGER));
        schema.put("rec_title", new Integer(Types.VARCHAR));
        schema.put("rec_number", new Integer(Types.INTEGER));
        schema.put("rec_length", new Integer(Types.TIME));
        schema.put("rec_removed", new Integer(Types.TIMESTAMP));
        schema.put("rec_int_artistCache", new Integer(Types.VARCHAR));
        schema.put("rec_int_artistSortString", new Integer(Types.VARCHAR));
        schema.put("rec_comments", new Integer(Types.VARCHAR));
        exportTable(conn, "RECORDS", schema, w);

        // TRACKS
        schema = new Hashtable();
        schema.put("trackId", new Integer(Types.INTEGER));
        schema.put("trk_record", new Integer(Types.INTEGER));
        schema.put("trk_title", new Integer(Types.VARCHAR));
        schema.put("trk_number", new Integer(Types.INTEGER));
        schema.put("trk_length", new Integer(Types.TIME));
        schema.put("trk_removed", new Integer(Types.TIMESTAMP));
        schema.put("trk_int_artistCache", new Integer(Types.VARCHAR));
        schema.put("trk_int_artistSortString", new Integer(Types.VARCHAR));
        schema.put("trk_comments", new Integer(Types.VARCHAR));
        exportTable(conn, "TRACKS", schema, w);

        // ARTISTS
        schema = new Hashtable();
        schema.put("artistId", new Integer(Types.INTEGER));
        schema.put("art_name", new Integer(Types.VARCHAR));
        schema.put("art_sortName", new Integer(Types.VARCHAR));
        schema.put("art_type", new Integer(Types.VARCHAR));
        schema.put("art_comments", new Integer(Types.VARCHAR));
        exportTable(conn, "ARTISTS", schema, w);
        
        // ALBUM_ARTISTS
        schema = new Hashtable();
        schema.put("albumArtistId", new Integer(Types.INTEGER));
        schema.put("lar_album", new Integer(Types.INTEGER));
        schema.put("lar_artist", new Integer(Types.INTEGER));
        schema.put("lar_role", new Integer(Types.VARCHAR));
        schema.put("lar_main", new Integer(Types.INTEGER));
        exportTable(conn, "ALBUM_ARTISTS", schema, w);
        
        // RECORD_ARTISTS
        schema = new Hashtable();
        schema.put("recordArtistId", new Integer(Types.INTEGER));
        schema.put("rar_record", new Integer(Types.INTEGER));
        schema.put("rar_artist", new Integer(Types.INTEGER));
        schema.put("rar_role", new Integer(Types.VARCHAR));
        schema.put("rar_main", new Integer(Types.INTEGER));
        exportTable(conn, "RECORD_ARTISTS", schema, w);

        // TRACK_ARTISTS
        schema = new Hashtable();
        schema.put("trackArtistId", new Integer(Types.INTEGER));
        schema.put("tar_track", new Integer(Types.INTEGER));
        schema.put("tar_artist", new Integer(Types.INTEGER));
        schema.put("tar_role", new Integer(Types.VARCHAR));
        schema.put("tar_main", new Integer(Types.INTEGER));
        exportTable(conn, "TRACK_ARTISTS", schema, w);

        // TAG_GROUPS
        schema = new Hashtable();
        schema.put("tagGroupId", new Integer(Types.INTEGER));
        schema.put("tgr_name", new Integer(Types.VARCHAR));
        schema.put("tgr_parent", new Integer(Types.INTEGER));
        exportHierarchyTable(conn, "TAG_GROUPS", schema, w, "tagGroupId", "tgr_parent");
        
        // TAGS
        schema = new Hashtable();
        schema.put("tagId", new Integer(Types.INTEGER));
        schema.put("tag_name", new Integer(Types.VARCHAR));
        schema.put("tag_appliesTo", new Integer(Types.VARCHAR));
        schema.put("tag_type", new Integer(Types.VARCHAR));
        schema.put("tag_tagGroup", new Integer(Types.INTEGER));
        exportTable(conn, "TAGS", schema, w);
        
        // TAG_VALUES
        schema = new Hashtable();
        schema.put("tagValueId", new Integer(Types.INTEGER));
        schema.put("tvl_tag", new Integer(Types.INTEGER));
        schema.put("tvl_value", new Integer(Types.VARCHAR));
        exportTable(conn, "TAG_VALUES", schema, w);
        
        // ALBUM_TAGS
        schema = new Hashtable();
        schema.put("albumTagId", new Integer(Types.INTEGER));
        schema.put("ltg_album", new Integer(Types.INTEGER));
        schema.put("ltg_tag", new Integer(Types.INTEGER));
        schema.put("ltg_value", new Integer(Types.VARCHAR));
        exportTable(conn, "ALBUM_TAGS", schema, w);

        // RECORD_TAGS
        schema = new Hashtable();
        schema.put("recordTagId", new Integer(Types.INTEGER));
        schema.put("rtg_record", new Integer(Types.INTEGER));
        schema.put("rtg_tag", new Integer(Types.INTEGER));
        schema.put("rtg_value", new Integer(Types.VARCHAR));
        exportTable(conn, "RECORD_TAGS", schema, w);
        
        // TRACK_TAGS
        schema = new Hashtable();
        schema.put("trackTagId", new Integer(Types.INTEGER));
        schema.put("ttg_track", new Integer(Types.INTEGER));
        schema.put("ttg_tag", new Integer(Types.INTEGER));
        schema.put("ttg_value", new Integer(Types.VARCHAR));
        exportTable(conn, "TRACK_TAGS", schema, w);

        // ARTIST_TAGS
        schema = new Hashtable();
        schema.put("artistTagId", new Integer(Types.INTEGER));
        schema.put("atg_artist", new Integer(Types.INTEGER));
        schema.put("atg_tag", new Integer(Types.INTEGER));
        schema.put("atg_value", new Integer(Types.VARCHAR));
        exportTable(conn, "ARTIST_TAGS", schema, w);
        
        // RATING_GROUPS
        schema = new Hashtable();
        schema.put("ratingGroupId", new Integer(Types.INTEGER));
        schema.put("rgr_name", new Integer(Types.VARCHAR));
        schema.put("rgr_parent", new Integer(Types.INTEGER));
        exportHierarchyTable(conn, "RATING_GROUPS", schema, w, "ratingGroupId", "rgr_parent");
        
        // RATINGS
        schema = new Hashtable();
        schema.put("ratingId", new Integer(Types.INTEGER));
        schema.put("rat_name", new Integer(Types.VARCHAR));
        schema.put("rat_appliesTo", new Integer(Types.VARCHAR));
        schema.put("rat_type", new Integer(Types.VARCHAR));
        schema.put("rat_minValue", new Integer(Types.INTEGER));
        schema.put("rat_maxValue", new Integer(Types.INTEGER));
        schema.put("rat_ratingGroup", new Integer(Types.INTEGER));
        exportTable(conn, "RATINGS", schema, w);
        
        // ALBUM_RATINGS
        schema = new Hashtable();
        schema.put("albumRatingId", new Integer(Types.INTEGER));
        schema.put("lra_album", new Integer(Types.INTEGER));
        schema.put("lra_rating", new Integer(Types.INTEGER));
        schema.put("lra_dateTime", new Integer(Types.TIMESTAMP));
        schema.put("lra_score", new Integer(Types.INTEGER));
        exportTable(conn, "ALBUM_RATINGS", schema, w);
        
        // RECORD_RATINGS
        schema = new Hashtable();
        schema.put("recordRatingId", new Integer(Types.INTEGER));
        schema.put("rra_record", new Integer(Types.INTEGER));
        schema.put("rra_rating", new Integer(Types.INTEGER));
        schema.put("rra_dateTime", new Integer(Types.TIMESTAMP));
        schema.put("rra_score", new Integer(Types.INTEGER));
        exportTable(conn, "RECORD_RATINGS", schema, w);
        
        // TRACK_RATINGS
        schema = new Hashtable();
        schema.put("trackRatingId", new Integer(Types.INTEGER));
        schema.put("tra_track", new Integer(Types.INTEGER));
        schema.put("tra_rating", new Integer(Types.INTEGER));
        schema.put("tra_dateTime", new Integer(Types.TIMESTAMP));
        schema.put("tra_score", new Integer(Types.INTEGER));
        exportTable(conn, "TRACK_RATINGS", schema, w);
        
        // ARTIST_RATINGS
        schema = new Hashtable();
        schema.put("artistRatingId", new Integer(Types.INTEGER));
        schema.put("ara_artist", new Integer(Types.INTEGER));
        schema.put("ara_rating", new Integer(Types.INTEGER));
        schema.put("ara_dateTime", new Integer(Types.TIMESTAMP));
        schema.put("ara_score", new Integer(Types.INTEGER));
        exportTable(conn, "ARTIST_RATINGS", schema, w);

        // ALBUM_AUDITIONS
        schema = new Hashtable();
        schema.put("albumAuditionId", new Integer(Types.INTEGER));
        schema.put("lau_album", new Integer(Types.INTEGER));
        schema.put("lau_auditionDate", new Integer(Types.DATE));
        schema.put("lau_auditionTime", new Integer(Types.TIME));
        schema.put("lau_recordCount", new Integer(Types.INTEGER));
        exportTable(conn, "ALBUM_AUDITIONS", schema, w);
        
        // RECORD_AUDITIONS
        schema = new Hashtable();
        schema.put("recordAuditionId", new Integer(Types.INTEGER));
        schema.put("rau_record", new Integer(Types.INTEGER));
        schema.put("rau_auditionDate", new Integer(Types.DATE));
        schema.put("rau_auditionTime", new Integer(Types.TIME));
        schema.put("rau_trackCount", new Integer(Types.INTEGER));
        exportTable(conn, "RECORD_AUDITIONS", schema, w);
        
        // TRACK_AUDITIONS
        schema = new Hashtable();
        schema.put("trackAuditionId", new Integer(Types.INTEGER));
        schema.put("tau_track", new Integer(Types.INTEGER));
        schema.put("tau_auditionDate", new Integer(Types.DATE));
        schema.put("tau_auditionTime", new Integer(Types.TIME));
        exportTable(conn, "TRACK_AUDITIONS", schema, w);

        // BORROWERS
        schema = new Hashtable();
        schema.put("borrowerId", new Integer(Types.INTEGER));
        schema.put("bor_firstName", new Integer(Types.VARCHAR));
        schema.put("bor_surname", new Integer(Types.VARCHAR));
        schema.put("bor_email", new Integer(Types.VARCHAR));
        exportTable(conn, "BORROWERS", schema, w);
        
        // ALBUM_LOANS
        schema = new Hashtable();
        schema.put("albumLoanId", new Integer(Types.INTEGER));
        schema.put("lln_album", new Integer(Types.INTEGER));
        schema.put("lln_borrower", new Integer(Types.INTEGER));
        schema.put("lln_dateLent", new Integer(Types.DATE));
        schema.put("lln_dateReturned", new Integer(Types.DATE));
        exportTable(conn, "ALBUM_LOANS", schema, w);
        
        // RECORD_LOANS
        schema = new Hashtable();
        schema.put("recordLoanId", new Integer(Types.INTEGER));
        schema.put("rln_record", new Integer(Types.INTEGER));
        schema.put("rln_borrower", new Integer(Types.INTEGER));
        schema.put("rln_dateLent", new Integer(Types.DATE));
        schema.put("rln_dateReturned", new Integer(Types.DATE));
        exportTable(conn, "RECORD_LOANS", schema, w);
    }

    /**
	 * Write column info to text file
	 * @param writer text file writer
	 * @param colName name of column
	 * @param colType type of column
	 * @param res result set from which to fetch data
	 * @throws SQLException
	 * @throws IOException
	 * @throws DataSourceException
	 */
	private void writeColumn(BufferedWriter writer, String colName, int colType, ResultSet res) throws SQLException, IOException, DataSourceException {
		StringBuffer sb = new StringBuffer();
		sb.append(COLUMN_HEADING);
		sb.append(TERMINATOR);
		sb.append(colName);
		sb.append(TERMINATOR);
		sb.append(colType);
		sb.append(TERMINATOR);
		switch (colType) {
		case Types.INTEGER:
			int i = res.getInt(colName);
			if (i == 0 && res.wasNull()) {
				sb.append(NULL_STR);
			} else {
				sb.append(formatInt(res.getInt(colName)));
			}
			break;
		case Types.VARCHAR:
			sb.append(formatString(res.getString(colName)));
			break;
		case Types.TIME:
			sb.append(formatTime(res.getTime(colName)));
			break;
		case Types.TIMESTAMP:
			sb.append(formatTimestamp(res.getTimestamp(colName)));
			break;
		case Types.DATE:
			sb.append(formatDate(res.getDate(colName)));
			break;
		default:
			log.log(Level.SEVERE, "Unsupported data type value " + colType + " for column " + colName);
			throw new DataSourceException(Errors.UNSUPPORTED_DATA_TYPE_VALUE, new Object[] {new Integer(colType)});
		}
		sb.append(TERMINATOR);
		sb.append(ENDCOLUMN_HEADING);
		sb.append(TERMINATOR);
		writer.write(sb.toString());
	}
	
	
	private String formatDate(Date d, String format) {
		if (d == null) {
			return NULL_STR;
		}
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(d);
	}

	private Date parseDate(String str, String format) throws DataSourceException{
		if (str == null) {
			return null;
		} else if (str.equals(NULL_STR)) {
			return null;
		}
		SimpleDateFormat df = new SimpleDateFormat(format);
		try {
			return df.parse(str);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Can't parse \"" + str + "\" as date with format \"" + format + "\"");
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), str, "<" + format + ">"}); 
		}
	}
	
	private String formatInt(int i) {
		return String.valueOf(i);
	}
	
	private int parseInt(String str) throws DataSourceException {
		try {
			return Integer.parseInt(str);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Can't parse \"" + str + "\" as integer");
			throw new DataSourceException(Errors.INCORRECT_FORMAT_OF_DATA_FILE, new Object[] {new Integer(curLine), str, "<integer>"}); 
		}
	}
	
	private String formatString(String str) {
		if (str == null) {
			return NULL_STR;
		}
		return str;
	}
	
	private String parseString(String str) {
		if (str == null) {
			return null;
		} else if (str.equals(NULL_STR)) {
			return null;
		}
		return str;
	}
	
	private String formatTimestamp(Timestamp t) {
		return formatDate(t, TIMESTAMP_FORMAT);
	}

	private Timestamp parseTimestamp(String str) throws DataSourceException {
		return new Timestamp(parseDate(str, TIMESTAMP_FORMAT).getTime());
	}
	
	private String formatTime(Time t) {
		return formatDate(t, TIME_FORMAT);
	}

	private Time parseTime(String str) throws DataSourceException {
		return new Time(parseDate(str, TIME_FORMAT).getTime());
	}

	private String formatDate(Date d) {
		return formatDate(d, DATE_FORMAT);
	}
	
	private Date parseDate(String str) throws DataSourceException {
		return parseDate(str, DATE_FORMAT);
	}
	
}
