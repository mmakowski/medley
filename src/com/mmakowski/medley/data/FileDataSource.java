/*
 * Created on 2004-04-09
 */
package com.mmakowski.medley.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;

import com.mmakowski.events.ProgressEvent;
import com.mmakowski.events.ProgressListener;
import com.mmakowski.io.File;
import com.mmakowski.medley.resources.Errors;

/**
 * A data source that uses files for storing data.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.7 $  $Date: 2005/08/19 18:01:07 $
 */
public class FileDataSource extends DataSource {
	/** latest file version supported by this release of Medley */
	public static final int LATEST_FILE_VERSION = 1 * MAX_MINOR_VERSIONS + 1;
	/** default database type for this release of Medley */
	public static final String DEFAULT_DB_TYPE = JDBCConnectorFactory.HSQLDB;
	
	private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
	private static final String NEW_FILE_TEMPLATE_NAME = "empty.tmd";
	private static final String IMAGE_SUBDIR = "img";
	/** format to store images in */
	private static final int IMAGE_FORMAT = SWT.IMAGE_JPEG;
	
	// read from system properties
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

	// manifest file entries
	private static final String ZMD_VERSION = "ZMD-Version";
	private static final String DATABASE_VERSION = "Database-Version";
	private static final String DATABASE_TYPE = "Database-Type";
	
	// task tags
	private static final String TASK_SAVE = "save";
	private static final String TASK_OPEN = "open";
	
    /** logger */
    private static final Logger log = Logger.getLogger(FileDataSource.class.getName());

    /** 
     * the tag with which temporary directories and files created 
	 * 	by this instance of Medley are marked. 
	 */
	private static String instanceTag = "medley";
	
	
	static {
		Calendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		instanceTag = "medley." + Long.toHexString(cal.getTimeInMillis());
        log.info("instance tag: " + instanceTag);
	}
	
	/**
	 * Create new data source based on supplied path.
	 * @param filePath path to data file
	 * @throws DataSourceException
	 */
	public static void create(String filePath) throws DataSourceException {
		log.finest("create(\"" + filePath + "\")");
        new FileDataSource(filePath);
	}
	
	public static void createNew() throws DataSourceException {
        log.finest("createNew()");
		new FileDataSource(null);
	}
	
	/** the zipped file name */
	protected String fileName;
	/** the database file path */
	protected com.mmakowski.io.File tmpDir;
	/** has this database been modified since last save? */
	private boolean modified;
	/** file format version */
	private int fileVersion;
	/** database type */
	String dbType;
	
	/** max value of progress */
	private int maxProgress;
	
	
	/**
	 * @param sourceString the path to the data (.zmd) file; if null, new file will be created.
	 * @throws DataSourceException
	 */
	private FileDataSource(String sourceString) throws DataSourceException {
        super(sourceString);
	}

	/**
	 * Mark this data source as modified.
	 */	
	public void setModified() {
		if (!modified) {
            modified = true;
    		notifyModifiedStateChanged();
        }
	}
	
	/**
	 * Mark this data source as unmodified.
	 */	
	private void clearModified() {
		if (modified) {
            modified = false;
    		notifyModifiedStateChanged();
        }
	}
	
	/**
	 * @see com.mmakowski.medley.data.DataSource#requiresSave()
	 */
	public boolean requiresSave() {
		return true;
	}

	/**
	 * @see com.mmakowski.medley.data.DataSource#save(java.lang.String)
	 */
	public boolean save(String name) throws DataSourceException {
	    log.finest("save(\"" + name + "\")");
        if (name != null && name.length() > 0) {
			saveDataObjects();
			connector.preSave();
			fileName = name;
			try {
				ProgressListener l = new ProgressListener () {
					public void taskProgressed(ProgressEvent e) {
						progress.notifyTaskProgressed(TASK_SAVE, e.getMinValue(), e.getMaxValue(), e.getCurValue());
					}
				};
				tmpDir.addProgressListener(l);
				tmpDir.zip(new File(fileName));
				tmpDir.removeProgressListener(l);
				progress.notifyTaskCompleted();
			} catch (Exception ex) {
				log.log(Level.SEVERE, "error while compressing files", ex);
				Object[] args = {tmpDir, fileName};
				throw new DataSourceException(Errors.DATA_SOURCE_CANT_ZIP_FILES, 
											  args, ex);
			}
			clearModified();
			connector.postSave();
			return true;
		} else {
			// incorrect file name provided
			return false;
		}
	}
	
	/**
	 * Save the data to the file that was opened.
	 */
	public boolean save() throws DataSourceException {
		log.finest("save()");
        return save(fileName);
	}	
	
