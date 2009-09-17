/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley;

import com.mmakowski.medley.resources.Errors;
import com.mmakowski.medley.resources.Resources;

/**
 * A Medley application exception. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $  $Date: 2005/04/24 16:02:48 $
 */
public class MedleyException extends Exception {
	/** the error code for this exception */
	private int errorCode = Errors.NO_SUCH_ERROR;
	/** the arguments */
	protected Object[] msgArgs = null;
	
	/**
	 * Construct a MedleyException.
	 * @param errorCode the code of this exception
	 */
	public MedleyException(int errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * Construct a MedleyException.
	 * @param errorCode the code of this exception
	 * @param msgArgs the arguments of this exception
	 */
	public MedleyException(int errorCode, Object[] msgArgs) {
		this(errorCode);
		this.msgArgs = msgArgs;
	}
	
	/**
	 * Construct a MedleyException.
	 * @param errorCode the code of this exception
	 * @param cause an Exception that caused this excepton
	 */
	public MedleyException(int errorCode, Exception cause) {
		super(cause);
		this.errorCode = errorCode;
	}
	
	/**
	 * Construct a MedleyException.
	 * @param errorCode the code of this exception
	 * @param msgArgs the arguments of this exception
	 * @param cause an Exception that caused this excepton
	 */
	public MedleyException(int errorCode, Object[] msgArgs, Exception cause) {
		this(errorCode, cause);
		this.msgArgs = msgArgs;
	}
	
	/**
	 * @return a formatted error message for this exception
	 */
	public String getMessage() {
		try {
			return Resources.formatErrorMsg(errorCode, msgArgs);
		} catch (Exception ex) {
			// create appropriate message stating also the problem with resources
			return super.getMessage() + "\n\n" + 
				"(error encountered while trying to format error message for the exception above: " +
				ex.getMessage() + ")";
		}
	}
	
	/**
	 * @return error code
	 */
	public int getErrorCode() {
		return errorCode;
	}
}
