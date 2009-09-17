/*
 * Created on 10-Apr-2005
 */
package com.mmakowski.medley.data;

import java.util.Date;
import java.util.Vector;

/**
 * A class of objects to which other object can delegate their Audible behaviour.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/05/22 00:07:38 $
 */
class AudibleDelegate implements Audible {

	/** audible type */
	protected int type;
	/** id of audible */
	protected int id;
	///** a vector of auditions */
	//protected Vector auditions;
	
	/**
	 * Construct an audible delegate
	 * @param type audible type
     * @param id audible id
	 */
	public AudibleDelegate(int type, int id) {
		this.type = type;
		this.id = id;
		//this.auditions = null;
	}

	/**
	 * @see com.mmakowski.medley.data.Audible#recordAudition(java.util.Date, int)
	 */
	public Audition recordAudition(Date dateTime, int subItemCount)
			throws DataSourceException {
	    Audition aud = Audition.create(type, id);
        if (dateTime != null) {
            aud.setDateTime(dateTime);
        }
        if (subItemCount > 0) {
            aud.setSubitemCount(subItemCount);
        }
        // insert new audition into auditions (at the correct position)
        //getAuditions(); // make sure auditions are loaded from the database
        // assert auditions != null;
        //int i = Collections.binarySearch(auditions, aud);
        // assert i < 0;
        //auditions.add(-i - 1, aud);
        return aud;
	}

	/**
	 * @see com.mmakowski.medley.data.Audible#getAuditions()
	 */
	public Vector getAuditions() throws DataSourceException {
		/*
		if (auditions == null) {
			auditions = loadAuditions();
		}
        */ 
		return loadAuditions(); //auditions;
	}

	/**
	 * @see com.mmakowski.medley.data.Audible#getAudibleType()
	 */
	public int getAudibleType() {
		return type;
	}
	
	/**
	 * Load auditions from database.
	 * @return vector of loaded auditions
	 * @throws DataSourceException
	 */
	protected Vector loadAuditions() throws DataSourceException {
		Vector aud = Audition.getAllFor(type, id);
		// assert aud != null;
        return aud;
	}

    /**
     * @see com.mmakowski.medley.data.Audible#hasSubitems()
     */
    public boolean hasSubitems() {
        return type != Audible.TRACK;
    }
    
    /**
     * Dispose of this AudibleDelegate
     * @throws DataSourceException
     */
    void dispose() throws DataSourceException {
        /*
        if (auditions != null) {
            DataObject.disposeAll(auditions);
        }
        */
    }
}
