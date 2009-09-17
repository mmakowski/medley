/*
 * Created on 2004-04-07
 */
package com.mmakowski.medley.resources;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mmakowski.medley.Medley;

/**
 * The resources for Medley application.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $  $Date: 2005/04/14 22:00:44 $
 */
public final class Resources {
	/** the path to the error messages resource file */ 
	private static final String RB_ERROR_MESSAGES = "errorMessages";
	/** the path to the messages resource file */
	private static final String RB_CAPTIONS = "captions";

	protected static Logger log = Logger.getLogger(Medley.class.getName());
	
	/**
	 * Format the error message.
	 * @param errorCode the code of error
	 * @param args the arguments for the message
	 * @return a String conatining formatted error message
	 */
	public static final String formatErrorMsg(int errorCode, Object[] args) 
			throws Exception {
		String err = Errors.formatCode(errorCode);
		try {
			ResourceBundle errMsgs = ResourceBundle.getBundle(RB_ERROR_MESSAGES);
			return MessageFormat.format(errMsgs.getString(err), args);
		} catch (MissingResourceException ex) {
			log.log(Level.WARNING, "no description for error " + err, ex);
			throw new Exception("No description for error " + err + ".", ex);
		}
	}
	
	/**
	 * Return the message string.
	 * @param obj object for which message should be retrieved
	 * @param tag the message tag
	 * @return message string
	 * @throws ResourceException
	 */
	public static final String getStr(Object obj, String tag)
			throws ResourceException {			
		String otag = Captions.formatTag(obj, tag);
		return getStr(otag);
	}

	/**
	 * Return common message string.
	 * @param tag the message tag
	 * @return message string
	 * @throws ResourceException
	 */
	public static final String getStr(String tag)
			throws ResourceException {
		try {
			ResourceBundle msgs = ResourceBundle.getBundle(RB_CAPTIONS);
			return msgs.getString(tag);
		} catch (MissingResourceException ex) {
			log.log(Level.WARNING, "no string for tag \"" + tag + "\"", ex);
			throw new ResourceException(Errors.NO_STRING_FOR_TAG, new Object[] {tag}, ex);
		}
	}
	
	/**
	 * Format message.
	 * @param tag message tag
	 * @param args message arguments
	 * @return formatted message
	 * @throws ResourceException
	 */
	public static final String formatStr(Object obj, String tag, Object[] args)
			throws ResourceException {			
		String otag = Captions.formatTag(obj, tag);
		try {
			ResourceBundle msgs = ResourceBundle.getBundle(RB_CAPTIONS);
			return MessageFormat.format(msgs.getString(otag), args);
		} catch (MissingResourceException ex) {
			log.log(Level.WARNING, "no string for tag \"" + otag + "\"", ex);
			throw new ResourceException(Errors.NO_STRING_FOR_TAG, new Object[] {otag}, ex);
		}
	}

}
