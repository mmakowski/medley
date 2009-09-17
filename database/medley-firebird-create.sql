---------------------------------------------------------------------------
-- $Id: medley-firebird-create.sql,v 1.1 2004/04/09 16:29:53 maciek Exp $
-- 
-- Create the database structure for Medley application.
-- 
-- author:  Maciek Makowski
-- version: $Revision: 1.1 $ 
-- date:    $Date: 2004/04/09 16:29:53 $
---------------------------------------------------------------------------

-- Create the database:
CREATE DATABASE 'medley.gdb' DEFAULT CHARACTER SET UNICODE_FSS; 

---------------------------------------------------------------------------
-- The basic entities: Albums, Records and Tracks

--
-- Albums
--
CREATE GENERATOR GEN_ALBUMS;
CREATE TABLE ALBUMS (
  albumId DECIMAL(5) NOT NULL PRIMARY KEY,
  alb_title VARCHAR(200) NOT NULL,
  alb_originalReleaseYear DECIMAL(4),
  alb_releaseYear DECIMAL(4),
  alb_label VARCHAR(80),
  alb_length TIME,
  alb_removed TIMESTAMP,
  alb_int_artistCache VARCHAR(512),			-- cache for main artists
  alb_int_artistSortString VARCHAR(512),	-- sort string for artists
  alb_comments VARCHAR(10000)
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUMS_INSERT FOR ALBUMS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumId IS NULL) OR (NEW.albumId <= 0)) THEN
  BEGIN
    NEW.albumId = GEN_ID(GEN_ALBUMS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumId) FROM ALBUMS INTO : maxId;
  	IF (maxId < NEW.albumId) THEN maxId = NEW.albumId;
    maxId = GEN_ID(GEN_ALBUMS, maxId - GEN_ID(GEN_ALBUMS, 0));
  END
END !!
SET TERM ; !!

--
-- Records
--
CREATE GENERATOR GEN_RECORDS;
CREATE TABLE RECORDS (
  recordId DECIMAL(6) NOT NULL PRIMARY KEY,
  rec_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  rec_title VARCHAR(200),
  rec_number DECIMAL(3),
  rec_length TIME, 
  rec_removed TIMESTAMP,
  rec_int_artistCache VARCHAR(512),			-- cache for main artists
  rec_int_artistSortString VARCHAR(512),	-- sort string for artists
  rec_comments VARCHAR(10000)
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORDS_INSERT FOR RECORDS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordId IS NULL) OR (NEW.recordId <= 0)) THEN
  BEGIN
    NEW.recordId = GEN_ID(GEN_RECORDS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordId) FROM RECORDS INTO : maxId;
  	IF (maxId < NEW.recordId) THEN maxId = NEW.recordId;
    maxId = GEN_ID(GEN_RECORDS, maxId - GEN_ID(GEN_RECORDS, 0));
  END
END !!
SET TERM ; !!

