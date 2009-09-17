/*
 * Created on 30-May-2005
 */
package com.mmakowski.medley.audrec;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.mmakowski.medley.MedleyException;
import com.mmakowski.medley.data.Record;
import com.mmakowski.medley.ui.Settings;

/**
 * A pane with record audition parameters.
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/08/19 17:54:34 $
 */
class RecordAuditionPane extends AuditionPane {

    private static final int ARTIST_COMBO_WIDTH = 160;
    private static final int RECORD_COMBO_WIDTH = 160;
    
    private static final String KEY_ID = "id";
    private static final String KEY_TRACK_COUNT = "trackCount";
    
    /**
     * A class to store information about artist's records 
     */
    private class ArtistRecords {
        Vector ids = new Vector();
        Vector titles = new Vector();
        Vector trackCounts = new Vector();
        void add(int id, String title, int trackCount) {
            ids.add(new Integer(id));
            titles.add(title);
            trackCounts.add(new Integer(trackCount));
        }
    }
    

    /** a combo box containing artists */
    private Combo artistCmb;
    /** a combo box containing records */ 
    private Combo recordCmb;
    /** a map mapping artist name to a list of records for this artist */
    private Hashtable recordMap;
    /** a list of artists */
    private Vector artists;
    /** last selected artist */
    private int prevSelArtist;
    
    /**
     * @param parent
     * @param style
     */
    public RecordAuditionPane(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * @see com.mmakowski.medley.audrec.AuditionPane#recordAudition()
     */
    boolean recordAudition() {
        // TODO: record audition
        return false;
    }

    /**
     * @see com.mmakowski.medley.audrec.AuditionPane#initData()
     */
    protected void initData() throws MedleyException {
        recordMap = new Hashtable();
        artists = new Vector();
        // Read in the list of records
        Vector recs = Record.getAllRecords();
        // we assume that records are returned sorted by artist
        for (Iterator i = recs.iterator(); i.hasNext();) {
            Record r = (Record) i.next();
            String artist = r.getMainArtistString();
            // add this record to the list of records of appropriate artist
            if (!recordMap.containsKey(artist)) {
                recordMap.put(artist, new ArtistRecords());
            }
            if (!artists.contains(artist)) {
                artists.add(artist);
            }
            ArtistRecords artRecs = (ArtistRecords) recordMap.get(artist);
            artRecs.add(r.getId(), r.getCompositeTitle(), r.getTrackCount());
        }
        Record.disposeAll(recs);
    }

    /**
     * @see com.mmakowski.medley.audrec.AuditionPane#initWidgets()
     */
    protected void initWidgets() throws MedleyException {
        int top = 0;
        int left = 0;
        artistCmb = new Combo(this, SWT.DROP_DOWN);
        FormData data = new FormData();
        data.left = new FormAttachment(0, left);
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.width = ARTIST_COMBO_WIDTH;
        artistCmb.setLayoutData(data);
        // set artists
        artistCmb.setItems((String[]) artists.toArray(new String[0]));
        artistCmb.setVisibleItemCount(artistCmb.getItemCount() >= 10 ? 10 : 5);
        artistCmb.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent ev) {
                prevSelArtist = artistCmb.getSelectionIndex();
            }
            public void focusLost(FocusEvent ev) {
                if (artistCmb.getSelectionIndex() > -1 &&
                    artistCmb.getSelectionIndex() != prevSelArtist) {
                    String artist = artistCmb.getItem(artistCmb.getSelectionIndex());
                    ArtistRecords artRecs = (ArtistRecords) recordMap.get(artist);
                    recordCmb.removeAll();
                    recordCmb.setItems((String[]) artRecs.titles.toArray(new String[0]));
                    recordCmb.setData(artRecs);
                    recordCmb.setVisibleItemCount(recordCmb.getItemCount() >= 10 ? 10 : 5);
                }
            }            
        });
        // TODO: add focus listener to fill in recordCmb
        
        left += artistCmb.computeSize(ARTIST_COMBO_WIDTH, SWT.DEFAULT).x + Settings.ITEM_SPACING_H;
        
        recordCmb = new Combo(this, SWT.DROP_DOWN);
        data = new FormData();
        data.left = new FormAttachment(0, left);
        data.top = new FormAttachment(0, top);
        data.height = Settings.COMBO_HEIGHT;
        data.width = RECORD_COMBO_WIDTH;
        recordCmb.setLayoutData(data);

        left = 0;
        top += Settings.COMBO_HEIGHT + Settings.ITEM_SPACING_V;
        
        // TODO: create other controls
        
        
    }

}
