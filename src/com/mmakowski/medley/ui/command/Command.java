/*
 * Created on 24-Jan-2005
 */
package com.mmakowski.medley.ui.command;

import com.mmakowski.medley.MedleyException;

/**
 * An abstract command.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/02/05 10:13:28 $
 */
public abstract class Command {

	/**
	 * Execute this command.
	 * @throws MedleyException
	 */
	public abstract void execute() throws MedleyException;
	
	/**
	 * Undo this command.
	 * @throws MedleyException
	 */
	public abstract void undo() throws MedleyException;
	
	/**
	 * @return true if this command can be undone after executed
	 */
	public abstract boolean isReversible();
}
