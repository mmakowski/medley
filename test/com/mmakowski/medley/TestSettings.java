/*
 * Created on 26-Jan-2005
 */
package com.mmakowski.medley;

/**
 * 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.3 $ $Date: 2005/04/14 22:00:44 $
 */
public abstract class TestSettings {
	public static final String FILE_NAME = "junit/___testfile.zmd";
	public static final String OLD_FILE_NAME = "junit/old.zmd";
	public static final String ORIGINAL_FILE_NAME = "test.zmd";
	public static final String LOG_PATH = "junit/test.log";
	public static final String TEST_STRING = ";$'\"%{<(&# ?????Ó??????Ô ÕÖÙÚÛ ÜÀÁÂÃÄÅÆÈÉÊË ?????ó ???ßàáâãäåæçè éêëðñôõùúûüö?'" +
			";$'\"%{<(&# ?????Ó??????Ô ÕÖÙÚÛ ÜÀÁÂÃÄÅÆÈÉÊË ?????ó ???ßàáâãäåæçè éêëðñôõùúûüö?'";
	public static final String TEST_STRING_MULTILINE = TEST_STRING + "\n" + TEST_STRING + "\n\n" + TEST_STRING + "\n";
}
