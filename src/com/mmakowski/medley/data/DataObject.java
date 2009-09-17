/*
 * Created on 2004-04-08
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.events.ListenerRegistry;
import com.mmakowski.medley.data.events.DataObjectEvent;
import com.mmakowski.medley.data.events.DataObjectListener;
import com.mmakowski.medley.resources.Errors;

/**
 * This is an abstract data object, that is an object
 * that stands in the middle between the UI and the data source.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $  $Date: 2005/08/19 17:57:59 $
 */
public abstract class DataObject {

	// item types -- for use with generic attributes like
	// tags and rankings
	public static final int ALBUM = 0;
	public static final int RECORD = 1;
	public static final int TRACK = 2;
	public static final int ARTIST = 3;

    /** logger */
    protected static Logger log = Logger.getLogger(DataObject.class.getName());
    
	/**
	 * Notify all listeners registered in ListenerRegistry for 
	 * appropriate class that a new object of this class has been created.
	 */
	protected static void notifyObjectCreated(DataObject dObj) {
	    log.finest("notifyObjectCreated(" + dObj + ")");
        final Vector ls = ListenerRegistry.getListeners(dObj.getClass());
		if (ls != null && !ls.isEmpty()) {
			final DataObjectEvent e = new DataObjectEvent(dObj, DataObjectEvent.OBJECT_CREATED, dObj.getId());
			// notify listeners asynchronously
			Thread notifier = new Thread() {
				public void run() {
					for (Iterator i = ls.iterator(); i.hasNext();) {
						try {
							DataObjectListener l = (DataObjectListener) i.next();
							l.objectCreated(e);
						} catch (ClassCastException ex) {
							log.log(Level.WARNING, "Registered listener is not a DataObjectListener", ex);
						}
					}
				}
			};
			notifier.start();
		}
	}
	
	/**
	 * Notify all listeners registered in ListenerRegistry for 
	 * appropriate class that an object has been deleted.
	 */
	protected static void notifyObjectDeleted(Class c, int id) {
	    log.finest("notifyObjectDeleted(" + c.getName() + ", " + id + ")");
        final Vector ls = ListenerRegistry.getListeners(c);
		if (ls != null && !ls.isEmpty()) {
			final DataObjectEvent e = new DataObjectEvent(c, DataObjectEvent.OBJECT_DELETED, id);
			// notify listeners asynchronously
			Thread notifier = new Thread() {
				public void run() {
					for (Iterator i = ls.iterator(); i.hasNext();) {
						try {
							DataObjectListener l = (DataObjectListener) i.next();
							l.objectDeleted(e);
						} catch (ClassCastException ex) {
							log.log(Level.WARNING, "Registered listener is not a DataObjectListener", ex);
						}
					}
				}
			};
			notifier.start();
		}
	}
	
	/**
	 * @return current data source as a JDBC data source.	
	 */	
	protected static DataSource getCurrentDataSource() throws DataSourceException {
		com.mmakowski.medley.data.DataSource tmpds =
			com.mmakowski.medley.data.DataSource.getNotNull();
		try {
			return (DataSource) tmpds;
		} catch (ClassCastException ex) {
			log.log(Level.SEVERE, "current data source is not a JDBC data source", ex);
			Object[] params = {tmpds};
			throw new DataSourceException(Errors.DATA_OBJECT_INCOMPATIBLE_WITH_SOURCE, params, ex);
		}
	}