	/**
	 * @see com.mmakowski.medley.data.DataSource#open(java.lang.String)
	 */
	protected void open(String sourceString) throws DataSourceException {
		log.finest("open(\"" + sourceString + "\")");
		
		fileName = sourceString; 
		// unpack the data from ZIP

		File zmdFile = null;
		if (fileName == null) {
			// create new file
			zmdFile = new File(NEW_FILE_TEMPLATE_NAME);
            log.info("creating new file");
		} else {
            zmdFile = new File(fileName);
            log.info("opening file: " + zmdFile.getAbsolutePath());
		}
		
        if (!zmdFile.exists()) {
            log.severe("data file not found: " + zmdFile.getAbsolutePath());
            throw new DataSourceException(Errors.SOURCE_FILE_NOT_FOUND, new Object[] {zmdFile.getAbsolutePath()});
        }
        
        if (!zmdFile.isFile()) {
            log.severe(zmdFile.getAbsolutePath() + " is not a file");
            throw new DataSourceException(Errors.SOURCE_IS_NOT_A_FILE, new Object[] {zmdFile.getAbsolutePath()});
        }
        
        if (!zmdFile.canRead()) {
            log.severe(zmdFile.getAbsolutePath() + " is not a file");
            throw new DataSourceException(Errors.CANT_READ_SOURCE_FILE, new Object[] {zmdFile.getAbsolutePath()});
        }

        try {
			// Specify destination where file will be unzipped
			tmpDir = new File(TEMP_DIR + "/" + instanceTag);
			tmpDir.mkdirs();
			ProgressListener l = new ProgressListener () {
				public void taskProgressed(ProgressEvent e) {
					progress.notifyTaskProgressed(TASK_OPEN, e.getMinValue(), e.getMaxValue() + 3, e.getCurValue());
					maxProgress = e.getMaxValue() + 3;
				}
			};
			zmdFile.addProgressListener(l);
			zmdFile.unzip(tmpDir);
			zmdFile.removeProgressListener(l);
		} catch (IOException ex) {
			log.log(Level.SEVERE, "error while uncompressing files", ex);
			Object[] args = {zmdFile};
			throw new DataSourceException(Errors.DATA_SOURCE_CANT_UNZIP_FILE, 
										  args, ex);
		}

		
		// read in the manifest file
		readManifest(tmpDir.getAbsolutePath() + "/" + MANIFEST_PATH);
		if (fileVersion > LATEST_FILE_VERSION) {
			close();
			String fvStr = String.valueOf(fileVersion / DataSource.MAX_MINOR_VERSIONS) + "." +
				String.valueOf(fileVersion % DataSource.MAX_MINOR_VERSIONS);
			String lvStr = String.valueOf(LATEST_FILE_VERSION / DataSource.MAX_MINOR_VERSIONS) + "." +
				String.valueOf(LATEST_FILE_VERSION % DataSource.MAX_MINOR_VERSIONS);
            log.warning("Cannot open data file " + zmdFile + ": this file's format version is " + fvStr + " while latest supported is " + lvStr);
			throw new DataSourceException(Errors.DATA_FILE_VERSION_TOO_NEW, 
					new Object[] {lvStr, fvStr});
		}
		progress.notifyTaskProgressedBy(1);
		
		// set database path
		String strDbPath = tmpDir.getAbsolutePath();
		if (fileVersion > 10000) {
			strDbPath += "/data";
		}
		File dbPath = new File(strDbPath);
		
		// create connector
		JDBCConnectorFactory connFact = new JDBCConnectorFactory();
		connector = connFact.createConnector(dbType);
		// open database
		connector.openFile(dbPath, dbVersion);
		
		progress.notifyTaskCompleted();
		
		clearModified();
	}

	/**
	 * Remove temporary files.
	 * @see com.mmakowski.medley.data.DataSource#close()
	 */
	protected void close() throws DataSourceException {
		log.entering(this.getClass().getName(), "close");
		// close the connection
		if (connector != null) {
			connector.close();
		}
		
		if (tmpDir == null) {
			return;
		}
		
		// remove the temporary files
		boolean deleteSuccesful = false;
		try {
			deleteSuccesful = tmpDir.deleteRecursively();
		} catch (SecurityException ex) {
			deleteSuccesful = false;
		}
		if (!deleteSuccesful) {
			log.log(Level.WARNING, "couldn't delete all the temporary files");
			throw new DataSourceException(Errors.DATA_SOURCE_CANT_REMOVE_TMP_FILES);			
		}
		tmpDir = null;
		
		// remove the data objects
		dataObjects.removeAllElements();
        log.exiting(this.getClass().getName(), "close");
	}

