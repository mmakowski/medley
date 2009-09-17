/*
 * Created on 20-Mar-2005
 */
package com.mmakowski.medley.ui.preferences;

import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.RatingBasicInfoPane;
import com.mmakowski.swt.widgets.BooleanPreferencesPaneElement;
import com.mmakowski.swt.widgets.ListPreferencesPaneElement;

/**
 * Preferences for ratings.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.4 $ $Date: 2005/04/24 19:00:37 $
 */
public class RatingsPane extends MedleyBuiltPreferencesPane {
	
	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public RatingsPane(Composite parent, int style) throws Exception {
		super(parent, style);
	}

    /**
     * @see com.mmakowski.medley.ui.preferences.MedleyBuiltPreferencesPane#initBuilder()
     */
    protected void initBuilder() throws MedleyException {
        // confirm delete latest score
        Preferences prefs = Preferences.userNodeForPackage(com.mmakowski.medley.ui.RatingsPane.class);
        builder.add(new BooleanPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "confirmDeleteLatestScore"),
                Resources.getStr(this, "confirmDeleteLatestScoreToolTip"),
                true,
                com.mmakowski.medley.ui.RatingsPane.PREF_CONFIRM_DELETE_LATEST_SCORE,
                BooleanPreferencesPaneElement.STYLE_CHECKBOX
                ));
        // what to do when user changes score range
        prefs = Preferences.userNodeForPackage(RatingBasicInfoPane.class);
        builder.add(new ListPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "onRangeChange"),
                Resources.getStr(this, "onRangeChangeToolTip"),
                new String[] {
                    Resources.getStr(this, "dontScale"),
                    Resources.getStr(this, "scale"),
                    Resources.getStr(this, "askUser")
                },
                new int[] {
                    RatingBasicInfoPane.RANGE_CHANGE_DONT_SCALE,
                    RatingBasicInfoPane.RANGE_CHANGE_SCALE,
                    RatingBasicInfoPane.RANGE_CHANGE_ASK_USER
                },
                RatingBasicInfoPane.RANGE_CHANGE_ASK_USER,
                RatingBasicInfoPane.PREF_ON_RANGE_CHANGE,
                ListPreferencesPaneElement.STYLE_COMBO
                ));
    }

}
