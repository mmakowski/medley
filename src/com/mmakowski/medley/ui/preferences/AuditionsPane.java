/*
 * Created on 21-May-2005
 */
package com.mmakowski.medley.ui.preferences;

import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.widgets.TextPreferencesPaneElement;

/**
 * Preferences for auditions.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/05/22 00:07:38 $
 */
public class AuditionsPane extends MedleyBuiltPreferencesPane {

    /**
     * @param parent
     * @param style
     * @throws Exception
     */
    public AuditionsPane(Composite parent, int style) throws Exception {
        super(parent, style);
    }

    /**
     * @see com.mmakowski.medley.ui.preferences.MedleyBuiltPreferencesPane#initBuilder()
     */
    protected void initBuilder() throws MedleyException {
        // date/time format
        Preferences prefs = Preferences.userNodeForPackage(com.mmakowski.medley.ui.AuditionsPane.class);
        TextPreferencesPaneElement dateTimeFormat = new TextPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "dateTimeFormat"),
                Resources.getStr(this, "dateTimeFormatToolTip"),
                "yyyy-MM-dd",
                com.mmakowski.medley.ui.AuditionsPane.PREF_DATE_TIME_FORMAT,
                TextPreferencesPaneElement.STYLE_DROP_DOWN
                ); 
        dateTimeFormat.addListElement("d MMM yyyy");
        dateTimeFormat.addListElement("yyyy-MM-dd HH:mm:ss");
        builder.add(dateTimeFormat);
    }

}
