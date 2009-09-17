/*
 * Created on 26-Jan-2005
 */
package com.mmakowski.medley;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/04/14 22:00:44 $
 */
public abstract class MedleyTest extends TestCase {

	public MedleyTest() {
		super();
	}
	
	public MedleyTest(String test) {
		super(test);
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
