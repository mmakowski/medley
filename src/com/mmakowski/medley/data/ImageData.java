/*
 * Created on 04-Jan-2005
 */
package com.mmakowski.medley.data;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageLoader;

/**
 * Class storing image information.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.2 $ $Date: 2005/04/24 16:02:45 $
 */
public class ImageData {
	/** the underlying image data */
	protected org.eclipse.swt.graphics.ImageData imageData;
	/** path to the image file */
	protected File imageFile;
	
	/**
	 * Create ImageData
	 * @param imageFile image file
	 */
	public ImageData(File imageFile) {
		this.imageData = new org.eclipse.swt.graphics.ImageData(imageFile.getPath());
		this.imageFile = imageFile;
	}
	
	/**
	 * Create ImageData
	 * @param filePath path to the image file
	 */
	public ImageData(String filePath) {
		this.imageData = new org.eclipse.swt.graphics.ImageData(filePath);
		this.imageFile = new File(filePath);
	}
	
	/**
	 * @return underlying SWT image data
	 */
	public org.eclipse.swt.graphics.ImageData getSWTImageData() {
		return imageData;
	}
	
	/**
	 * @return return image file
	 */
	public File getImageFile() {
		return imageFile;
	}
	
	/**
	 * @param f new image file
	 */
	public void setImageFile(File f) {
		imageFile = f;
	}
	
	/**
	 * Export this image to a JPEG file
	 * @param path
	 */
	public void export(String path) {
		ImageLoader il = new ImageLoader();
		il.data = new org.eclipse.swt.graphics.ImageData[] {imageData};
		il.save(path, SWT.IMAGE_JPEG);
	}
}
