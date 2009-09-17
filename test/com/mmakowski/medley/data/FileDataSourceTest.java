/*
 * Created on 27-Jan-2005
 */
package com.mmakowski.medley.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.swt.SWT;

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
 * @version $Revision: 1.3 $ $Date: 2005/02/20 00:17:55 $
 */
public class FileDataSourceTest extends MedleyTest {

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

	public void testCreate() throws DataSourceException {
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertNotNull(fb);
		FileDataSource.createNew();
		fb = (FileDataSource) DataSource.get();
		Assert.assertNotNull(fb);
		DataSource.deactivate();
	}

	public void testDeactivate() throws DataSourceException {
		FileDataSource.createNew();
		Assert.assertTrue(DataSource.isActive());
		DataSource.deactivate();
		Assert.assertFalse(DataSource.isActive());
	}

	public void testIsActive() throws DataSourceException {
		Assert.assertFalse(DataSource.isActive());
		FileDataSource.createNew();
		Assert.assertTrue(DataSource.isActive());
		DataSource.deactivate();
		Assert.assertFalse(DataSource.isActive());
	}

	public void testGet() throws DataSourceException {
		Assert.assertNull(DataSource.get());
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertNotNull(fb);
		DataSource.deactivate();
	}

	public void testGetNotNull() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.getNotNull();
		Assert.assertNotNull(fb);
		DataSource.deactivate();
	}

	public void testGetFileVersion() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getFileVersion(), FileDataSource.LATEST_FILE_VERSION);
		DataSource.deactivate();
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getFileVersion(), 10000);
		DataSource.deactivate();
	}

	public void testGetDatabaseType() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(FileDataSource.DEFAULT_DB_TYPE, fb.getDatabaseType());
		DataSource.deactivate();
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(JDBCConnectorFactory.FIREBIRD, fb.getDatabaseType());
		DataSource.deactivate();
	}
	
	public void testGetDatabaseVersion() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		JDBCConnectorFactory fac = new JDBCConnectorFactory();
		Assert.assertEquals(fb.getDatabaseVersion(), fac.createConnector(FileDataSource.DEFAULT_DB_TYPE).getLatestDatabaseVersion());
		DataSource.deactivate();
		FileDataSource.create(TestSettings.OLD_FILE_NAME);
		fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getDatabaseVersion(), 10006);
		DataSource.deactivate();
	}

	public void testIsNew() throws DataSourceException {
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertFalse(fb.isNew());
		DataSource.deactivate();
		FileDataSource.createNew();
		fb = (FileDataSource) DataSource.get();
		Assert.assertTrue(fb.isNew());
		fb.save(TestSettings.FILE_NAME);
		Assert.assertFalse(fb.isNew());
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
	}

	public void testRequiresSave() throws DataSourceException {
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertTrue(fb.requiresSave());
		DataSource.deactivate();
		FileDataSource.createNew();
		fb = (FileDataSource) DataSource.get();
		Assert.assertTrue(fb.requiresSave());
		fb.save(TestSettings.FILE_NAME);
		Assert.assertTrue(fb.requiresSave());
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
	}

	public void testSetModified() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertFalse(fb.isModified());
		fb.setModified();
		Assert.assertTrue(fb.isModified());
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
	}

	public void testIsModified() throws DataSourceException {
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertFalse(fb.isModified());
		DataSource.deactivate();
		FileDataSource.createNew();
		fb = (FileDataSource) DataSource.get();
		Assert.assertFalse(fb.isModified());
		fb.setModified();
		Assert.assertTrue(fb.isModified());
		fb.save(TestSettings.FILE_NAME);
		Assert.assertFalse(fb.isModified());
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
	}

	public void testGetShortName() throws DataSourceException {
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getShortName(), (new File(TestSettings.ORIGINAL_FILE_NAME)).getName());
		DataSource.deactivate();
		FileDataSource.createNew();
		fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getShortName(), "Untitled");
		fb.save(TestSettings.FILE_NAME);
		File f = new File(TestSettings.FILE_NAME);
		Assert.assertEquals(fb.getShortName(), f.getName());
		DataSource.deactivate();
		f.delete();
	}

	/*
	 * Class under test for boolean save(String)
	 */
	public void testSaveString() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		File f = new File(TestSettings.FILE_NAME);
		Assert.assertFalse(fb.save(null));
		Assert.assertFalse(f.exists());
		Assert.assertFalse(fb.save(""));
		Assert.assertFalse(f.exists());
		Assert.assertTrue(fb.save(TestSettings.FILE_NAME));
		Assert.assertTrue(f.exists());
		Assert.assertEquals(fb.getShortName(), f.getName());
		DataSource.deactivate();
		f.delete();
		FileDataSource.create(TestSettings.ORIGINAL_FILE_NAME);
		fb = (FileDataSource) DataSource.get();
		Assert.assertFalse(fb.save(null));
		Assert.assertFalse(f.exists());
		Assert.assertFalse(fb.save(""));
		Assert.assertFalse(f.exists());
		Assert.assertTrue(fb.save(TestSettings.FILE_NAME));
		Assert.assertTrue(f.exists());
		Assert.assertEquals(fb.getShortName(), f.getName());
		DataSource.deactivate();
		f.delete();
	}

	/*
	 * Class under test for boolean save()
	 */
	public void testSave() throws DataSourceException, IOException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		File f = new File(TestSettings.FILE_NAME);
		Assert.assertFalse(fb.save());
		Assert.assertFalse(f.exists());
		Assert.assertTrue(fb.save(TestSettings.FILE_NAME));
		Album.create();
		Assert.assertTrue(fb.save());
		DataSource.deactivate();
		f.delete();
		File of = new File(TestSettings.ORIGINAL_FILE_NAME);
		of.copyTo(f);
		FileDataSource.create(TestSettings.FILE_NAME);
		fb = (FileDataSource) DataSource.get();
		Album.create();
		Assert.assertTrue(fb.save());
		DataSource.deactivate();
		f.delete();
	}

	public void testConnect() throws DataSourceException, SQLException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		Assert.assertFalse(conn.isClosed());
		fb.disconnect(conn);
		DataSource.deactivate();
	}

	public void testDisconnect() throws DataSourceException, SQLException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		Assert.assertFalse(conn.isClosed());
		fb.disconnect(conn);
		DataSource.deactivate();
	}

	public void testGetImageDir() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Album a = Album.create();
		File f = fb.getImageDir(a);
		Assert.assertTrue(f.exists());
		Assert.assertTrue(f.isDirectory());
		DataSource.deactivate();
	}

	public void testGetImageFormat() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		int i = fb.getImageFormat();
		Assert.assertTrue(i == SWT.IMAGE_BMP ||
				i == SWT.IMAGE_BMP_RLE ||
				i == SWT.IMAGE_GIF ||
				i == SWT.IMAGE_JPEG ||
				i == SWT.IMAGE_PNG ||
				i == SWT.IMAGE_TIFF);
		DataSource.deactivate();
	}

	public void testAddImage() {
		// TODO
	}

	public void testRemoveImage() {
		// TODO
	}

	public void testGetDataObjects() throws DataSourceException {
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertTrue(fb.getDataObjects().isEmpty());
		Album.create();
		Assert.assertEquals(fb.getDataObjects().size(), 1);
		DataSource.deactivate();
	}

	public void testAddProgressListener() throws DataSourceException {
		ProgressListener pl = new ProgressListener() {
			public void taskProgressed(ProgressEvent e) {
				flag = true;
			}
		};
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		fb.addProgressListener(pl);
		flag = false;
		fb.save(TestSettings.FILE_NAME);
		Assert.assertTrue(flag);
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
	}

	public void testRemoveProgressListener() throws DataSourceException {
		ProgressListener pl = new ProgressListener() {
			public void taskProgressed(ProgressEvent e) {
				flag = true;
			}
		};
		FileDataSource.createNew();
		FileDataSource fb = (FileDataSource) DataSource.get();
		fb.addProgressListener(pl);
		fb.removeProgressListener(pl);
		flag = false;
		fb.save(TestSettings.FILE_NAME);
		Assert.assertFalse(flag);
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
	}

	public void testAddDataSourceListener() throws DataSourceException, InterruptedException {
		// TODO: notification is asynchroneous, so need to synchronize somehow
		/*
		DataSourceListener dsl = new DataSourceListener() {
			public void modifiedStateChanged(DataSourceEvent e) {
				flag = ((DataSource) e.getSource()).isModified();
			}
		};
		DataSource.create("com.mmakowski.medley.data.FileDataSource", null);
		FileDataSource fb = (FileDataSource) DataSource.get();
		fb.addDataSourceListener(dsl);
		flag = false;
		Album.create();
		while (!flag);
		Assert.assertTrue(flag);
		DataSource.deactivate();
		*/
	}

	public void testRemoveDataSourceListener() {
		// TODO: notification is asynchroneous, so need to synchronize somehow
	}
	
}
