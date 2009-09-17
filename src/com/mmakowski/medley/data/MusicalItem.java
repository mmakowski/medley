/*
 * Created on 15-Oct-2004
 */
package com.mmakowski.medley.data;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A common superclass for albums, records and tracks.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.17 $ $Date: 2005/05/22 00:07:38 $
 */
public abstract class MusicalItem extends DataObject implements Visible, Taggable, Ratable, Audible {
	
	// types of elements artists can play on
	public static final int ALBUM = DataObject.ALBUM;
	public static final int RECORD = DataObject.RECORD;
	public static final int TRACK = DataObject.TRACK;

    /** logger */
    private static final Logger log = Logger.getLogger(MusicalItem.class.getName());
    
	/**
	 * @param type type of muical item (ALBUM/RECORD/TRACK)
	 * @param id id of musical item
	 * @return object for give type and id
	 * @throws DataSourceException
	 */
	static MusicalItem load(int type, int id) throws DataSourceException {
		log.finest("load(" + type + ", " + id + ")");

		switch (type) {
		case ALBUM:
			return Album.load(id);
		case RECORD:
			return Record.load(id);
		case TRACK:
			return Track.load(id);
		default:
			log.log(Level.SEVERE, "Unsupported musical item type value: " + id);
			return null;
		}
	}
	
	/** the cache for main artists */
	protected String artistCache;
	/** the cache for main artists sort string */
	protected String artistSortString;
	/** visible delegate */
	protected VisibleDelegate visible;
	/** taggable delegate */
	protected TaggableDelegate taggable;
	/** ratable delegate */
	protected RatableDelegate ratable;
    /** audible delegate */
    protected AudibleDelegate audible;
	
	/**
	 * Construct a data object.
	 * @throws DataSourceException if registering with a
	 * JDBCDataSource fails
	 */
	protected MusicalItem() throws DataSourceException {
		super();
	}
	
	/** @return the type tag for this item */
	public abstract int getType();
	
	/** @return this item's id */
	public abstract int getId();
	
	/**
	 * Load tag values from database.
	 * @throws DataSourceException
	 */
	protected abstract void loadTags() throws DataSourceException;
	
	/** 
	 * Load rating scores from database.
	 * @throws DataSourceException
	 */
	protected abstract void loadRatings() throws DataSourceException;
	
    /**
     * Load auditions from database.
     * @throws DataSourceException
     */
	protected void loadAuditions() throws DataSourceException {
        audible = new AudibleDelegate(getType(), getId());
    }

    /**
	 * @return a string tag representing this musical item 
	 */
	public String getTag() {
		String tag = "";
		switch (getType()) {
		case ALBUM:	tag = "album"; break;
		case RECORD: tag = "record"; break;
		case TRACK: tag = "track"; break;
		}
		DecimalFormat df = new DecimalFormat("0000000");
		return tag + "-" + df.format(getId());
	}
	
	/**
	 * Import artists from subitems. 
	 * @throws DataSourceException
	 */
	public abstract void importArtistsUp() throws DataSourceException;
	
	/**
	 * Import artists from superitem. 
	 * @throws DataSourceException
	 */
	public abstract void importArtistsDown() throws DataSourceException;

	/**
	 * Import all the artists from given set of musical items.
	 * @param items a vector of MusicalItems
	 * @throws DataSourceException
	 */
	protected void importArtists(Vector items) throws DataSourceException {
	    log.finest("importArtists(" + items + ")");
        for (Iterator i = items.iterator(); i.hasNext();) {
			MusicalItem item = (MusicalItem) i.next();
			importArtists(item);
		}
	}

	/**
	 * Import all artists from given musical item.
	 * @param item the item to import artists from
	 * @throws DataSourceException
	 */
	protected void importArtists(MusicalItem item) throws DataSourceException {
	    log.finest("importArtists(" + item + ")");
        Vector artists = item.getArtists();
		for (Iterator j = artists.iterator(); j.hasNext();) {
			ArtistRole artist = (ArtistRole) j.next();
			ArtistRole copy = artist.createCopy(this);
			if (copy != null) {
				copy.dispose();
			}
		}
		disposeAll(artists);
	}
	
	/**
	 * @return the artists for this item
	 */
	public Vector getArtists() throws DataSourceException {
		log.finest("getArtists()");
        return ArtistRole.getAllFor(getType(), getId());
	}

	/**
	 * @return a string representing all the main artists on this item (based on cache)
	 */
	public String getMainArtistString() throws DataSourceException {
		return artistCache == null ? "" : artistCache;
	}
	
	/**
	 * @return Returns the artistSortString.
	 */
	public String getArtistSortString() {
		return artistSortString;
	}

