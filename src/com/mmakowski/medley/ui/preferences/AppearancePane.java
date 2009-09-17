/*
 * Created on 19-Feb-2005
 */
package com.mmakowski.medley.ui.preferences;

import java.util.prefs.Preferences;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.medley.ui.DataWindow;
import com.mmakowski.medley.ui.MainWindow;
import com.mmakowski.medley.ui.Settings;
import com.mmakowski.swt.widgets.BooleanPreferencesPaneElement;
import com.mmakowski.swt.widgets.ListPreferencesPaneElement;

/**
 * Preferences pane containing appearance settings.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/04/24 16:02:46 $
 */
public class AppearancePane extends MedleyBuiltPreferencesPane {
	
	protected MainWindow mainWnd;
	protected Button enhViewTabsBtn;
	protected Button rememberMainWndPosnBtn;
    protected Button rememberDataWndPosnBtn;
	protected Combo viewTabStyleCmb;
	protected int[] viewTabStyleVal;
	protected Combo viewTabSizeCmb;
	protected int[] viewTabSizeVal;
	
	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public AppearancePane(Composite parent, int style, MainWindow mainWnd) throws Exception {
		super(parent, style);
		this.mainWnd = mainWnd;
	}

    /**
     * @see com.mmakowski.medley.ui.preferences.MedleyBuiltPreferencesPane#initBuilder()
     */
    protected void initBuilder() throws MedleyException {
        // view tab style
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        builder.add(new ListPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "viewTabStyle"),
                Resources.getStr(this, "viewTabStyleToolTip"),
                new String[] {
                    Resources.getStr(this, "textOnly"),
                    Resources.getStr(this, "iconsOnly"),
                    Resources.getStr(this, "textAndIcons")
                },
                new int[] {
                    MainWindow.VIEW_TAB_TEXT,
                    MainWindow.VIEW_TAB_ICON,
                    MainWindow.VIEW_TAB_TEXT_ICON
                },
                MainWindow.VIEW_TAB_TEXT_ICON,
                MainWindow.PREF_VIEW_TABS_STYLE,
                ListPreferencesPaneElement.STYLE_COMBO
                ));
        // view tab size
        builder.add(new ListPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "viewTabSize"),
                Resources.getStr(this, "viewTabSizeToolTip"),
                new String[] {
                    Resources.getStr(this, "normal"),
                    Resources.getStr(this, "small")
                },
                new int[] {
                    Settings.VIEWTAB_HEIGHT_NORMAL,
                    Settings.VIEWTAB_HEIGHT_SMALL
                },
                Settings.VIEWTAB_HEIGHT_SMALL,
                MainWindow.PREF_VIEW_TABS_SIZE,
                ListPreferencesPaneElement.STYLE_COMBO
                ));
        // enhance view tabs
        builder.add(new BooleanPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "enhancedViewTabs"),
                Resources.getStr(this, "enhancedViewTabsToolTip"),
                true,
                MainWindow.PREF_VIEW_TABS_ENHANCED,
                BooleanPreferencesPaneElement.STYLE_CHECKBOX
                ));
        // remember main window position
        builder.add(new BooleanPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "restoreMainWndPosition"),
                Resources.getStr(this, "restoreMainWndPositionToolTip"),
                true,
                MainWindow.PREF_RESTORE_POSITION,
                BooleanPreferencesPaneElement.STYLE_CHECKBOX
                ));
        // remember main window position
        prefs = Preferences.userNodeForPackage(DataWindow.class);
        builder.add(new BooleanPreferencesPaneElement(
                prefs, 
                Resources.getStr(this, "restoreDataWndPosition"),
                Resources.getStr(this, "restoreDataWndPositionToolTip"),
                true,
                DataWindow.PREF_RESTORE_POSITION,
                BooleanPreferencesPaneElement.STYLE_CHECKBOX
                ));
    }

}