--
-- Tracks
--
CREATE GENERATOR GEN_TRACKS;
CREATE TABLE TRACKS (
  trackId DECIMAL(7) NOT NULL PRIMARY KEY,
  trk_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  trk_title VARCHAR(200),
  trk_number DECIMAL(3),
  trk_length TIME, 
  trk_int_artistCache VARCHAR(512),			-- cache for main artists
  trk_int_artistSortString VARCHAR(512),	-- sort string for artists
  trk_comments VARCHAR(10000)
);
SET TERM !! ;
CREATE TRIGGER TRG_TRACKS_INSERT FOR TRACKS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.trackId IS NULL) OR (NEW.trackId <= 0)) THEN
  BEGIN
    NEW.trackId = GEN_ID(GEN_TRACKS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(trackId) FROM TRACKS INTO : maxId;
  	IF (maxId < NEW.trackId) THEN maxId = NEW.trackId;
    maxId = GEN_ID(GEN_TRACKS, maxId - GEN_ID(GEN_TRACKS, 0));
  END
END !!
SET TERM ; !!


---------------------------------------------------------------------------
-- The Artists and their links with Albums, Records and 
-- Tracks

--
-- Artists
--
CREATE GENERATOR GEN_ARTISTS;
CREATE TABLE ARTISTS (
  artistId DECIMAL(5) NOT NULL PRIMARY KEY,
  art_name VARCHAR(80) NOT NULL,
  art_sortName VARCHAR(80) NOT NULL,
  art_type CHAR(20) NOT NULL, -- individual or ensemble
  art_comments VARCHAR(10000),
  UNIQUE (art_name),
  UNIQUE (art_sortName)
);
SET TERM !! ;
CREATE TRIGGER TRG_ARTISTS_INSERT FOR ARTISTS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.artistId IS NULL) OR (NEW.artistId <= 0)) THEN
  BEGIN
    NEW.artistId = GEN_ID(GEN_ARTISTS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(artistId) FROM ARTISTS INTO : maxId;
  	IF (maxId < NEW.artistId) THEN maxId = NEW.artistId;
    maxId = GEN_ID(GEN_ARTISTS, maxId - GEN_ID(GEN_ARTISTS, 0));
  END
END !!
SET TERM ; !!

--
-- Album Artists
--
CREATE GENERATOR GEN_ALBUM_ARTISTS;
CREATE TABLE ALBUM_ARTISTS (
  albumArtistId DECIMAL(7) NOT NULL PRIMARY KEY,
  lar_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  lar_artist DECIMAL(5) NOT NULL REFERENCES ARTISTS(artistId) ON DELETE CASCADE,
  lar_role VARCHAR(60) NOT NULL,
  lar_main DECIMAL(1) NOT NULL,
  UNIQUE (lar_album, lar_artist, lar_role)
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUM_ARTISTS_INSERT FOR ALBUM_ARTISTS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumArtistId IS NULL) OR (NEW.albumArtistId <= 0)) THEN
  BEGIN
    NEW.albumArtistId = GEN_ID(GEN_ALBUM_ARTISTS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumArtistId) FROM ALBUM_ARTISTS INTO : maxId;
  	IF (maxId < NEW.albumArtistId) THEN maxId = NEW.albumArtistId;
    maxId = GEN_ID(GEN_ALBUM_ARTISTS, maxId - GEN_ID(GEN_ALBUM_ARTISTS, 0));
  END
END !!
SET TERM ; !!

--
-- Record Artists
--
CREATE GENERATOR GEN_RECORD_ARTISTS;
CREATE TABLE RECORD_ARTISTS (
  recordArtistId DECIMAL(8) NOT NULL PRIMARY KEY,
  rar_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  rar_artist DECIMAL(5) NOT NULL REFERENCES ARTISTS(artistId) ON DELETE CASCADE,
  rar_role VARCHAR(60) NOT NULL,
  rar_main DECIMAL(1) NOT NULL,
  UNIQUE (rar_record, rar_artist, rar_role)
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORD_ARTISTS_INSERT FOR RECORD_ARTISTS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordArtistId IS NULL) OR (NEW.recordArtistId <= 0)) THEN
  BEGIN
    NEW.recordArtistId = GEN_ID(GEN_RECORD_ARTISTS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordArtistId) FROM RECORD_ARTISTS INTO : maxId;
  	IF (maxId < NEW.recordArtistId) THEN maxId = NEW.recordArtistId;
    maxId = GEN_ID(GEN_RECORD_ARTISTS, maxId - GEN_ID(GEN_RECORD_ARTISTS, 0));
  END
END !!
SET TERM ; !!

