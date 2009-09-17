/*
 * Created on 04-Jan-2005
 */
package com.mmakowski.medley.data;

import java.util.Vector;

/**
 * An interface for all the data objects that can have
 * images associated with them 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/01/04 17:48:34 $
 */
public interface Visible {
	/** @return string tag identifying this object */
	String getTag();
	/** @return a vector of ImageData objects for this item */
	Vector getImages() throws DataSourceException;
	/** 
	 * Add given image to the collection of this item's images.
	 * @param imageData the ImageData object for image to be added
	 * @return ImageData object for image added
	 * @throws DataSourceException
	 */
	ImageData addImage(ImageData imageData) throws DataSourceException;
	/**
	 * Remove given image from the data source
	 * @param imageData the image to be removed
	 * @throws DataSourceException
	 */
	void removeImage(ImageData imageData) throws DataSourceException;
	/**
	 * Add given image to the collection fo this item's images.
	 * @param path path to the image file
	 * @return ImageData object for image added
	 * @throws DataSourceException
	 */
	ImageData addImage(String path) throws DataSourceException;
}