	/**
	 * Generates new name for this data object so that UNIQUE
	 * constraint on given column of given table is not violated.
	 * @param table database table
	 * @param column column name in <code>table</code>
	 * @param prefix the prefix of new name
	 * @return unique name for new item
	 * @throws DataSourceException
	 */
	protected static String getSafeNewName(String table, String column, String prefix) 
			throws DataSourceException {
		log.finest("getSafeNewName(\"" 
		        + table + "\", \"" + column + "\", \"" + prefix + "\")");
		// obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();

		Connection conn = null;
		String name = "";
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("SELECT " + column + " AS n FROM " + table + 
									  " WHERE SUBSTRING(" + column + " FROM 1 FOR " +
									  prefix.length() + ") = ? ORDER BY " + column);
			pstmt.setString(1, prefix);
			ResultSet res = pstmt.executeQuery();
			if (!res.next()) {
				// no value with matching prefix has been found
				name = prefix;
			} else {
				int num = 1, suffnum = 0;
				do {
					String suff = res.getString("n").substring(prefix.length());
					try {
						suffnum = Integer.parseInt(suff.trim());
					} catch (NumberFormatException ex) {
						// do nothing -- current num is ok.
					}
					if (suffnum >= num) {
						num = suffnum + 1;
					}
				} while (res.next());
				name = prefix + " " + String.valueOf(num);
			}
			
			res.close();
			pstmt.close();
			ds.disconnect(conn);
			return name;
		} catch (SQLException ex) {
			log.log(Level.SEVERE, "error generating safe new name", ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
		
	}

	/**
	 * Delete given record
	 * @param tableName table from which to delete
	 * @param keyFieldName name of key field in table
	 * @param id key value for record to be deleted
	 * @throws DataSourceException
	 */
	protected static void delete(Class c, String tableName, String keyFieldName, int id) throws DataSourceException {
	    log.finest("delete(" + c.getName() + ", \"" + tableName + "\", \"" + keyFieldName + "\", " + id + ")");
        // obtain the data source and make sure it's a JDBC data source
		DataSource ds = getCurrentDataSource();
		
		// delete the record
		Connection conn = null;
		try {
			conn = ds.connect();
			PreparedStatement pstmt = 
				conn.prepareStatement("DELETE FROM " + tableName + " WHERE " + keyFieldName + " = ?");
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			pstmt.close();
			getCurrentDataSource().setModified();
			notifyObjectDeleted(c, id);
		} catch (SQLException ex) {
			log.log(Level.SEVERE , "error deleting from " + tableName + " where " + keyFieldName + " = " + id, ex);
			throw new DataSourceException(ex);
		} finally {
			ds.disconnect(conn);
		}
	}

	/**
	 * Dispose of all the DataObjects contained in given vector and remove them from the vector.
	 * @param v a Vector containing DataObjects
	 * @throws DataSourceException
	 */
	public static void disposeAll(Vector v) throws DataSourceException {
		log.finest("disposeAll(" + v + ")");
        for (Iterator i = v.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof DataObject) {
				((DataObject) o).dispose();
				i.remove();
			}
		}
	}

	/**
	 * @param type item type (ALBUM, RECORD, TRACK or ARTIST)
	 * @return item string
	 * @throws DataSourceException
	 */
	public static String itemTypeToString(int type) throws DataSourceException {
        switch(type) {
		case ALBUM: return "album";
		case RECORD: return "record";
		case TRACK: return "track";
		case ARTIST: return "artist";
		default: throw new DataSourceException(Errors.UNSUPPORTED_ITEM_TYPE_VALUE, new Object[] {new Integer(type)});
		}
	}
	
	/**
	 * @param str string denoting ALBUM, RECORD, TRACK or ARTIST
	 * @return item type value for given string
	 * @throws DataSourceException
	 */
	public static int stringToItemType(String str) throws DataSourceException {
        String s = str.toLowerCase().trim();
		if (s.equals("album")) {
			return ALBUM;
		} else if (s.equals("record")) {
			return RECORD;
		} else if (s.equals("track")) {
			return TRACK;
		} else if (s.equals("artist")) {
			return ARTIST;
		} else {
			throw new DataSourceException(Errors.UNSUPPORTED_ITEM_TYPE_STRING, new Object[] {str});
		}
	}
	
    /** the list of listeners to this data source */
    private List listeners;
    /** Does the data in this object need to be saved? */
    private boolean modified = false;
    /** Has this object been disposed? */
    private boolean disposed = false;

