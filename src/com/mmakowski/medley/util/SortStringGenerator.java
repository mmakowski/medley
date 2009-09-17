/*
 * Created on 25-Feb-2005
 */
package com.mmakowski.medley.util;

import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An interface for algorithms generating sort strings.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/04/10 20:55:46 $
 */
public abstract class SortStringGenerator {
	/**
	 * @param str a string
	 * @return a sort string for given string
	 */
	public abstract String toSortString(String str);
	
	// TODO: check name
	protected String toEnd(String str) {
		Vector rems = new Vector();
		rems.add("the");
		rems.add("a");
		rems.add("an");
		rems.add("le");
		rems.add("la");
		rems.add("der");
		rems.add("die");
		rems.add("das");
		
		StringTokenizer tokenizer = new StringTokenizer(str, " ");
		StringBuffer newStr = new StringBuffer();
		while(tokenizer.hasMoreTokens()) {
			String tok = tokenizer.nextToken();
		}
		return str;
	}
}
