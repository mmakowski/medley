/*
 * Created on 24-Apr-2005
 */
package com.mmakowski.medley.ui.preferences;

import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.swt.widgets.PreferencesPaneBuilder;

/**
 * A preferences pane that is being built using PreferencesPaneBuilder
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/04/24 16:02:46 $
 */
abstract class MedleyBuiltPreferencesPane extends MedleyPreferencesPane {

    //protected Button confirmDeleteLatestScoreBtn;
    protected PreferencesPaneBuilder builder; 
    
    /**
     * @param parent
     * @param style
     * @throws Exception
     */
    public MedleyBuiltPreferencesPane(Composite parent, int style) throws Exception {
        super(parent, style);
    }

    /**
     * @see com.mmakowski.swt.widgets.PreferencesPane#initWidgets()
     */
    protected void initWidgets() throws MedleyException {
        builder = new PreferencesPaneBuilder(this);
        initBuilder();
        builder.buildWidgets();
    }

    /**
     * Add elements to builder. 
     * @throws MedleyException 
     */
    protected abstract void initBuilder() throws MedleyException;

    /**
     * @see com.mmakowski.swt.widgets.PreferencesPane#disposeWidgets()
     */
    public void disposeWidgets() throws Exception {
        builder.disposeWidgets();
    }

    /**
     * @see com.mmakowski.swt.widgets.PreferencesPane#restoreDefaults()
     */
    public void restoreDefaults() {
        builder.restoreDefaults();
    }

    /**
     * @see com.mmakowski.swt.widgets.PreferencesPane#restoreCurrentSettings()
     */
    public void restoreCurrentSettings() {
        builder.restoreCurrentSettings();
    }

    /**
     * @see com.mmakowski.swt.widgets.PreferencesPane#apply()
     */
    public void apply() {
        builder.apply();
    }
    
}
