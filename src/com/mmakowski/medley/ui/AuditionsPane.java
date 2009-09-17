/*
 * Created on 21-May-2005
 */
package com.mmakowski.medley.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Audible;
import com.mmakowski.medley.data.Audition;
import com.mmakowski.medley.data.DataSourceException;
import com.mmakowski.medley.resources.ResourceException;
import com.mmakowski.medley.resources.Resources;
import com.mmakowski.swt.events.IntegerVerifier;

/**
 * A pane presenting a list of auditions of a particular Audible. 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/05/22 00:07:38 $
 */
public class AuditionsPane extends Composite {
    public static final String PREF_DATE_TIME_FORMAT = "/auditions_pane/date_time_format";

    protected static final String AUDITION_ID = "id";

    private static final int COL_DATETIME = 0;
    private static final int COL_SUBITEMS = 1;
    private static final int COL_DELETE = 2;
    
    /** preferences */
    private static final Preferences prefs = Preferences.userNodeForPackage(AuditionsPane.class);
    /** logger */
    private static final Logger log = Logger.getLogger(AuditionsPane.class.getName());

    /** the table */
    private Table table;
    /** "add artist" button */
    private Button addAuditionBtn;
    /** are the values editable? */
    private boolean editable;
    /** the album/record/track */
    private Audible audible;
    /** date/time format */
    private DateFormat dtFormat; 
    
    /**
     * Construct the pane.
     * @param parent the parent control
     * @param style the style of this pane
     * @param editable should the controls be editable
     * @param audible the item whose auditions are presented
     */
    public AuditionsPane(Composite parent, int style, 
                         boolean editable, Audible audible) throws MedleyException {
        super(parent, style);
        this.editable = editable;
        this.audible = audible;
        this.dtFormat = new SimpleDateFormat(prefs.get(PREF_DATE_TIME_FORMAT, "yyyy-MM-dd"));
        FormLayout layout = new FormLayout();
        setLayout(layout);
        initWidgets();
    }

