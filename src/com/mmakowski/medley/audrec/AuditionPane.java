/*
 * Created on 30-May-2005
 */
package com.mmakowski.medley.audrec;

import java.util.logging.Logger;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.data.MusicalItem;
import com.mmakowski.medley.resources.Errors;
import com.mmakowski.medley.ui.ExceptionWindow;

/**
 * An abstract pane containing form with audition parameters.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/08/19 17:54:34 $
 */
abstract class AuditionPane extends Composite {

    /** logger */
    private static final Logger log = Logger.getLogger(AuditionPane.class.getName());
    
    /**
     * Create audition pane for given item type.
     * @param parent parent control
     * @param style pane style
     * @param itemType type of musical item
     * @return AuditionPane for given item type.
     * @throws MedleyException
     */
    public static AuditionPane create(Composite parent, int style, int itemType) throws MedleyException {
        switch (itemType) {
        case MusicalItem.RECORD:
            return new RecordAuditionPane(parent, style);
        default:
            log.severe("unsupported musical item type: " + itemType);
            throw new MedleyException(Errors.GENERAL_INTERNAL_ERROR, new Object[] {"unsupported musical item type " + itemType});
        }
    }
    
    /**
     * Construct an audition pane.
     * @param parent
     * @param style
     */
    protected AuditionPane(Composite parent, int style) {
        super(parent, style);
        try {
            initData();
            setLayout(new FormLayout());
            initWidgets();
        } catch (MedleyException ex) {
            (new ExceptionWindow(getDisplay(), ex)).show();
        }
    }
    
    /**
     * Record audition.
     * @return <code>true</code> if audition has been successfully created, false otherwise
     * @throws DataSourceException
     */
    abstract boolean recordAudition() throws DataSourceException;
    
    /**
     * Read in all the data required.
     * @throws MedleyException
     */
    protected abstract void initData() throws MedleyException;
    
    /**
     * Create the widgets.
     * @throws MedleyException
     */
    protected abstract void initWidgets() throws MedleyException;
}
