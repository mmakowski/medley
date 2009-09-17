/*
 * Created on 17-Feb-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.io.File;
import com.mmakowski.medley.resources.Errors;

/**
 * An object that provides connection to HSQLDB database.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/08/19 18:01:40 $
 */
class HSQLDBConnector extends JDBCConnector {
	public static final int LATEST_DB_VERSION = 1 * DataSource.MAX_MINOR_VERSIONS + 10;
	protected static final String DB_FILE_NAME = "medley";

    /** logger */
    private static final Logger log = Logger.getLogger(HSQLDBConnector.class.getName());

    /** is this connector open? */
	protected boolean open;
	/** the connection to the database */
	protected Connection conn;
	/** path to database file */
	protected File dbPath;
	/** database format version */
	protected int dbVersion;
	
	public HSQLDBConnector() {
	    log.finest("HsqldbConnector()");
        open = false;
		conn = null;
	}
	
	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#supportsFile()
	 */
	public boolean supportsFile() {
		return true;
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#openFile(com.mmakowski.io.File)
	 */
	public void openFile(File path, int version) throws DataSourceException {
	    log.finest("openFile(" + path + ", " + version + ")");
        dbPath = path; 
		dbVersion = version;
		if (dbVersion != LATEST_DB_VERSION) {
			log.warning("latest database format version: " + LATEST_DB_VERSION + ", this file's database format version: " + dbVersion);
		}
		
		// register the JDBC driver
		String className = "org.hsqldb.jdbcDriver"; 
		try {
			Class.forName(className);
		} catch (ClassNotFoundException ex) {
			log.log(Level.SEVERE, "error loading JDBC driver", ex);
			Object[] args = {className};
			throw new DataSourceException(Errors.CANT_LOAD_JDBC_DRIVER, args, ex);
		}
		Properties props = new Properties();

		String user = "medley";
		String password = "medley";

		props.setProperty("user", user);
		props.setProperty("password", password);

		// open connection
		try {
			conn = 
				java.sql.DriverManager.getConnection("jdbc:hsqldb:file:" + path.getAbsolutePath() + "/" + DB_FILE_NAME,
				 								     props);
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while creating database connection", ex);
			throw new DataSourceException(ex);
		}
		mode = FILE;
		open = true;
	}

	/**
	 * Make sure that connector is open.
	 * @throws DataSourceException
	 */
	protected void checkNotClosed() throws DataSourceException {
		if (!isOpen()) {
			throw new DataSourceException(Errors.CONNECTOR_CLOSED);
		}
		
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#connect()
	 */
	public Connection connect() throws DataSourceException {
		checkNotClosed();
		return conn;
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#disconnect(java.sql.Connection)
	 */
	public void disconnect(Connection conn) throws DataSourceException {
		checkNotClosed();
		// Don't do anything -- keeping persistent connection
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#close()
	 */
	public void close() throws DataSourceException {
	    log.finest("close()");
        checkNotClosed();
		try {
			PreparedStatement pstmt = conn.prepareStatement("SHUTDOWN");
			pstmt.executeUpdate();
			conn.close();
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while closing database connection", ex);
			throw new DataSourceException(Errors.DATA_SOURCE_ERROR_WHILE_CLOSING_CONNECTION, ex);
		}
		open = false;
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#isOpen()
	 */
	public boolean isOpen() {
		return open;
	}
	
	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#preSave()
	 */
	public void preSave() throws DataSourceException {
		log.finest("preSave()");
        super.preSave();
		close();
	}
	
	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#postSave()
	 */
	public void postSave() throws DataSourceException {
		log.finest("postSave()");
        super.postSave();
		openFile(dbPath, dbVersion);
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCConnector#getLatestDatabaseVersion()
	 */
	public int getLatestDatabaseVersion() {
		return LATEST_DB_VERSION;
	}
}
