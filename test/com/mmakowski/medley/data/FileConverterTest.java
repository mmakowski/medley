/*
 * Created on 28-Jan-2005
 */
package com.mmakowski.medley.data;

import junit.framework.Assert;

import com.mmakowski.events.ProgressEvent;
import com.mmakowski.events.ProgressListener;
import com.mmakowski.io.File;
import com.mmakowski.medley.MedleyTest;
import com.mmakowski.medley.TestSettings;

/**
 * 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/02/19 10:30:30 $
 */
public class FileConverterTest extends MedleyTest {

	boolean flag;
	
	/*
	 * @see MedleyTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see MedleyTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testCanConvert() throws DataSourceException {
		//DataSource.create("com.mmakowski.medley.data.FileDataSource", null);
		FileDataSource.createNew();
		FileConverter conv = new FileConverter();
		Assert.assertFalse(conv.canConvert());
		DataSource.deactivate();
		//DataSource.create("com.mmakowski.medley.data.FileDataSource", TestSettings.OLD_FILE_NAME);
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		conv = new FileConverter();
		Assert.assertTrue(conv.canConvert());
		DataSource.deactivate();
	}
	
	public void testConvert() throws DataSourceException {
		//DataSource.create("com.mmakowski.medley.data.FileDataSource", TestSettings.OLD_FILE_NAME);
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		FileConverter conv = new FileConverter();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertTrue(conv.canConvert());
		//Assert.assertTrue(fb.getDatabaseVersion() < FileDataSource.LATEST_DB_VERSION);
		fb = conv.convert();
		Assert.assertEquals(fb.getFileVersion(), FileDataSource.LATEST_FILE_VERSION);
		JDBCConnectorFactory fac = new JDBCConnectorFactory();
		Assert.assertEquals(fb.getDatabaseVersion(), fac.createConnector(FileDataSource.DEFAULT_DB_TYPE).getLatestDatabaseVersion());
		File of = new File(TestSettings.OLD_FILE_NAME);
		Assert.assertEquals(fb.getShortName(), of.getName());
		DataSource.deactivate();
		File f = new File(TestSettings.OLD_FILE_NAME + ".old");
		Assert.assertTrue(f.exists());
		Assert.assertTrue(of.delete());
		Assert.assertTrue(f.renameTo(of));
	}
	
	public void testAddProgressListener() throws DataSourceException {
		ProgressListener pl = new ProgressListener() {
			public void taskProgressed(ProgressEvent e) {
				flag = true;
			}
		};
		//DataSource.create("com.mmakowski.medley.data.FileDataSource", TestSettings.OLD_FILE_NAME);
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		FileConverter conv = new FileConverter();
		conv.addProgressListener(pl);
		flag = false;
		conv.convert();
		Assert.assertTrue(flag);
		DataSource.deactivate();
		File of = new File(TestSettings.OLD_FILE_NAME);
		File f = new File(TestSettings.OLD_FILE_NAME + ".old");
		Assert.assertTrue(of.delete());
		Assert.assertTrue(f.renameTo(of));
	}

	public void testRemoveProgressListener() throws DataSourceException {
		ProgressListener pl = new ProgressListener() {
			public void taskProgressed(ProgressEvent e) {
				flag = true;
			}
		};
		//DataSource.create("com.mmakowski.medley.data.FileDataSource", TestSettings.OLD_FILE_NAME);
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		FileConverter conv = new FileConverter();
		conv.addProgressListener(pl);
		conv.removeProgressListener(pl);
		flag = false;
		conv.convert();
		Assert.assertFalse(flag);
		DataSource.deactivate();
		File of = new File(TestSettings.OLD_FILE_NAME);
		File f = new File(TestSettings.OLD_FILE_NAME + ".old");
		Assert.assertTrue(of.delete());
		Assert.assertTrue(f.renameTo(of));
	}


}