	/**
	 * 
	 * @param path the path to the manifest file
	 */
	private void readManifest(String path) throws DataSourceException {
		log.finest("readManifest(\"" + path + "\")");
		FileInputStream mis = null;
		Manifest mf = null;
		try {
			mis = new FileInputStream(path);
			mf = new Manifest(mis);
			mis.close();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "error opening manifest file", ex);
			Object[] args = {path};
			throw new DataSourceException(Errors.DATA_SOURCE_CANT_OPEN_MANIFEST, 
										  args, ex);
		}
		Attributes attr = mf.getMainAttributes();
		// read the file version
		String tmp = attr.getValue(ZMD_VERSION);
		if (tmp == null) {
			log.log(Level.SEVERE, "missing " + ZMD_VERSION + " in the manifest file");
			Object[] args = {ZMD_VERSION, path};
			throw new DataSourceException(Errors.DATA_SOURCE_MANIFEST_ENTRY_MISSING,
										  args);
		}
		log.info(ZMD_VERSION + ": " + tmp);
		StringTokenizer tok = new StringTokenizer(tmp, ".");
		fileVersion = Integer.parseInt(tok.nextToken());
		fileVersion = fileVersion * MAX_MINOR_VERSIONS + Integer.parseInt(tok.nextToken());
		if (fileVersion != LATEST_FILE_VERSION) {
			log.warning("latest file format version: " + LATEST_FILE_VERSION + ", this file's format version: " + fileVersion);
		}

		// read the database type
		tmp = attr.getValue(DATABASE_TYPE);
		if (tmp == null) {
			if (fileVersion == 10000) {
				// prior to file version 10001 only Firebird has been used as database
				tmp = JDBCConnectorFactory.FIREBIRD;
			} else {
                log.log(Level.SEVERE, "missing " + DATABASE_TYPE + " in the manifest file");
				Object[] args = {DATABASE_TYPE, path};
				throw new DataSourceException(Errors.DATA_SOURCE_MANIFEST_ENTRY_MISSING,
											  args);
			}
		}
		dbType = tmp;
		
		// read the database version
		tmp = attr.getValue(DATABASE_VERSION);
		if (tmp == null) {
            log.log(Level.SEVERE, "missing " + DATABASE_VERSION + " in the manifest file");
			Object[] args = {DATABASE_VERSION, path};
			throw new DataSourceException(Errors.DATA_SOURCE_MANIFEST_ENTRY_MISSING,
										  args);
		}
		log.info(DATABASE_VERSION + ": " + tmp);
		tok = new StringTokenizer(tmp, ".");
		dbVersion = Integer.parseInt(tok.nextToken());
		dbVersion = dbVersion * MAX_MINOR_VERSIONS + Integer.parseInt(tok.nextToken());
	}
	
	/**
	 * @see com.mmakowski.medley.data.DataSource#isModified()
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * @return the file name of open file
	 * @see com.mmakowski.medley.data.DataSource#getShortName()
	 */
	public String getShortName() {
		return (fileName == null) ? "Untitled" : (new File(fileName)).getName();
	}

	/**
	 * @see com.mmakowski.medley.data.DataSource#isNew()
	 */
	public boolean isNew() {
		return fileName == null;
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataSource#GetImageDir(com.mmakowski.medley.data.Visible)
	 */
	File getImageDir(Visible v) throws DataSourceException {
		File imageDir = new File(tmpDir.toString() + "/" + IMAGE_SUBDIR);
		if (!imageDir.exists()) {
			if (!imageDir.mkdirs()) {
                log.log(Level.SEVERE, "Cannot create image directory " + imageDir);
				throw new DataSourceException(Errors.CANT_CREATE_DIR, new Object[] {imageDir});
			}
		}
		return imageDir;
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataSource#addImage(com.mmakowski.medley.data.Visible, java.io.File)
	 */
	void addImage(Visible v, ImageData i) throws DataSourceException {
	    log.finest("addImage(" + v + ", " + i + ")");
        // File is already in image dir and will be packed into
		// the data file during next save, so there's no need to do anything
		setModified();
	}
	
	/**
	 * @see com.mmakowski.medley.data.JDBCDataSource#removeImage(com.mmakowski.medley.data.Visible, com.mmakowski.medley.data.ImageData)
	 */
	void removeImage(Visible v, ImageData i) throws DataSourceException {
        log.finest("removeImage(" + v + ", " + i + ")");
		// File has already been removed from image dir 
		// so there's no need to do anything
		setModified();
	}

	/**
	 * @see com.mmakowski.medley.data.JDBCDataSource#getImageFormat()
	 */
	int getImageFormat() {
		return IMAGE_FORMAT;
	}

	/**
	 * @return Returns the database type.
	 */
	public String getDatabaseType() {
		return dbType;
	}
	
	/**
	 * @return file version number.
	 */
	public int getFileVersion() {
		return fileVersion;
	}

    /**
     * @see com.mmakowski.medley.data.DataSource#isFormatUpToDate()
     */
    public boolean isFormatUpToDate() {
        return getFileVersion() == LATEST_FILE_VERSION && 
            getDatabaseVersion() == connector.getLatestDatabaseVersion();
    }
}