	/**
	 * @return a string representing all the main artists on this item
	 * @throws DataSourceException
	 */
	protected String calculateMainArtistString() throws DataSourceException {
		log.finest("calculateMainArtistString()");
		Vector ma = ArtistRole.getMainFor(getType(), getId());
		if (ma.isEmpty()) {
			return "";
		}
		Iterator i = ma.iterator();
		ArtistRole ar = (ArtistRole) i.next();
		String mastr = ar.getArtistName();
		ar.dispose();
		while (i.hasNext()) {
			ar = (ArtistRole) i.next();
			mastr += ", " + ar.getArtistName();
			ar.dispose();
		}
		return mastr;
	}
	
	/**
	 * @return a string representing sort name for all main artists
	 * @throws DataSourceException
	 */
	protected String calculateArtistSortString() throws DataSourceException {
		log.finest("calculateArtistSortString()");
		Vector ma = ArtistRole.getMainFor(getType(), getId());
		if (ma.isEmpty()) {
			return "";
		}
		String mastr = "";
		Iterator i = ma.iterator();
		while (i.hasNext()) {
			ArtistRole ar = (ArtistRole) i.next();
			mastr += ar.getArtistSortName();
			ar.dispose();
		}
		return mastr;
	}

	/**
	 * Refresh the cache for main artists.
	 * @throws DataSourceException
	 */
	public void refreshMainArtistCache() throws DataSourceException {
        log.finest("refreshMainArtistCache()");
		artistCache = calculateMainArtistString();
		artistSortString = calculateArtistSortString();
		setModified();
	}
	
	/**
	 * @see com.mmakowski.medley.data.Taggable#getTaggableType()
	 */
	public int getTaggableType() {
		return getType();
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#setTagValue(int, java.lang.String)
	 */
	public void setTagValue(int tagId, String value) throws DataSourceException {
		if (taggable == null) {
			loadTags();
		}
		taggable.setTagValue(tagId, value);
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.Taggable#getTagValue(int)
	 */
	public String getTagValue(int tagId) throws DataSourceException {
		if (taggable == null) {
			loadTags();
		}
		return taggable.getTagValue(tagId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatableType()
	 */
	public int getRatableType() {
		return getType();
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#setRatingScore(int, java.lang.String)
	 */
	public void setRatingScore(int ratingId, String score) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		ratable.setRatingScore(ratingId, score);
		attributeChanged();
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScore(int)
	 */
	public Integer getRatingScore(int ratingId) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		return ratable.getRatingScore(ratingId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#getRatingScoreString(int)
	 */
	public String getRatingScoreString(int ratingId) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		return ratable.getRatingScoreString(ratingId);
	}

	/**
	 * @see com.mmakowski.medley.data.Ratable#deleteLatestRatingScore(int)
	 */
	public void deleteLatestRatingScore(int ratingId) throws DataSourceException {
		if (ratable == null) {
			loadRatings();
		}
		ratable.deleteLatestRatingScore(ratingId);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#getImages()
	 */
	public Vector getImages() throws DataSourceException {
		return visible.getImages();
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#addImage(com.mmakowski.medley.data.ImageData)
	 */
	public ImageData addImage(ImageData imageData) throws DataSourceException {
		log.finest("addImage(" + imageData + ")");
        return visible.addImage(imageData);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#removeImage(com.mmakowski.medley.data.ImageData)
	 */
	public void removeImage(ImageData imageData) throws DataSourceException {
        log.finest("removeImage(" + imageData + ")");
		visible.removeImage(imageData);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#addImage(java.lang.String)
	 */
	public ImageData addImage(String path) throws DataSourceException {
        log.finest("addImage(\"" + path + "\")");
		return visible.addImage(path);
	}

    /**
     * @see com.mmakowski.medley.data.Audible#getAudibleType()
     */
    public int getAudibleType() {
        return audible.getAudibleType();
    }
    
    /**
     * @see com.mmakowski.medley.data.Audible#getAuditions()
     */
    public Vector getAuditions() throws DataSourceException {
        if (audible == null) {
            loadAuditions();
        }
        return audible.getAuditions();
    }
    
    /**
     * @see com.mmakowski.medley.data.Audible#hasSubitems()
     */
    public boolean hasSubitems() throws DataSourceException {
        if (audible == null) {
            loadAuditions();
        }
        return audible.hasSubitems();
    }

    /**
     * @see com.mmakowski.medley.data.Audible#recordAudition(java.util.Date, int)
     */
    public Audition recordAudition(Date dateTime, int subItemCount)
            throws DataSourceException {
        if (audible == null) {
            loadAuditions();
        }
        return audible.recordAudition(dateTime, subItemCount);
    }

    
    /**
     * @see com.mmakowski.medley.data.DataObject#dispose()
     */
    public void dispose() throws DataSourceException {
        if (audible != null) {
            audible.dispose();
        }
        super.dispose();
    }
}
