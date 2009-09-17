/*
 * Created on 2004-08-07
 */
package com.mmakowski.medley.resources;

/**
 * A class containing caption tags for the whole application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $  $Date: 2004/08/07 11:06:10 $
 */
public final class Captions {

	/**
	 * @param obj the object for which caption should be found
	 * @param tag the caption tag
	 * @return the full tag including the class information
	 */
	public static String formatTag(Object obj, String tag) {
		String className = obj.getClass().getName();
		// remove leading package path if the class is in the Medley UI package
		if (className.substring(0, 24).equals("com.mmakowski.medley.ui.")) {
			className = className.substring(24);
		}
		// remove trailing $... (for anonymous classes)
		int spos = className.indexOf("$");
		if (spos > 0) {
			className = className.substring(0, spos);
		}
		return className + "." + tag;
	}
}
