/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import com.mmakowski.events.ProgressDelegate;
import com.mmakowski.events.ProgressListener;
import com.mmakowski.io.File;
import com.mmakowski.medley.data.events.DataSourceEvent;
import com.mmakowski.medley.data.events.DataSourceListener;
import com.mmakowski.medley.resources.Errors;

/**
 * An abstract data source for Medley. There can be only one data source in 
 * a running instance of Medley. It can be created using static method
 * DataSource.create() and then obtained using DataSource.get().
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.16 $ $Date: 2005/08/19 17:58:42 $ 
 */
public abstract class DataSource {
	public static final int MAX_MINOR_VERSIONS = 10000; 
	// TODO: revise the reason for DataSource keeping track of all DataObjects
    
	// The global data source of the application.
	private static DataSource dataSource = null;
    /** logger */
    private static Logger log = Logger.getLogger(DataSource.class.getName());
	
	/**
	 * Return the data source.
	 * @return the current data source of the application
	 */
	public static final DataSource get() {
		return dataSource;
	}
	
	/**
	 * @return true if there is a data source opened.
	 */
	public static final boolean isActive() {
		return dataSource != null;
	}
	
	/**
	 * Remove current data source.
	 */
	public static final void deactivate() throws DataSourceException {
	    log.entering("DataSource", "deactivate");
        if (isActive()) {
			dataSource.close();
			dataSource = null;
		}
	}
	
	/**
	 * Return the data source throwing an exception if it's null.
	 * @return the current data source of the application
	 * @throws DataSourceException if the data source is null
	 */
	public static final DataSource getNotNull() throws DataSourceException {
		if (dataSource == null) {
			throw new DataSourceException(Errors.DATA_SOURCE_IS_NULL);
		} else {
			return dataSource;
		}
	}
	
	/** the list of listeners to this data source */
	private List listeners;
	/** delegate for progress events */
	protected ProgressDelegate progress;
	/** a list of data objects */
	protected Vector dataObjects;
	/** database version */
	protected int dbVersion;
	/** JDBC connector */
	protected JDBCConnector connector;
	