    /**
     * Initialize the widgets in the pane.
     */
    protected void initWidgets() throws MedleyException {
        table = new Table (this, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
        // add columns
        TableColumn column = new TableColumn (table, SWT.NULL);
        column.setText(Resources.getStr(this, "dateTime"));
        column.setWidth(100);
        column = new TableColumn (table, SWT.NULL);
        if (audible.hasSubitems()) {
            column.setText(Resources.getStr(this, "subitemCount." + audible.getAudibleType()));
            column.setWidth(40);
        } else {
            column.setWidth(0);
            column.setResizable(false);
        }
        column = new TableColumn (table, SWT.NULL);
        column.setText(Resources.getStr(this, "actions"));
        column.setWidth(Settings.ACTION_COLUMN_WIDTH);
        column.setResizable(false);
    
        final TableEditor dateTimeEditor = new TableEditor(table);
        dateTimeEditor.grabHorizontal = true;
        final TableEditor subitemsEditor = new TableEditor(table);
        subitemsEditor.grabHorizontal = true;
        final TableEditor deleteEditor = new TableEditor(table);
        deleteEditor.grabHorizontal = true;

        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e){
                if (!editable) {
                    return;
                }
                
                // Clean up any previous editor control
                Control oldEditor = dateTimeEditor.getEditor();
                if (oldEditor != null) oldEditor.dispose();
                oldEditor = subitemsEditor.getEditor();
                if (oldEditor != null) oldEditor.dispose();
                oldEditor = deleteEditor.getEditor();
                if (oldEditor != null) oldEditor.dispose();
    
                // Identify the selected row
                final TableItem titem = (TableItem)e.item;
                if (titem == null) return;
                final int auditionId = ((Integer) titem.getData(AUDITION_ID)).intValue(); 
                
                // the editor for date/time
                Text dateTime = new Text(table, SWT.FLAT);
                dateTime.setText(titem.getText(COL_DATETIME));
                dateTime.addFocusListener(new FocusAdapter() {
                    public void focusLost(FocusEvent ev) {
                        Text txt = (Text) dateTimeEditor.getEditor();
                        dateTimeEditor.getItem().setText(COL_DATETIME, txt.getText());
                        // save the change to the model
                        try {
                            // update the audition
                            Date dt = null;
                            try {
                                dt = dtFormat.parse(txt.getText());
                                Audition aud = Audition.load(audible.getAudibleType(), auditionId);
                                aud.setDateTime(dt);
                                aud.dispose();
                            } catch (ParseException ex) {
                                MessageBox mb = new MessageBox(getShell(), 
                                           SWT.ICON_WARNING | SWT.OK);
                                mb.setText(Resources.getStr(this, "mb.incorrectDateTimeFormat.title"));
                                mb.setMessage(Resources.getStr(this, "mb.incorrectDateTimeFormat.msg"));
                                mb.open();
                            }
                        } catch (MedleyException ex) {
                            (new ExceptionWindow(getDisplay(), ex)).show();                         
                        }
                        
                    }
                });
                dateTimeEditor.setEditor(dateTime, titem, COL_DATETIME);
    
                boolean hasSubsTmp;
                try {
                    hasSubsTmp = audible.hasSubitems();
                } catch (DataSourceException ex) {
                    (new ExceptionWindow(getDisplay(), ex)).show();
                    hasSubsTmp = false;
                }
                final boolean hasSubs = hasSubsTmp;
                if (hasSubs) {
                    // the editor for subitem count
                    Text subitems = new Text(table, SWT.FLAT);
                    subitems.setText(titem.getText(COL_SUBITEMS));
                    // NOTE: using arbitrary upper bound for subitem count. Might consider reading actual number of subitems.
                    subitems.addVerifyListener(new IntegerVerifier(1, 1000));
                    subitems.addFocusListener(new FocusAdapter() {
                        public void focusLost(FocusEvent ev) {
                            Text txt = (Text) subitemsEditor.getEditor();
                            subitemsEditor.getItem().setText(COL_SUBITEMS, txt.getText());
                            // save the change to the model
                            try {
                                // update the audition
                                int count = 0;
                                try {
                                    count = Integer.parseInt(txt.getText());
                                    Audition aud = Audition.load(audible.getAudibleType(), auditionId);
                                    aud.setSubitemCount(count);
                                    aud.dispose();
                                } catch (NumberFormatException ex) {
                                    MessageBox mb = new MessageBox(getShell(), 
                                               SWT.ICON_WARNING | SWT.OK);
                                    mb.setText(Resources.getStr(this, "mb.incorrectNumberFormat.title"));
                                    mb.setMessage(Resources.getStr(this, "mb.incorrectNumberFormat.msg"));
                                    mb.open();
                                }
                            } catch (MedleyException ex) {
                                (new ExceptionWindow(getDisplay(), ex)).show();                         
                            }
                            
                        }
                    });
                    subitemsEditor.setEditor(subitems, titem, COL_SUBITEMS);
                }
    
                // the delete button
                Button button = new Button(table, SWT.FLAT);
                try {
                    button.setText(Resources.getStr(this, "delete"));
                } catch (ResourceException ex) {
                    (new ExceptionWindow(getDisplay(), ex)).show();                 
                }
                button.addListener(SWT.Selection, new Listener() {
                    public void handleEvent (Event e) {
                        try {
                            Audition.delete(audible.getAudibleType(),
                                    auditionId);
                        } catch (MedleyException ex) {
                            (new ExceptionWindow(getDisplay(), ex)).show();                         
                        }
                        table.remove(table.indexOf(titem));
                        table.deselectAll();
                        dateTimeEditor.getEditor().dispose();
                        if (hasSubs) {
                            subitemsEditor.getEditor().dispose();
                        }
                        deleteEditor.getEditor().dispose();
                    }
                });
                deleteEditor.setEditor(button, titem, COL_DELETE);
            }
        });        
    
        FormData data = new FormData();
        data.top = new FormAttachment(0, Settings.MARGIN_TOP);
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM - 
                                              Settings.BUTTON_HEIGHT - 
                                              Settings.ITEM_SPACING_V);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.right = new FormAttachment(100, -Settings.MARGIN_RIGHT);
        table.setLayoutData(data);
        
        // add artist button
        addAuditionBtn = new Button(this, SWT.NONE);
        addAuditionBtn.setText(Resources.getStr(this, "addAudition"));
        addAuditionBtn.addListener(SWT.Selection, new Listener() {
            public void handleEvent (Event e) {
                try {
                    // record audition with default parameters
                    Audition aud = audible.recordAudition(null, 0);
                    TableItem item = new TableItem(table, SWT.NULL);
                    item.setText(COL_DATETIME, dtFormat.format(aud.getDateTime()));          
                    item.setText(COL_SUBITEMS, String.valueOf(aud.getSubitemCount()));        
                    item.setData(AUDITION_ID, new Integer(aud.getId()));
                    aud.dispose(); 
                    table.setSelection(new TableItem[] {item});
                } catch (MedleyException ex) {
                    (new ExceptionWindow(getDisplay(), ex)).show();
                }
            }
        });
        data = new FormData();
        data.bottom = new FormAttachment(100, -Settings.MARGIN_BOTTOM);
        data.left = new FormAttachment(0, Settings.MARGIN_LEFT);
        data.width = Settings.BUTTON_WIDTH;
        data.height = Settings.BUTTON_HEIGHT;
        addAuditionBtn.setLayoutData(data);

        // fill in table contents
        refresh();
    }    
    
    /**
     * Refresh the contents of artists table.
     * @throws MedleyException
     */
    protected void refresh() throws MedleyException {
        table.removeAll();
        Vector auds = audible.getAuditions();
        for (Iterator i = auds.iterator(); i.hasNext();) {
            TableItem item = new TableItem (table, SWT.NULL);
            Audition aud = (Audition) i.next();
            item.setText(COL_DATETIME, dtFormat.format(aud.getDateTime()));           
            item.setText(COL_SUBITEMS, String.valueOf(aud.getSubitemCount()));        
            item.setData(AUDITION_ID, new Integer(aud.getId()));
        }
        Audition.disposeAll(auds);
        // do not dispose of auds since audible caches them.
    }
    
}