    /**
     * Create a data object
     */
    protected DataObject() throws DataSourceException {
        log.finest("DataObject()");
        listeners = new Vector();
        // add registered listeners
        Vector ls = ListenerRegistry.getListeners(getClass());
        if (ls != null) {
            for (Iterator i = ls.iterator(); i.hasNext();) {
                try {
                    addDataObjectListener((DataObjectListener) i.next());
                } catch (ClassCastException ex) {
                    throw new DataSourceException(Errors.INCORRECT_LISTENER_TYPE, ex);
                }
            }
        }
        getCurrentDataSource().registerDataObject(this);
    }
    
    /**
     * Add a listener for events related to this data object.
     * @param listener the listener to be added
     */
    public void addDataObjectListener(DataObjectListener listener) {
        log.finest("addDataObjectListener(" + listener + ")");
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
    public boolean removeDataObjectListener(DataObjectListener listener) {
        log.finest("removeDataObjectListener(" + listener + ")");
        return listeners.remove(listener);
    }

    /**
     * Notify listeners that an attribute of this data object
     * has changed.
     */
    protected void notifyAttributeChanged() {
        if (listeners.isEmpty()) {
            return;
        }
        final DataObjectEvent e = new DataObjectEvent(this, DataObjectEvent.ATTRIBUTE_CHANGED, getId());
        // notify listeners asynchronously
        Thread notifier = new Thread() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext();) {
                    DataObjectListener l = (DataObjectListener) i.next();
                    l.attributeChanged(e);
                }
            }
        };
        notifier.start();
    }

    /**
     * Notify listeners that this object has been saved
     */
    protected void notifyObjectSaved() {
        log.finest("notifyObjectSaved()");
        if (listeners.isEmpty()) {
            return;
        }
        final DataObjectEvent e = new DataObjectEvent(this, DataObjectEvent.OBJECT_SAVED, getId());
        // notify listeners asynchronously
        Thread notifier = new Thread() {
            public void run() {
                for (Iterator i = listeners.iterator(); i.hasNext();) {
                    DataObjectListener l = (DataObjectListener) i.next();
                    l.objectSaved(e);
                }
            }
        };
        notifier.start();
    }

	/**
	 * Save all the changes to this object.	 
	 */
	protected abstract void save() throws DataSourceException;
	
	/**
	 * Unregister this data object with current data source.
	 * @throws DataSourceException if unregistering fails
	 */
	public void dispose() throws DataSourceException {
	    log.finest("dispose()");
        checkDisposed();
		save();
		getCurrentDataSource().unregisterDataObject(this);
		disposed = true;
	}
	
	/**
	 * Delete the data this object corresponds to from the database.
	 * @throws DataSourceException
	 */
	protected abstract void deleteSelf() throws DataSourceException;
	
	/**
	 * Delete data corresponding to this object from the database
	 * and unregister it with data source.
	 * @throws DataSourceException
	 */
	public void delete() throws DataSourceException {
	    log.finest("delete()");
        checkDisposed();
		deleteSelf();
		clearModified();
		getCurrentDataSource().unregisterDataObject(this);
	}
	
	/**
	 * Mark the object as modified and inform the data source
	 * that the data has been modified. 
	 * @throws DataSourceException
	 */
	protected void setModified() throws DataSourceException {
        modified = true;
		getCurrentDataSource().setModified();
	}

	/**
	 * Notify listeners that attribute has changed.
	 * @throws DataSourceException
	 */
	protected void attributeChanged() throws DataSourceException {
        checkDisposed();
		setModified();
		notifyAttributeChanged();
	}
	
	/**
	 * Throw an exception if this object is disposed.
	 * @throws DataSourceException
	 */
	protected void checkDisposed() throws DataSourceException {
        if (disposed) {
			throw new DataSourceException(Errors.DATA_OBJECT_DISPOSED);
		}
	}	
	
	/**
	 * @return id of this object
	 */
	public abstract int getId();
    
    /**
     * @return <code>true</code> if this object has been modified since last save
     */
    protected boolean isModified() {
        return modified;
    }
    
    /**
     * Clear the modified flag of this object.
     */
    protected void clearModified() {
        modified = false;
    }
}
