---------------------------------------------------------------------------
-- $Id: medley-firebird-drop.sql,v 1.1 2004/04/09 16:29:53 maciek Exp $
-- 
-- Remove the database structure for Medley application.
-- 
-- author:  Maciek Makowski
-- version: $Revision: 1.1 $ 
-- date:    $Date: 2004/04/09 16:29:53 $
---------------------------------------------------------------------------

DROP TABLE RECORD_LOANS;
DROP TABLE ALBUM_LOANS;
DROP TABLE BORROWERS;
DROP TABLE TRACK_AUDITIONS;
DROP TABLE RECORD_AUDITIONS;
DROP TABLE ALBUM_AUDITIONS;
DROP TABLE ARTIST_RATINGS;
DROP TABLE TRACK_RATINGS;
DROP TABLE RECORD_RATINGS;
DROP TABLE ALBUM_RATINGS;
DROP TABLE RATINGS;
DROP TABLE RATING_GROUPS;
DROP TABLE ARTIST_TAGS;
DROP TABLE TRACK_TAGS;
DROP TABLE RECORD_TAGS;
DROP TABLE ALBUM_TAGS;
DROP TABLE TAG_VALUES;
DROP TABLE TAGS;
DROP TABLE TAG_GROUPS;
DROP TABLE TRACK_ARTISTS;
DROP TABLE RECORD_ARTISTS;
DROP TABLE ALBUM_ARTISTS;
DROP TABLE ARTISTS;
DROP TABLE TRACKS;
DROP TABLE RECORDS;
DROP TABLE ALBUMS;

DROP USER medley;