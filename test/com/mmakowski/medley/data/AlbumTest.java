/*
 * Created on 28-Jan-2005
 */
package com.mmakowski.medley.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Vector;

import com.mmakowski.medley.TestSettings;
import com.mmakowski.medley.resources.Errors;

import junit.framework.Assert;

/**
 * 
 * 
 * @author Maciek Makowski
 * @version $Revision: 1.5 $ $Date: 2005/05/22 00:07:40 $
 */
public class AlbumTest extends DataObjectTest {

	boolean flag;
	
	/*
	 * @see DataObjectTest#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see DataObjectTest#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	static Vector getAlbumIds() throws DataSourceException, SQLException {
		Vector v = new Vector();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		PreparedStatement pstmt = conn.prepareStatement("SELECT albumId FROM ALBUMS ORDER BY albumId");
		ResultSet res = pstmt.executeQuery();
		while (res.next()) {
			v.add(new Integer(res.getInt("albumId")));
		}
		fb.disconnect(conn);
		Assert.assertTrue(!v.isEmpty());
		return v;
	}
	
	public void testGetLabels() throws DataSourceException, SQLException {
		Vector v = Album.getLabels();
		Assert.assertNotNull(v);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(DISTINCT alb_label) AS cnt FROM ALBUMS");
		ResultSet res = pstmt.executeQuery();
		Assert.assertTrue(res.next());
		int cnt = res.getInt("cnt");
		res.close();
		pstmt.close();
		fb.disconnect(conn);
		Assert.assertEquals(v.size(), cnt);
	}

	public void testGetAllAlbums() throws DataSourceException, SQLException {
		Vector v = Album.getAllAlbums();
		Assert.assertNotNull(v);
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(albumId) AS cnt FROM ALBUMS");
		ResultSet res = pstmt.executeQuery();
		Assert.assertTrue(res.next());
		int cnt = res.getInt("cnt");
		res.close();
		pstmt.close();
		fb.disconnect(conn);
		Assert.assertEquals(v.size(), cnt);
	}
	
	public void testDisposeAll() throws DataSourceException {
		Vector v = Album.getAllAlbums();
		FileDataSource fb = (FileDataSource) DataSource.get();
		Assert.assertEquals(fb.getDataObjects().size(), v.size());
		Album.disposeAll(v);
		Assert.assertTrue(fb.getDataObjects().isEmpty());
	}

	public void testLoad() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		int id = 0;
		for (Iterator i = v.iterator(); i.hasNext();) {
			id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Assert.assertNotNull(a);
		}
		id++;
		flag = false;
		try {
			Album a = Album.load(id);
		} catch (DataSourceException ex) {
			if (ex.getErrorCode() == Errors.NO_DATA_FOR_ID) {
				flag = true;
			} else {
				throw ex;
			}
		}
		Assert.assertTrue(flag);
	}

	public void testDispose() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Assert.assertTrue(fb.getDataObjects().contains(a));
			a.dispose();
			Assert.assertFalse(fb.getDataObjects().contains(a));
		}
	}

	public void testCreate() throws DataSourceException {
		Album a = Album.create();
		Assert.assertNotNull(a);
		Assert.assertTrue(DataSource.get().isModified());
	}

	/*
	 * Class under test for void delete(int)
	 */
	public void testDeleteint() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album.delete(id);
			Assert.assertTrue(DataSource.get().isModified());
			flag = false;
			try {
				Album a = Album.load(id);
			} catch (DataSourceException ex) {
				if (ex.getErrorCode() == Errors.NO_DATA_FOR_ID) {
					flag = true;
				} else {
					throw ex;
				}
			}
			Assert.assertTrue(flag);
		}
	}

	/*
	 * Class under test for void delete()
	 */
	public void testDelete() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			a.delete();
			Assert.assertTrue(DataSource.get().isModified());
			a.dispose();
			flag = false;
			try {
				a = Album.load(id);
			} catch (DataSourceException ex) {
				if (ex.getErrorCode() == Errors.NO_DATA_FOR_ID) {
					flag = true;
				} else {
					throw ex;
				}
			}
			Assert.assertTrue(flag);
		}
	}

	public void testGetType() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Assert.assertEquals(a.getType(), MusicalItem.ALBUM);
			a.dispose();
		}
	}

	public void testGetId() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Assert.assertEquals(a.getId(), id);
			a.dispose();
		}
	}

	public void testGetTitle() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_title FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			Assert.assertEquals(a.getTitle(), res.getString("alb_title"));
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetTitle() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			String title = TestSettings.TEST_STRING.substring(0, 100);
			a.setTitle(title);
			Assert.assertTrue(DataSource.get().isModified());
			Assert.assertEquals(a.getTitle(), title);
			a.dispose();
			a = Album.load(id);
			Assert.assertEquals(a.getTitle(), title);
			a.dispose();
		}
	}

	public void testGetOriginalReleaseYear() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_originalReleaseYear FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			Assert.assertEquals(a.getOriginalReleaseYear(), res.getInt("alb_originalReleaseYear"));
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetOriginalReleaseYear() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			int year = 1941;
			a.setOriginalReleaseYear(year);
			Assert.assertTrue(DataSource.get().isModified());
			Assert.assertEquals(a.getOriginalReleaseYear(), year);
			a.dispose();
			a = Album.load(id);
			Assert.assertEquals(a.getOriginalReleaseYear(), year);
			a.dispose();
		}
	}

	public void testGetReleaseYear() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_releaseYear FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			Assert.assertEquals(a.getReleaseYear(), res.getInt("alb_releaseYear"));
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetReleaseYear() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			int year = 1941;
			a.setReleaseYear(year);
			Assert.assertTrue(DataSource.get().isModified());
			Assert.assertEquals(a.getReleaseYear(), year);
			a.dispose();
			a = Album.load(id);
			Assert.assertEquals(a.getReleaseYear(), year);
			a.dispose();
		}
	}

	public void testGetLabel() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_label FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			String lbl = res.getString("alb_label");
			Assert.assertEquals(a.getLabel(), lbl == null ? "" : lbl);
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetLabel() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			String label = TestSettings.TEST_STRING.substring(0, 40);
			a.setLabel(label);
			Assert.assertTrue(DataSource.get().isModified());
			Assert.assertEquals(a.getLabel(), label);
			a.dispose();
			a = Album.load(id);
			Assert.assertEquals(a.getLabel(), label);
			a.dispose();
		}
	}

	public void testGetLength() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_length FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			Assert.assertEquals(a.getLength(), res.getTime("alb_length"));
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetLength() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			GregorianCalendar gc = new GregorianCalendar();
			gc.set(Calendar.HOUR, 15);
			gc.set(Calendar.MINUTE, 31);
			gc.set(Calendar.SECOND, 13);
			a.setLength(gc.getTime());
			Assert.assertTrue(DataSource.get().isModified());
			GregorianCalendar agc = new GregorianCalendar();
			agc.setTime(a.getLength());
			Assert.assertEquals(agc.get(Calendar.HOUR), gc.get(Calendar.HOUR));
			Assert.assertEquals(agc.get(Calendar.MINUTE), gc.get(Calendar.MINUTE));
			Assert.assertEquals(agc.get(Calendar.SECOND), gc.get(Calendar.SECOND));
			a.dispose();
			a = Album.load(id);
			agc = new GregorianCalendar();
			agc.setTime(a.getLength());
			Assert.assertEquals(agc.get(Calendar.HOUR), gc.get(Calendar.HOUR));
			Assert.assertEquals(agc.get(Calendar.MINUTE), gc.get(Calendar.MINUTE));
			Assert.assertEquals(agc.get(Calendar.SECOND), gc.get(Calendar.SECOND));
			a.dispose();
		}
	}

	public void testGetRemoved() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_removed FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			Assert.assertEquals(a.getRemoved(), res.getString("alb_removed"));
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testGetComments() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_comments FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			String cmnt = res.getString("alb_comments");
			Assert.assertEquals(a.getComments(), cmnt == null ? "" : cmnt);
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testSetComments() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			String cmnts = TestSettings.TEST_STRING_MULTILINE;
			a.setComments(cmnts);
			Assert.assertTrue(DataSource.get().isModified());
			Assert.assertEquals(a.getComments(), cmnts);
			a.dispose();
			a = Album.load(id);
			Assert.assertEquals(a.getComments(), cmnts);
			a.dispose();
		}
	}

	public void testGetRecords() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Vector recs = a.getRecords();
			a.dispose();
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT recordId FROM RECORDS WHERE rec_album = ? ORDER BY rec_number");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			for (Iterator j = recs.iterator(); j.hasNext();) {
				Assert.assertTrue(res.next());
				Record r = (Record) j.next();
				Assert.assertEquals(r.getId(), res.getInt("recordId"));
			}
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			Record.disposeAll(recs);
		}
	}

	public void testGetArtists() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Vector arts = a.getArtists();
			a.dispose();
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT albumArtistId " +
					  "FROM ALBUM_ARTISTS " +
					  "WHERE lar_album = ? " +
 					  "ORDER BY lar_main DESC, lar_role");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			for (Iterator j = arts.iterator(); j.hasNext();) {
				Assert.assertTrue(res.next());
				ArtistRole ar = (ArtistRole) j.next();
				Assert.assertEquals(ar.getId(), res.getInt("albumArtistId"));
			}
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			ArtistRole.disposeAll(arts);
		}
	}

	public void testGetMainArtistString() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_int_artistCache FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			String art = res.getString("alb_int_artistCache");
			Assert.assertEquals(a.getMainArtistString(), art == null ? "" : art);
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testGetArtistSortString() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			FileDataSource fb = (FileDataSource) DataSource.get();
			Connection conn = fb.connect();
			PreparedStatement pstmt = conn.prepareStatement("SELECT alb_int_artistSortString FROM ALBUMS WHERE albumId = ?");
			pstmt.setInt(1, id);
			ResultSet res = pstmt.executeQuery();
			Assert.assertTrue(res.next());
			String art = res.getString("alb_int_artistSortString");
			Assert.assertEquals(a.getArtistSortString(), art == null ? "" : art);
			res.close();
			pstmt.close();
			fb.disconnect(conn);
			a.dispose();
		}
	}

	public void testRefreshMainArtistCache() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			ArtistRole ar = ArtistRole.create(MusicalItem.ALBUM, id);
			ar.setMain(true);
			ar.dispose();
			String ma = a.getMainArtistString();
			String as = a.getArtistSortString();
			a.refreshMainArtistCache();
			Assert.assertFalse(ma.equals(a.getMainArtistString()));
			Assert.assertFalse(as.equals(a.getArtistSortString()));
			a.dispose();
		}
	}

	public void testGetTaggableType() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Assert.assertEquals(a.getTaggableType(), Tag.ALBUM);
			a.dispose();
		}
	}

	public void testSetTagValue() throws DataSourceException, SQLException {
		FileDataSource fb = (FileDataSource) DataSource.get();
		Connection conn = fb.connect();
		// TODO: test other tag types as well
		PreparedStatement pstmt = conn.prepareStatement("SELECT MIN(tagId) as m " +
				  "FROM TAGS " +
				  "WHERE tag_appliesTo = 'album' AND tag_type = 'text'");
		ResultSet res = pstmt.executeQuery();
		Assert.assertTrue(res.next());
		int tagId = res.getInt("m");
		res.close();
		pstmt.close();
		fb.disconnect(conn);
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			String val = TestSettings.TEST_STRING.substring(0, 100);
			a.setTagValue(tagId, val);
			Assert.assertTrue(fb.isModified());
			Assert.assertEquals(a.getTagValue(tagId), val);
			a.dispose();
		}
		
	}

	public void testGetTagValue() {
		//TODO Implement getTagValue().
	}

	public void testGetRatableType() throws DataSourceException, SQLException {
		Vector v = getAlbumIds();
		for (Iterator i = v.iterator(); i.hasNext();) {
			int id = ((Integer) i.next()).intValue();
			Album a = Album.load(id);
			Assert.assertEquals(a.getRatableType(), Rating.ALBUM);
			a.dispose();
		}
	}

	public void testSetRatingScore() {
		//TODO Implement setRatingScore().
	}

	public void testGetRatingScore() {
		//TODO Implement getRatingScore().
	}

	public void testGetRatingScoreString() {
		//TODO Implement getRatingScoreString().
	}
    
    public void testDeleteLatestRatingScore() {
        //TODO: Implement deleteLatestRatingScore()
    }

	public void testGetImages() {
		//TODO Implement getImages().
	}

	/*
	 * Class under test for ImageData addImage(ImageData)
	 */
	public void testAddImageImageData() {
		//TODO Implement addImage().
	}

	public void testRemoveImage() {
		//TODO Implement removeImage().
	}

	/*
	 * Class under test for ImageData addImage(String)
	 */
	public void testAddImageString() {
		//TODO Implement addImage().
	}

	public void testImportArtistsUp() {
		//TODO Implement importArtistsUp().
	}

	public void testImportArtistsDown() {
		//TODO Implement importArtistsDown().
	}


}