--
-- Track Artists
--
CREATE GENERATOR GEN_TRACK_ARTISTS;
CREATE TABLE TRACK_ARTISTS (
  trackArtistId DECIMAL(9) NOT NULL PRIMARY KEY,
  tar_track DECIMAL(7) NOT NULL REFERENCES TRACKS(trackId) ON DELETE CASCADE,
  tar_artist DECIMAL(5) NOT NULL REFERENCES ARTISTS(artistId) ON DELETE CASCADE,
  tar_role VARCHAR(60) NOT NULL,
  tar_main DECIMAL(1) NOT NULL,
  UNIQUE (tar_track, tar_artist, tar_role)
);
SET TERM !! ;
CREATE TRIGGER TRG_TRACK_ARTISTS_INSERT FOR TRACK_ARTISTS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.trackArtistId IS NULL) OR (NEW.trackArtistId <= 0)) THEN
  BEGIN
    NEW.trackArtistId = GEN_ID(GEN_TRACK_ARTISTS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(trackArtistId) FROM TRACK_ARTISTS INTO : maxId;
  	IF (maxId < NEW.trackArtistId) THEN maxId = NEW.trackArtistId;
    maxId = GEN_ID(GEN_TRACK_ARTISTS, maxId - GEN_ID(GEN_TRACK_ARTISTS, 0));
  END
END !!
SET TERM ; !!


---------------------------------------------------------------------------
-- The Tags, Tag Groups, Tag Values  and their links with Albums, Records, 
-- Tracks and Artists

--
-- Tag Groups (dictionary)
--
CREATE GENERATOR GEN_TAG_GROUPS;
CREATE TABLE TAG_GROUPS (
  tagGroupId DECIMAL(5) NOT NULL PRIMARY KEY,
  tgr_name VARCHAR(60) NOT NULL,
  tgr_parent DECIMAL(5) REFERENCES TAG_GROUPS(tagGroupId), 
  UNIQUE (tgr_name, tgr_parent)
);
SET TERM !! ;
CREATE TRIGGER TRG_TAG_GROUPS_INSERT FOR TAG_GROUPS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.tagGroupId IS NULL) OR (NEW.tagGroupId <= 0)) THEN
  BEGIN
    NEW.tagGroupId = GEN_ID(GEN_TAG_GROUPS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(tagGroupId) FROM TAG_GROUPS INTO : maxId;
  	IF (maxId < NEW.tagGroupId) THEN maxId = NEW.tagGroupId;
    maxId = GEN_ID(GEN_TAG_GROUPS, maxId - GEN_ID(GEN_TAG_GROUPS, 0));
  END
END !!
SET TERM ; !!

--
-- Tags
--
CREATE GENERATOR GEN_TAGS;
CREATE TABLE TAGS (
  tagId DECIMAL(6) NOT NULL PRIMARY KEY,
  tag_name VARCHAR(50) NOT NULL,
  tag_appliesTo CHAR(12) NOT NULL, -- album, record, track or artist
  tag_type CHAR(8) DEFAULT 'text' NOT NULL, -- text, enum or list
  tag_tagGroup DECIMAL(5) REFERENCES TAG_GROUPS(tagGroupId),
  UNIQUE (tag_name, tag_appliesTo, tag_tagGroup)
);
SET TERM !! ;
CREATE TRIGGER TRG_TAGS_INSERT FOR TAGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.tagId IS NULL) OR (NEW.tagId <= 0)) THEN
  BEGIN
    NEW.tagId = GEN_ID(GEN_TAGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(tagId) FROM TAGS INTO : maxId;
  	IF (maxId < NEW.tagId) THEN maxId = NEW.tagId;
    maxId = GEN_ID(GEN_TAGS, maxId - GEN_ID(GEN_TAGS, 0));
  END
END !!
SET TERM ; !!

--
-- Tag Values
--
CREATE GENERATOR GEN_TAG_VALUES;
CREATE TABLE TAG_VALUES (
  tagValueId DECIMAL(7) NOT NULL PRIMARY KEY,
  tvl_tag DECIMAL(6) NOT NULL REFERENCES TAGS(tagId) ON DELETE CASCADE,
  tvl_value VARCHAR(60) NOT NULL,
  UNIQUE (tvl_tag, tvl_value)
);
SET TERM !! ;
CREATE TRIGGER TRG_TAG_VALUES_INSERT FOR TAG_VALUES
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.tagValueId IS NULL) OR (NEW.tagValueId <= 0)) THEN
  BEGIN
    NEW.tagValueId = GEN_ID(GEN_TAG_VALUES, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(tagValueId) FROM TAG_VALUES INTO : maxId;
  	IF (maxId < NEW.tagValueId) THEN maxId = NEW.tagValueId;
    maxId = GEN_ID(GEN_TAG_VALUES, maxId - GEN_ID(GEN_TAG_VALUES, 0));
  END
END !!
SET TERM ; !!

--
-- Album Tags
--
CREATE GENERATOR GEN_ALBUM_TAGS;
CREATE TABLE ALBUM_TAGS (
  albumTagId DECIMAL(8) NOT NULL PRIMARY KEY,
  ltg_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  ltg_tag DECIMAL(6) NOT NULL REFERENCES TAGS(tagId) ON DELETE CASCADE,
  ltg_value VARCHAR(200) NOT NULL,
  UNIQUE (ltg_album, ltg_tag)
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUM_TAGS_INSERT FOR ALBUM_TAGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumTagId IS NULL) OR (NEW.albumTagId <= 0)) THEN
  BEGIN
    NEW.albumTagId = GEN_ID(GEN_ALBUM_TAGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumTagId) FROM ALBUM_TAGS INTO : maxId;
  	IF (maxId < NEW.albumTagId) THEN maxId = NEW.albumTagId;
    maxId = GEN_ID(GEN_ALBUM_TAGS, maxId - GEN_ID(GEN_ALBUM_TAGS, 0));
  END