	/**
	 * Construct a data source.
	 * @param sourceString a String specifying the particular source;
	 * can be a connection string for databases or a file name.
	 */
	protected DataSource(String sourceString) throws DataSourceException {
	    log.finest("DataSource(\"" + sourceString + "\")");
        // deactivate previous data source and register new object as current data source
		deactivate();
		dataSource = this;
		
		progress = new ProgressDelegate(this);
		listeners = new Vector();
		dataObjects = new Vector();
		connector = null;
		open(sourceString);
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
	 * Add a listener for events related to this data source.
	 * @param listener the listener to be added
	 */
	public void addDataSourceListener(DataSourceListener listener) {
	    log.finest("addDataSourceListener(" + listener + ")");
        // add only once
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes given listener from the list of listeners.
	 * @param listener listener to be removed
	 * @return false if given listener was not found on the list, true otherwise
	 */
	public boolean removeDataSourceListener(DataSourceListener listener) {
        log.finest("removeDataSourceListener(" + listener + ")");
		return listeners.remove(listener);
	}
	
	/**
	 * Notify listeners that "modified" state of this data source
	 * has changed.
	 */
	protected void notifyModifiedStateChanged() {
		if (listeners.isEmpty()) {
			return;
		}
		final DataSourceEvent e = new DataSourceEvent(this, DataSourceEvent.MODIFIED_STATE_CHANGED);
		// notify listeners asynchronously
		Thread notifier = new Thread() {
			public void run() {
				for (Iterator i = listeners.iterator(); i.hasNext();) {
					DataSourceListener l = (DataSourceListener) i.next();
					l.modifiedStateChanged(e);
				}
			}
		};
		notifier.start();
	}
	
	/**
	 * @return a short name for this data source (e.g. a file name)
	 */
	public abstract String getShortName();
	
	/**
	 * @return true if this data source requires user to perform a 
	 * "save file" in order for the changes to be saved. For instance
	 * a database data source should return false, because all the 
	 * operations immediately affect the data stored in it.
	 */
	public abstract boolean requiresSave();
	
	/**
	 * @return true if the file represented by this data source is
	 * newly created and needs to have a name assinged before it can be saved.
	 */
	public abstract boolean isNew();
	
	/**
	 * @return true if this data source has been modified. This method
	 * is intended to use by the UI together with requiresSave(); i.e. 
	 * before closing a data source the UI should check whether it 
	 * needs to be saved and ask user for action.
	 * @see com.mmakowski.medley.data.DataSource#requiresSave();
	 */
	public abstract boolean isModified();
	
	/**
	 * Mark this data source as modified.
	 */
	public abstract void setModified();
	
	/**
	 * Commit all the changes to the data source.
	 * @param sourceString a String specifying the exact data source
	 *        to which the data should be saved.
	 * @return true if the save was succesful, false otherwise 
	 * @throws DataSourceException if an erro occurs
	 */
	public abstract boolean save(String sourceString) throws DataSourceException;
	
	/**
	 * Save the data to the default source.
	 * @return true if save was successful
	 * @throws DataSourceException
	 */
	public abstract boolean save() throws DataSourceException;

	/**
	 * Open the data source.
	 * @param sourceString a string specifying the details of the source
	 * @throws DataSourceException if the source for given sourceString
	 *         cannot be opened
	 */
	protected abstract void open(String sourceString) throws DataSourceException;
	
	/**
	 * Close the data source.
	 */
	protected abstract void close() throws DataSourceException;
	
	/**
	 * @return a database connection
	 * @throws DataSourceException
	 */
	Connection connect() throws DataSourceException {
		return connector.connect();
	}
	
	/**
	 * @return path to the directory where the images for given item are stored.
	 * @throws DataSourceException
	 */
	abstract File getImageDir(Visible v) throws DataSourceException;
	
	/**
	 * Add new image for given visible item.
	 * @param v the item for which image is being added
	 * @param i image data of the image to be added
	 * @throws DataSourceException
	 */
	abstract void addImage(Visible v, ImageData i) throws DataSourceException;
	
	/**
	 * Remove image from the data source
	 * @param v item for which image should be removed
	 * @param i image data of the image to be removed
	 * @throws DataSourceException
	 */
	abstract void removeImage(Visible v, ImageData i) throws DataSourceException;
	
	/**
	 * @return constant corresponding to the format in which image
     * should be stored
	 */
	abstract int getImageFormat();
	
	/**
	 * Try to close the connection and convert possible SQLException to
	 * DataSourceException.
	 * @param conn the connection to be closed
	 * @throws DataSourceException if an SQLException is thrown during
	 * closing of the connection.
	 */
	void disconnect(Connection conn) throws DataSourceException {
		connector.disconnect(conn);
	}

	/**
	 * Register a data object with this data source.
	 * @param dObj the data object to be registered
	 */
	protected void registerDataObject(DataObject dObj) {
		dataObjects.add(dObj);
	}
	
	/**
	 * Unregister given data object with this source.
	 * @param dObj the data object to be unregistered
	 */
	protected void unregisterDataObject(DataObject dObj) {
		dataObjects.remove(dObj);
	}
	
	/**
	 * Call the save() methods of all the registered data objects.
	 * @throws DataSourceException
	 */
	protected void saveDataObjects() throws DataSourceException {
		log.finest("saveDataObjects()");
		// DataObject.save() might itself modify dataObjects list
		// so we need to iterate through a copy of it -- otherwise we'll get
		// ConcurrentModifictaionException
		Vector dObjs = (Vector) dataObjects.clone();
		for (Iterator i = dObjs.iterator(); i.hasNext();) {
			((DataObject) i.next()).save();
		}
	}

	/**
	 * 
	 * @return the database format version
	 */
	public int getDatabaseVersion() {
		return dbVersion;
	}
	
	/**
	 * @return active data objects
	 */
	public Vector getDataObjects() {
		return dataObjects;
	}
    
    /**
     * @return <code>true</code> if the format of this data source is up to date, <code>false</code> otherwise.
     */
    public abstract boolean isFormatUpToDate();

}
