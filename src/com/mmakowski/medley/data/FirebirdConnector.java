/*
 * Created on 16-Feb-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.io.File;
import com.mmakowski.medley.resources.Errors;

/**
 * An object that provides connection to Firebird SQL database.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/04/24 16:02:45 $
 */
class FirebirdConnector extends JDBCConnector {
	public static final int LATEST_DB_VERSION = 1 * DataSource.MAX_MINOR_VERSIONS + 8;
	private static final String DB_FILE_NAME = "medley.gdb";

    /** logger */
    private static final Logger log = Logger.getLogger(FirebirdConnector.class.getName());

    /** is this connector open? */
	private boolean open;
	/** the connection to the database */
	private Connection conn;
	/** database format version */
	private int dbVersion;
	
	public FirebirdConnector() {
        log.finest("FirebirdConnector()");
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
        dbVersion = version;
		if (dbVersion != LATEST_DB_VERSION) {
			log.warning("latest database format version: " + LATEST_DB_VERSION + ", this file's database format version: " + dbVersion);
		}
		
		// register the JDBC driver
		String className = "org.firebirdsql.jdbc.FBDriver"; 
		try {
			Class.forName(className);
		} catch (ClassNotFoundException ex) {
			log.log(Level.SEVERE, "error loading JDBC driver", ex);
			Object[] args = {className};
			throw new DataSourceException(Errors.CANT_LOAD_JDBC_DRIVER, args, ex);
		}
		Properties props = new Properties();

		// There's no password protection of user files so far, so we use
		// the default Firebird sysdba account to acces the database.
		String user = "sysdba";
		String password = "masterkey";

		props.setProperty("user", user);
		props.setProperty("password", password);
		if (version >= 1 * DataSource.MAX_MINOR_VERSIONS + 5) {
			// Unicode supported from version 1.5
			props.setProperty("lc_ctype", "UNICODE_FSS");
		}
		// open connection
		try {
			conn = 
				java.sql.DriverManager.getConnection("jdbc:firebirdsql:embedded:" + path.getAbsolutePath() + "/" + DB_FILE_NAME,
				 								     props);
		} catch (SQLException ex) {
            log.log(Level.SEVERE, "error while creating database connection", ex);
			throw new DataSourceException(ex);
		}
		mode = FILE;
		open = true;
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
	 * @see com.mmakowski.medley.data.JDBCConnector#getLatestDatabaseVersion()
	 */
	public int getLatestDatabaseVersion() {
		return LATEST_DB_VERSION;
	}

}