END !!
SET TERM ; !!

--
-- Record Tags
--
CREATE GENERATOR GEN_RECORD_TAGS;
CREATE TABLE RECORD_TAGS (
  recordTagId DECIMAL(8) NOT NULL PRIMARY KEY,
  rtg_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  rtg_tag DECIMAL(6) NOT NULL REFERENCES TAGS(tagId) ON DELETE CASCADE,
  rtg_value VARCHAR(200) NOT NULL,
  UNIQUE (rtg_record, rtg_tag)
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORD_TAGS_INSERT FOR RECORD_TAGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordTagId IS NULL) OR (NEW.recordTagId <= 0)) THEN
  BEGIN
    NEW.recordTagId = GEN_ID(GEN_RECORD_TAGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordTagId) FROM RECORD_TAGS INTO : maxId;
  	IF (maxId < NEW.recordTagId) THEN maxId = NEW.recordTagId;
    maxId = GEN_ID(GEN_RECORD_TAGS, maxId - GEN_ID(GEN_RECORD_TAGS, 0));
  END
END !!
SET TERM ; !!

--
-- Track Tags
--
CREATE GENERATOR GEN_TRACK_TAGS;
CREATE TABLE TRACK_TAGS (
  trackTagId DECIMAL(9) NOT NULL PRIMARY KEY,
  ttg_track DECIMAL(7) NOT NULL REFERENCES TRACKS(trackId) ON DELETE CASCADE,
  ttg_tag DECIMAL(6) NOT NULL REFERENCES TAGS(tagId) ON DELETE CASCADE,
  ttg_value VARCHAR(200) NOT NULL,
  UNIQUE (ttg_track, ttg_tag)
);
SET TERM !! ;
CREATE TRIGGER TRG_TRACK_TAGS_INSERT FOR TRACK_TAGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.trackTagId IS NULL) OR (NEW.trackTagId <= 0)) THEN
  BEGIN
    NEW.trackTagId = GEN_ID(GEN_TRACK_TAGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(trackTagId) FROM TRACK_TAGS INTO : maxId;
  	IF (maxId < NEW.trackTagId) THEN maxId = NEW.trackTagId;
    maxId = GEN_ID(GEN_TRACK_TAGS, maxId - GEN_ID(GEN_TRACK_TAGS, 0));
  END
END !!
SET TERM ; !!

--
-- Artist Tags
--
CREATE GENERATOR GEN_ARTIST_TAGS;
CREATE TABLE ARTIST_TAGS (
  artistTagId DECIMAL(7) NOT NULL PRIMARY KEY,
  atg_artist DECIMAL(5) NOT NULL REFERENCES ARTISTS(artistId) ON DELETE CASCADE,
  atg_tag DECIMAL(6) NOT NULL REFERENCES TAGS(tagId) ON DELETE CASCADE,
  atg_value VARCHAR(200) NOT NULL,
  UNIQUE (atg_artist, atg_tag)
);
SET TERM !! ;
CREATE TRIGGER TRG_ARTIST_TAGS_INSERT FOR ARTIST_TAGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.artistTagId IS NULL) OR (NEW.artistTagId <= 0)) THEN
  BEGIN
    NEW.artistTagId = GEN_ID(GEN_ARTIST_TAGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(artistTagId) FROM ARTIST_TAGS INTO : maxId;
  	IF (maxId < NEW.artistTagId) THEN maxId = NEW.artistTagId;
    maxId = GEN_ID(GEN_ARTIST_TAGS, maxId - GEN_ID(GEN_ARTIST_TAGS, 0));
  END
END !!
SET TERM ; !!


---------------------------------------------------------------------------
-- The Ratings, their Groups and links with other entities

--
-- Rating Groups
--
CREATE GENERATOR GEN_RATING_GROUPS;
CREATE TABLE RATING_GROUPS (
  ratingGroupId DECIMAL(4) NOT NULL PRIMARY KEY,
  rgr_name VARCHAR(60) NOT NULL,
  rgr_parent DECIMAL(4) REFERENCES RATING_GROUPS(ratingGroupId),
  UNIQUE (rgr_name, rgr_parent)
);
SET TERM !! ;
CREATE TRIGGER TRG_RATING_GROUPS_INSERT FOR RATING_GROUPS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.ratingGroupId IS NULL) OR (NEW.ratingGroupId <= 0)) THEN
  BEGIN
    NEW.ratingGroupId = GEN_ID(GEN_RATING_GROUPS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(ratingGroupId) FROM RATING_GROUPS INTO : maxId;
  	IF (maxId < NEW.ratingGroupId) THEN maxId = NEW.ratingGroupId;
    maxId = GEN_ID(GEN_RATING_GROUPS, maxId - GEN_ID(GEN_RATING_GROUPS, 0));
  END
END !!
SET TERM ; !!

--
-- Ratings
--
CREATE GENERATOR GEN_RATINGS;
CREATE TABLE RATINGS (
  ratingId DECIMAL(5) NOT NULL PRIMARY KEY,
  rat_name VARCHAR(60) NOT NULL,
  rat_appliesTo CHAR(12) NOT NULL, -- album, record, track or artist
  rat_type CHAR(24) NOT NULL, -- decimal, letter, percentage or whole number
  rat_minValue DECIMAL(6) NOT NULL,
  rat_maxValue DECIMAL(6) NOT NULL,
  rat_ratingGroup DECIMAL(4) REFERENCES RATING_GROUPS(ratingGroupId),
  UNIQUE(rat_name, rat_ratingGroup)
);
SET TERM !! ;
CREATE TRIGGER TRG_RATINGS_INSERT FOR RATINGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.ratingId IS NULL) OR (NEW.ratingId <= 0)) THEN
  BEGIN
    NEW.ratingId = GEN_ID(GEN_RATINGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(ratingId) FROM RATINGS INTO : maxId;
  	IF (maxId < NEW.ratingId) THEN maxId = NEW.ratingId;
    maxId = GEN_ID(GEN_RATINGS, maxId - GEN_ID(GEN_RATINGS, 0));
  END
END !!
SET TERM ; !!

--
-- Album Ratings
--
CREATE GENERATOR GEN_ALBUM_RATINGS;
CREATE TABLE ALBUM_RATINGS (
  albumRatingId DECIMAL(8) NOT NULL PRIMARY KEY,
  lra_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  lra_rating DECIMAL(5) NOT NULL REFERENCES RATINGS(ratingId) ON DELETE CASCADE,
  lra_dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  lra_score DECIMAL(4) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUM_RATINGS_INSERT FOR ALBUM_RATINGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumRatingId IS NULL) OR (NEW.albumRatingId <= 0)) THEN
  BEGIN
    NEW.albumRatingId = GEN_ID(GEN_ALBUM_RATINGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumRatingId) FROM ALBUM_RATINGS INTO : maxId;
  	IF (maxId < NEW.albumRatingId) THEN maxId = NEW.albumRatingId;
    maxId = GEN_ID(GEN_ALBUM_RATINGS, maxId - GEN_ID(GEN_ALBUM_RATINGS, 0));
  END
