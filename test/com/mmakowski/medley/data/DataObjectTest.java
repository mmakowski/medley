/*
 * Created on 26-Jan-2005
 */
package com.mmakowski.medley.data;

import com.mmakowski.io.File;
import com.mmakowski.medley.MedleyTest;
import com.mmakowski.medley.TestSettings;

/**
 * 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/02/19 10:30:30 $
 */
public abstract class DataObjectTest extends MedleyTest {

	public DataObjectTest() {
		super();
	}

	public DataObjectTest(String test) {
		super(test);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		File f = new File(TestSettings.ORIGINAL_FILE_NAME);
		f.copyTo(new File(TestSettings.FILE_NAME));
		FileDataSource.create(TestSettings.FILE_NAME);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		DataSource.deactivate();
		File f = new File(TestSettings.FILE_NAME);
		f.delete();
		super.tearDown();
	}
	
}
