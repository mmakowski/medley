/*
 * Created on 2004-08-08
 */
package com.mmakowski.medley.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Class containing the UI defaults for Medley.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.8 $  $Date: 2005/05/22 00:07:38 $
 */
public class Settings extends com.mmakowski.swt.Settings {
	/** time control height */
    public static final int TIME_HEIGHT;
    /** cover image preview size */
	public static final int THUMBNAIL_MAX_WIDTH = 200;
	public static final int THUMBNAIL_MAX_HEIGHT = 200;
	/** view tab heights */
	public static final int VIEWTAB_HEIGHT_SMALL = 20;
	public static final int VIEWTAB_HEIGHT_NORMAL = 36;
    
    /**
     * Compute preferred control heights.
     */
    static {
        Shell shell = new Shell(Display.getCurrent());
        Time tim = new Time(shell, SWT.NONE);
        TIME_HEIGHT = tim.computeSize(100, SWT.DEFAULT, true).y;
        tim.dispose();
        shell.dispose();
    }
    
    // a protected constructor to make this class non-instantiable
    // but allow inheritance
    protected Settings() {}
    
}
