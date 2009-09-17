/*
 * Created on 2004-08-07
 */
package com.mmakowski.medley.resources;

import com.mmakowski.medley.MedleyException;

/**
 * A resource exception in Medley.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $  $Date: 2004/08/07 11:06:10 $
 */
public class ResourceException extends MedleyException {

	/**
	 * @param errorCode
	 */
	public ResourceException(int errorCode) {
		super(errorCode);
	}

	/**
	 * @param errorCode
	 * @param msgArgs
	 */
	public ResourceException(int errorCode, Object[] msgArgs) {
		super(errorCode, msgArgs);
	}

	/**
	 * @param errorCode
	 * @param cause
	 */
	public ResourceException(int errorCode, Exception cause) {
		super(errorCode, cause);
	}

	/**
	 * @param errorCode
	 * @param msgArgs
	 * @param cause
	 */
	public ResourceException(int errorCode, Object[] msgArgs, Exception cause) {
		super(errorCode, msgArgs, cause);
	}

}
