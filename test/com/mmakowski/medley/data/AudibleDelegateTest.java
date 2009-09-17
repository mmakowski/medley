/*
 * Created on 16-May-2005
 */
package com.mmakowski.medley.data;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import junit.framework.Assert;

/**
 * Test AudibleDelegate
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.1 $ $Date: 2005/05/22 00:07:39 $
 */
public class AudibleDelegateTest extends DataObjectTest {

    public void testRecordAudition() throws DataSourceException, SQLException {
        // TODO: test albums
        Vector v = AlbumTest.getAlbumIds();
        int id = 0;
        for (Iterator i = v.iterator(); i.hasNext();) {
            id = ((Integer) i.next()).intValue();
            Album a = Album.load(id);
            Assert.assertNotNull(a);
        }
        // TODO: test records
        // TODO: test tracks
    }

    public void testGetAuditions() {
        // TODO: test albums
        // TODO: test records
        // TODO: test tracks
    }

    public void testGetAudibleType() {
        AudibleDelegate ad = new AudibleDelegate(Audible.ALBUM, 1);
        Assert.assertEquals(Audible.ALBUM, ad.getAudibleType());
        ad = new AudibleDelegate(Audible.RECORD, 1);
        Assert.assertEquals(Audible.RECORD, ad.getAudibleType());
        ad = new AudibleDelegate(Audible.TRACK, 1);
        Assert.assertEquals(Audible.TRACK, ad.getAudibleType());        
    }

    public void testLoadAuditions() {
        // TODO: test albums
        // TODO: test records
        // TODO: test tracks
    }

}
