/*
 * Created on 24-Jan-2005
 */
package com.mmakowski.medley.data;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;

import com.mmakowski.medley.resources.Errors;

/**
 * A class of objects to which other objects can delegate their
 * visible behaviour.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/04/24 16:02:45 $
 */
class VisibleDelegate implements Visible {

    /** logger */
    private static final Logger log = Logger.getLogger(HSQLDBConnector.class.getName());

    private DataSource ds;
	private String tag;
	
	/**
	 * Construct a visible delegate
	 * @param ds current data source
	 */
	public VisibleDelegate(DataSource ds, String tag) {
		this.ds = ds;
		this.tag = tag;
	}
	
	/**
	 * @return all image files assigned to this item
	 * @throws DataSourceException
	 */
	private File[] getImageFiles() throws DataSourceException {
		log.finest("getImageFiles()");
		// assume that images are stored in the filesystem
		File imageDir = ds.getImageDir(this);
		// find all matching images
		File[] imgs = imageDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(getTag());
			}
		});
		return imgs;
	}
	
	/**
	 * @return vector of ImageData for all images assigned to this musical item.
	 * @throws DataSourceException
	 */
	public Vector getImages() throws DataSourceException {
		log.finest("getImages()");
		File[] imgs = getImageFiles();
		// load images
		Vector v = new Vector();
		for (int i = 0; i < imgs.length; i++) {
			v.add(new ImageData(imgs[i]));
		}
		return v;
	}

	/**
	 * Add image to this musical item
	 * @param path path to image file
	 * @return ImageData for image added
	 */
	public ImageData addImage(String path) throws DataSourceException {
		ImageData data = new ImageData(path);
		return addImage(data);
	}
	
	/**
	 * Add image to this musical item.
	 * @param imageData the image data to be saved
	 * @throws DataSourceException
	 */
	public ImageData addImage(ImageData imageData) throws DataSourceException {
		log.finest("addImage(...)");
		// image file sent in imageData is the original image file.
		// Save the data to image directory under new name and update
		// image file in imageData.
		
		// assume that images are stored in the filesystem
		File imageDir = ds.getImageDir(this);
		// construct new file name
		int max = 0;
		File imageFile;
		String ext;
		switch (ds.getImageFormat()) {
		case SWT.IMAGE_PNG: ext = "png"; break;
		case SWT.IMAGE_GIF: ext = "gif"; break;
		case SWT.IMAGE_JPEG: ext = "jpg"; break;
		default: throw new DataSourceException(Errors.UNSUPPORTED_IMAGE_FORMAT_CONSTANT, new Object[] {new Integer(ds.getImageFormat())});
		}
		File[] imgs = getImageFiles();
		int start = getTag().length() + 1;
		for (int i = 0; i < imgs.length; i++) {
			try {
				int curr = Integer.parseInt(imgs[i].getName().substring(start, start + 3));
				if (curr > max) {
					max = curr;
				}
			} catch (NumberFormatException ex) {
				throw new DataSourceException(Errors.INCORRECT_IMAGE_FILE_NAME, new Object[] {imgs[i].getName()}, ex);
			}
		}
		DecimalFormat df = new DecimalFormat("000");
		imageFile = new File(imageDir.getPath() + "/" + getTag() + "-" + df.format(max + 1) + "." + ext);
		// write file
		ImageLoader il = new ImageLoader();
		il.data = new org.eclipse.swt.graphics.ImageData[] {imageData.getSWTImageData()};
		il.save(imageFile.getPath(), ds.getImageFormat());
		ds.addImage(this, imageData);
		imageData.setImageFile(imageFile);
		return imageData;
	}
	
	/**
	 * Remove given image
	 * @param imageData image data for the image to be removed
	 */
	public void removeImage(ImageData imageData) throws DataSourceException {
		if (!imageData.getImageFile().delete()) {
			throw new DataSourceException(Errors.CANT_DELETE_FILE, new Object[] {imageData.getImageFile()});
		}
		ds.removeImage(this, imageData);
	}

	/**
	 * @see com.mmakowski.medley.data.Visible#getTag()
	 */
	public String getTag() {
		return tag;
	}

}
