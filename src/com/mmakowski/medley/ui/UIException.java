/*
 * Created on 19-Feb-2005
 */
package com.mmakowski.medley.ui;

import com.mmakowski.medley.MedleyException;

/**
 * An UI exception class. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/02/20 00:16:35 $
 */
public class UIException extends MedleyException {

	/**
	 * @param errorCode
	 */
	public UIException(int errorCode) {
		super(errorCode);
	}

	/**
	 * @param errorCode
	 * @param msgArgs
	 */
	public UIException(int errorCode, Object[] msgArgs) {
		super(errorCode, msgArgs);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public UIException(int errorCode, Exception cause) {
		super(errorCode, cause);
	}

	/**
	 * @param errorCode
	 * @param msgArgs
	 * @param cause
	 */
	public UIException(int errorCode, Object[] msgArgs, Exception cause) {
		super(errorCode, msgArgs, cause);
	}

}