END !!
SET TERM ; !!

--
-- Record Ratings
--
CREATE GENERATOR GEN_RECORD_RATINGS;
CREATE TABLE RECORD_RATINGS (
  recordRatingId DECIMAL(8) NOT NULL PRIMARY KEY,
  rra_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  rra_rating DECIMAL(5) NOT NULL REFERENCES RATINGS(ratingId) ON DELETE CASCADE,
  rra_dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  rra_score DECIMAL(4) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORD_RATINGS_INSERT FOR RECORD_RATINGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordRatingId IS NULL) OR (NEW.recordRatingId <= 0)) THEN
  BEGIN
    NEW.recordRatingId = GEN_ID(GEN_RECORD_RATINGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordRatingId) FROM RECORD_RATINGS INTO : maxId;
  	IF (maxId < NEW.recordRatingId) THEN maxId = NEW.recordRatingId;
    maxId = GEN_ID(GEN_RECORD_RATINGS, maxId - GEN_ID(GEN_RECORD_RATINGS, 0));
  END
END !!
SET TERM ; !!

--
-- Track Ratings
--
CREATE GENERATOR GEN_TRACK_RATINGS;
CREATE TABLE TRACK_RATINGS (
  trackRatingId DECIMAL(8) NOT NULL PRIMARY KEY,
  tra_track DECIMAL(7) NOT NULL REFERENCES TRACKS(trackId) ON DELETE CASCADE,
  tra_rating DECIMAL(5) NOT NULL REFERENCES RATINGS(ratingId) ON DELETE CASCADE,
  tra_dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  tra_score DECIMAL(4) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_TRACK_RATINGS_INSERT FOR TRACK_RATINGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.trackRatingId IS NULL) OR (NEW.trackRatingId <= 0)) THEN
  BEGIN
    NEW.trackRatingId = GEN_ID(GEN_TRACK_RATINGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(trackRatingId) FROM TRACK_RATINGS INTO : maxId;
  	IF (maxId < NEW.trackRatingId) THEN maxId = NEW.trackRatingId;
    maxId = GEN_ID(GEN_TRACK_RATINGS, maxId - GEN_ID(GEN_TRACK_RATINGS, 0));
  END
END !!
SET TERM ; !!

--
-- Artist Ratings
--
CREATE GENERATOR GEN_ARTIST_RATINGS;
CREATE TABLE ARTIST_RATINGS (
  artistRatingId DECIMAL(8) NOT NULL PRIMARY KEY,
  ara_artist DECIMAL(5) NOT NULL REFERENCES ARTISTS(artistId) ON DELETE CASCADE,
  ara_rating DECIMAL(5) NOT NULL REFERENCES RATINGS(ratingId) ON DELETE CASCADE,
  ara_dateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  ara_score DECIMAL(4) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_ARTIST_RATINGS_INSERT FOR ARTIST_RATINGS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.artistRatingId IS NULL) OR (NEW.artistRatingId <= 0)) THEN
  BEGIN
    NEW.artistRatingId = GEN_ID(GEN_ARTIST_RATINGS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(artistRatingId) FROM ARTIST_RATINGS INTO : maxId;
  	IF (maxId < NEW.artistRatingId) THEN maxId = NEW.artistRatingId;
    maxId = GEN_ID(GEN_ARTIST_RATINGS, maxId - GEN_ID(GEN_ARTIST_RATINGS, 0));
  END
END !!
SET TERM ; !!


---------------------------------------------------------------------------
-- The Auditions

--
-- Album Auditions
--
CREATE GENERATOR GEN_ALBUM_AUDITIONS;
CREATE TABLE ALBUM_AUDITIONS (
  albumAuditionId DECIMAL(8) NOT NULL PRIMARY KEY,
  lau_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  lau_auditionDate DATE NOT NULL,
  lau_auditionTime TIME,
  lau_recordCount DECIMAL(3) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUM_AUDITIONS_INSERT FOR ALBUM_AUDITIONS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumAuditionId IS NULL) OR (NEW.albumAuditionId <= 0)) THEN
  BEGIN
    NEW.albumAuditionId = GEN_ID(GEN_ALBUM_AUDITIONS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumAuditionId) FROM ALBUM_AUDITIONS INTO : maxId;
  	IF (maxId < NEW.albumAuditionId) THEN maxId = NEW.albumAuditionId;
    maxId = GEN_ID(GEN_ALBUM_AUDITIONS, maxId - GEN_ID(GEN_ALBUM_AUDITIONS, 0));
  END
