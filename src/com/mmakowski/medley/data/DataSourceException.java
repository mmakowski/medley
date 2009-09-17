/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley.data;

import java.sql.SQLException;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Errors;

/**
 * An exception that can be thrown by a data source.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $  $Date: 2004/04/09 22:27:21 $
 */
public class DataSourceException extends MedleyException {

	/**
	 * Checks if given exception was caused by a DataSourceException and
	 * if this is the case, throw the cause of given exception. Otherwise 
	 * return false. 
	 * @param ex an Exception
	 * @return false if the cause of ex was not a DataSourceException
	 * @throws DataSourceException if the cause of ex was a DataSourceException
	 */
	public static boolean unpack(Exception ex) throws DataSourceException {
		if (ex.getCause() instanceof DataSourceException) {
			throw (DataSourceException) ex.getCause();
		}
		return false;
	}
	/**
	 * Construct a DataSourceException.
	 * @param errorCode the code of this exception
	 */
	public DataSourceException(int errorCode) {
		super(errorCode);
	}
	
	/**
	 * Construct a DataSourceException.
	 * @param errorCode the code of this exception
	 * @param msgArgs the arguments of this exception
	 */
	public DataSourceException(int errorCode, Object[] msgArgs) {
		super(errorCode, msgArgs);
	}
	
	/**
	 * Construct a DataSourceException.
	 * @param errorCode the code of this exception
	 * @param cause an Exception that caused this excepton
	 */
	public DataSourceException(int errorCode, Exception cause) {
		super(errorCode, cause);
	}
	
	/**
	 * Construct a DataSourceException.
	 * @param errorCode the code of this exception
	 * @param msgArgs the arguments of this exception
	 * @param cause an Exception that caused this excepton
	 */
	public DataSourceException(int errorCode, Object[] msgArgs, Exception cause) {
		super(errorCode, msgArgs, cause);

	}
	
	/**
	 * Construct a DataSourceException
	 * @param cause an SQLException that caused this exception
	 */
	public DataSourceException(SQLException cause) {
		super(Errors.GENERAL_SQL_ERROR, cause);
		Object args[] = {cause.getMessage()};
		msgArgs = args;
	}
}
