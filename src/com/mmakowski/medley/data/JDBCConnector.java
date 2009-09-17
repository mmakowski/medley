/*
 * Created on 16-Feb-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.io.File;
import com.mmakowski.medley.resources.Errors;

/**
 * An abstract class for objects that provide connections to JDBC databases.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/04/24 16:02:45 $
 */
abstract class JDBCConnector {
	// connector modes
	protected static final int FILE = 0;
	protected static final int SERVER = 1;

    /** logger */
    private static final Logger log = Logger.getLogger(JDBCConnector.class.getName());
    
	/** the mode in which this connector connects to the database; either FILE or SERVER */
	protected int mode;
	
	/**
	 * @return connector mode
	 */
	int getMode() {
		return mode;
	}
	
	/**
	 * @return latest database schema version supported by this connector.
	 */
	abstract int getLatestDatabaseVersion();
	
	/**
	 * @return <code>true</code> if this connector support local files.
	 */
	boolean supportsFile() { return false; }
	
	/**
	 * @return <code>true</code> it this connector supports connection with remote servers.
	 */
	boolean supportsServer() { return false; }
	
	/**
	 * @return <code>true</code> if this connector is currently connected.
	 */
	abstract boolean isOpen();
	
	/**
	 * Open given local database file.
	 * @param path the path to local file
	 * @param version database format version
	 * @throws DataSourceException
	 */
	void openFile(File path, int version) throws DataSourceException {
        log.finest("openFile(" + path + ", " + version + ")");
		throw new DataSourceException(Errors.CONNECTOR_DOES_NOT_SUPPORT_FILES);
	}
	
	/**
	 * Establish connection with given database server and open given database
	 * @param serverURL database server's address
	 * @param databaseName name of the database
	 * @param version database format version
	 * @throws DataSourceException
	 */
	void openServer(String serverURL, String databaseName, int version) throws DataSourceException {
        log.finest("openServer(\"" + serverURL + "\", \"" + databaseName + "\", " + version + ")");
		throw new DataSourceException(Errors.CONNECTOR_DOES_NOT_SUPPORT_SERVERS);
	}
	
	/**
	 * @return a connection to active database
	 * @throws DataSourceException
	 */
	abstract Connection connect() throws DataSourceException;
	
	/**
	 * Disconnect supplied connection.
	 * @param conn the connection to be disconnected
	 * @throws DataSourceException
	 */
	abstract void disconnect(Connection conn) throws DataSourceException;
	
	/**
	 * Close connection with server/file.
	 * @throws DataSourceException
	 */
	abstract void close() throws DataSourceException;
	
	/**
	 * Prepare database files for saving (i.e. unlock them). Works in file mode only.
	 * @throws DataSourceException
	 */
	void preSave() throws DataSourceException {
        log.finest("preSave()");
		if (mode != FILE) {
			log.log(Level.SEVERE, "JDBCConnector.preSave() called in non-file mode.");
			throw new DataSourceException(Errors.OPERATION_SUPPORTED_IN_FILE_MODE_ONLY);
		}
	}
	
	/**
	 * Reclaim database files after save. Works in file mode only.
	 * @throws DataSourceException
	 */
	void postSave() throws DataSourceException {
		if (mode != FILE) {
            log.log(Level.SEVERE, "JDBCConnector.postSave() called in non-file mode.");
			throw new DataSourceException(Errors.OPERATION_SUPPORTED_IN_FILE_MODE_ONLY);
		}
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
	
}