END !!
SET TERM ; !!

--
-- Record Auditions
--
CREATE GENERATOR GEN_RECORD_AUDITIONS;
CREATE TABLE RECORD_AUDITIONS (
  recordAuditionId DECIMAL(9) NOT NULL PRIMARY KEY,
  rau_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  rau_auditionDate DATE NOT NULL,
  rau_auditionTime TIME,
  rau_trackCount DECIMAL(3) NOT NULL
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORD_AUDITIONS_INSERT FOR RECORD_AUDITIONS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordAuditionId IS NULL) OR (NEW.recordAuditionId <= 0)) THEN
  BEGIN
    NEW.recordAuditionId = GEN_ID(GEN_RECORD_AUDITIONS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordAuditionId) FROM RECORD_AUDITIONS INTO : maxId;
  	IF (maxId < NEW.recordAuditionId) THEN maxId = NEW.recordAuditionId;
    maxId = GEN_ID(GEN_RECORD_AUDITIONS, maxId - GEN_ID(GEN_RECORD_AUDITIONS, 0));
  END
END !!
SET TERM ; !!

--
-- Track Auditions
--
CREATE GENERATOR GEN_TRACK_AUDITIONS;
CREATE TABLE TRACK_AUDITIONS (
  trackAuditionId DECIMAL(10) NOT NULL PRIMARY KEY,
  tau_track DECIMAL(7) NOT NULL REFERENCES TRACKS(trackId) ON DELETE CASCADE,
  tau_auditionDate DATE NOT NULL,
  tau_auditionTime TIME
);
SET TERM !! ;
CREATE TRIGGER TRG_TRACK_AUDITIONS_INSERT FOR TRACK_AUDITIONS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.trackAuditionId IS NULL) OR (NEW.trackAuditionId <= 0)) THEN
  BEGIN
    NEW.trackAuditionId = GEN_ID(GEN_TRACK_AUDITIONS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(trackAuditionId) FROM TRACK_AUDITIONS INTO : maxId;
  	IF (maxId < NEW.trackAuditionId) THEN maxId = NEW.trackAuditionId;
    maxId = GEN_ID(GEN_TRACK_AUDITIONS, maxId - GEN_ID(GEN_TRACK_AUDITIONS, 0));
  END
END !!
SET TERM ; !!


---------------------------------------------------------------------------
-- The Borrowers and Loans

--
-- Borrowers
--
CREATE GENERATOR GEN_BORROWERS;
CREATE TABLE BORROWERS (
  borrowerId DECIMAL(5) NOT NULL PRIMARY KEY,
  bor_firstName VARCHAR(60) NOT NULL,
  bor_surname VARCHAR(60) NOT NULL,
  bor_email VARCHAR(60)
  -- fields to big for index:
  -- UNIQUE (bor_firstName, bor_surname, bor_email)
);
SET TERM !! ;
CREATE TRIGGER TRG_BORROWERS_INSERT FOR BORROWERS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.borrowerId IS NULL) OR (NEW.borrowerId <= 0)) THEN
  BEGIN
    NEW.borrowerId = GEN_ID(GEN_BORROWERS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(borrowerId) FROM BORROWERS INTO : maxId;
  	IF (maxId < NEW.borrowerId) THEN maxId = NEW.borrowerId;
    maxId = GEN_ID(GEN_BORROWERS, maxId - GEN_ID(GEN_BORROWERS, 0));
  END
END !!
SET TERM ; !!

--
-- Album Loans
--
CREATE GENERATOR GEN_ALBUM_LOANS;
CREATE TABLE ALBUM_LOANS (
  albumLoanId DECIMAL(7) NOT NULL PRIMARY KEY,
  lln_album DECIMAL(5) NOT NULL REFERENCES ALBUMS(albumId) ON DELETE CASCADE,
  lln_borrower DECIMAL(5) NOT NULL REFERENCES BORROWERS(borrowerId) ON DELETE CASCADE,
  lln_dateLent DATE NOT NULL,
  lln_dateReturned DATE
);
SET TERM !! ;
CREATE TRIGGER TRG_ALBUM_LOANS_INSERT FOR ALBUM_LOANS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.albumLoanId IS NULL) OR (NEW.albumLoanId <= 0)) THEN
  BEGIN
    NEW.albumLoanId = GEN_ID(GEN_ALBUM_LOANS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(albumLoanId) FROM ALBUM_LOANS INTO : maxId;
  	IF (maxId < NEW.albumLoanId) THEN maxId = NEW.albumLoanId;
    maxId = GEN_ID(GEN_ALBUM_LOANS, maxId - GEN_ID(GEN_ALBUM_LOANS, 0));
  END
END !!
SET TERM ; !!

--
-- Record Loans
--
CREATE GENERATOR GEN_RECORD_LOANS;
CREATE TABLE RECORD_LOANS (
  recordLoanId DECIMAL(7) NOT NULL PRIMARY KEY,
  rln_record DECIMAL(6) NOT NULL REFERENCES RECORDS(recordId) ON DELETE CASCADE,
  rln_borrower DECIMAL(5) NOT NULL REFERENCES BORROWERS(borrowerId) ON DELETE CASCADE,
  rln_dateLent DATE NOT NULL,
  rln_dateReturned DATE
);
SET TERM !! ;
CREATE TRIGGER TRG_RECORD_LOANS_INSERT FOR RECORD_LOANS
ACTIVE
BEFORE INSERT
POSITION 0
AS
  DECLARE VARIABLE maxId INTEGER;
BEGIN
  IF ((NEW.recordLoanId IS NULL) OR (NEW.recordLoanId <= 0)) THEN
  BEGIN
    NEW.recordLoanId = GEN_ID(GEN_RECORD_LOANS, 1);
  END
  ELSE
  BEGIN
  	SELECT MAX(recordLoanId) FROM RECORD_LOANS INTO : maxId;
  	IF (maxId < NEW.recordLoanId) THEN maxId = NEW.recordLoanId;
    maxId = GEN_ID(GEN_RECORD_LOANS, maxId - GEN_ID(GEN_RECORD_LOANS, 0));
  END
END !!
SET TERM ; !!
