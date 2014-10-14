import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.ID3v11Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v22Tag;
import org.jaudiotagger.tag.id3.ID3v23Frame;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.id3.ID3v24Frame;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

public class AudioWorker {

    private static final String VA = "VA";

    private LogOutput logOutput;

    private HashMap<File, Integer> audioList;
    private File initFolder;
    private File parentFolder;
    private File currentFolder;
    private boolean va;
    private boolean hasMultiCD;
    private boolean issueChoosed = false;
    private Issue selectedIssue;
    private IssuesChooser issuesChooser;

    private String country;
    private String artistGenres;
    private String recordGenres;
    private String artist;
    private String albumYear;
    private String albumType;

    private RYMParser parser;

    private File[] folderContent;

    public AudioWorker(HashMap<File, Integer> audioList) throws IOException,
            TagException, ReadOnlyFileException, InvalidAudioFrameException,
            CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        country = "";
        artistGenres = "";
        recordGenres = "";
        artist = "";
        albumYear = "";
        albumType = "";
        this.audioList = audioList;
    }

    public void process() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        va = isVA();

        parser = new RYMParser();
        parser.setLogOutput(logOutput);

        if (va) {
            parser.setAudioAlbumName(getAlbumName());
            if (parser.parseVAInfo()) {
                recordGenres = parser.getRecord().firstThreeGenresToString();
                editTracks();
            }

        } else {
            parser.setAudioArtistName(getArtistName());
            parser.setAudioAlbumName(getAlbumName());
            parser.initUrls();

            if (parser.parseAlbumInfo()) {
                parser.parseArtistInfo(parser.getRYMArtistName());

            } else {
                if (parser.parseArtistInfo(parser.getAudioArtistName())) {
                    if (parser.getSingleRecords() != null) {
                        checkDiscography(parser);
                    }
                }
            }

            if (parser.getArtist() != null) {
                if (!parser.getArtist().getName().isEmpty()) {
                    artist = parser.getArtist().getName();
                }

                if (!parser.getArtist().getGenres().isEmpty()) {
                    artistGenres = parser.getArtist().firstThreeGenresToString();
                } else if (!parser.getRecord().getGenres().isEmpty()) {
                    artistGenres = parser.getRecord().firstThreeGenresToString();
                }

                if (!parser.getArtist().getCountry().isEmpty()) {
                    country = parser.getArtist().getCountry();
                }
            } else {
                System.out.println("Исполнитель не найден");
            }

            if (parser.getRecord() != null) {
                if (!parser.getRecord().getGenres().isEmpty()) {
                    recordGenres = parser.getRecord().allGenresToString();
                } else if (!parser.getArtist().getGenres().isEmpty()) {
                    recordGenres = parser.getArtist().allGenresToString();
                }

                if (!parser.getRecord().getYearRecorded().isEmpty()) {
                    albumYear = parser.getRecord().getYearRecorded();
                } else if (!parser.getRecord().getYearReleased().isEmpty()) {
                    albumYear = parser.getRecord().getYearReleased();
                }

                if (!parser.getRecord().getType().isEmpty()) {
                    albumType = parser.getRecord().getType();
                }

                if (!parser.getRecord().getIssues().isEmpty()) {
                    issuesChooser = new IssuesChooser();
                    issuesChooser.setIssues(parser.getRecord().getIssues());
                    issuesChooser.init();

                    issuesChooser.getOkButton().setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            try {
                                issueChoosed = true;
                                selectedIssue = issuesChooser.getSelectedIssue();
                                editTracks();
                                issuesChooser.close();
                            } catch (KeyNotFoundException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
                                Logger.getLogger(AudioWorker.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });

                    issuesChooser.getCancelButton().setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent t) {
                            try {
                                issueChoosed = false;
                                editTracks();
                                issuesChooser.close();
                            } catch (KeyNotFoundException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
                                Logger.getLogger(AudioWorker.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                }

            } else {
                System.out.println("Запись не найдена");
            }
        }

    }

    private void editTracks() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {

        normalizeTracklist();

        if (hasMultiCD) {
            Iterator it = sortByValue(audioList).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MP3File mp3 = new MP3File((File) pairs.getKey());

                if (!mp3.getTag().getFirst(FieldKey.DISC_NO).isEmpty()) {
                    mp3.getTag().setField(FieldKey.DISC_NO, (String) pairs.getValue());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        } else {
            Iterator it = sortByValue(audioList).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MP3File mp3 = new MP3File((File) pairs.getKey());
                if (!recordGenres.isEmpty()) {
                    mp3.getTag().setField(FieldKey.GENRE, recordGenres);
                }

                if (!artist.isEmpty() && !mp3.getTag().getFirst(FieldKey.ARTIST).equals(artist)) {
                    mp3.getTag().setField(FieldKey.ARTIST, artist);
                }

                if (artist.startsWith("The ")) {
                    mp3.getTag().setField(FieldKey.ARTIST_SORT, artist.replaceFirst("The ", "") + ", The");
                } else {
                    mp3.getTag().setField(FieldKey.ARTIST_SORT, artist);
                }

                if (!albumYear.isEmpty() && !mp3.getTag().getFirst(FieldKey.YEAR).equals(albumYear)) {
                    if (!mp3.getTag().getFirst(FieldKey.YEAR).isEmpty()) {
                        if (Integer.valueOf(albumYear) < Integer.valueOf(mp3.getTag().getFirst(FieldKey.YEAR))) {
                            mp3.getTag().setField(FieldKey.YEAR, albumYear);
                        }
                    } else {
                        mp3.getTag().setField(FieldKey.YEAR, albumYear);
                    }
                }

                if (!country.isEmpty()) {
                    mp3.getTag().setField(FieldKey.COUNTRY, country);
                }

                if (folderArtworkExists() && !mp3ArtworkExists(mp3)) {
                    mp3.getTag().setField(getArtworkFromFolder());
                }

                mp3.getTag().setField(FieldKey.COMMENT, "(c) Scwot");

                if (issueChoosed) {
                    addIssueTags(mp3);
                }

                try {
                    mp3.commit();
                } catch (CannotWriteException ex) {
                    System.out.println("mp3 tag commit failed");
                }

                it.remove(); // avoids a ConcurrentModificationException
            }

            renameAudio();
            move();
        }
    }

    private void addIssueTags(MP3File file) throws KeyNotFoundException, FieldDataInvalidException {
        if (issueChoosed) {
            if (selectedIssue.getIssueName() != null) {
                file.getTag().setField(FieldKey.ALBUM, selectedIssue.getIssueName());
            }

            if (selectedIssue.getIssueLabel() != null) {
                file.getTag().setField(FieldKey.RECORD_LABEL, selectedIssue.getIssueLabel());
            }

            if (selectedIssue.getCatNumber() != null) {
                file.getTag().setField(FieldKey.CATALOG_NO, selectedIssue.getCatNumber());
            }

            /*if (selectedIssue.getIssueYear() != null) {
             file.getTag().setField(FieldKey.YEAR, selectedIssue.getIssueYear());
             }
            
             if (albumYear != null) {
             file.getTag().setField(FieldKey.ORIGINAL_YEAR, albumYear);
             }*/
        }
    }

    private void checkDiscography(RYMParser parser) throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        int recordQuantityMatches = 0;

        // Проверяем альбомы
        for (Record record : parser.getAlbumRecords()) {
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

        // Проверяем live albums
        for (Record record : parser.getLiveRecords()) {
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

        // Проверяем compilations
        for (Record record : parser.getCompRecords()) {
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

        // Проверяем va
        for (Record record : parser.getVaRecords()) {
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

        //Проверяем ep
        for (Record record : parser.getEpRecords()) {

            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(parser.getAudioAlbumName())) {
                        recordQuantityMatches++;
                        System.out.println(singlePart);
                    }
                }
            }

            System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

        //Проверяем синглы
        for (Record record : parser.getSingleRecords()) {

            // Проверяем, есть ли в имени записи на RYM разделитель " / "
            // если есть, разделяем строку и сравниваем ее с именем альбома
            // скорее всего перед нами сингл или EP
            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(parser.getAudioAlbumName())) {
                        recordQuantityMatches++;
                        System.out.println(singlePart);
                    }
                }
            }

            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()));
                parser.setCurrentAlbumUrl(record.getLink());
                parser.parseAlbumInfo();
            }
        }

    }

    private void normalizeTracklist() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        if (hasMultiCD) {
            String currCD = "0";
            int count = 0;
            Iterator it = sortByValue(audioList).entrySet().iterator();
            String track = "";
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MP3File mp3 = new MP3File((File) pairs.getKey());
                if (!mp3.getTag().getFirst(FieldKey.DISC_NO).isEmpty()) {
                    if (!currCD.equals(mp3.getTag().getFirst(FieldKey.DISC_NO))) {
                        count = 0;
                        currCD = mp3.getTag().getFirst(FieldKey.DISC_NO);
                    }
                    count++;
                    if (count < 10) {
                        track = "0" + Integer.toString(count);
                    } else {
                        track = Integer.toString(count);
                    }

                    mp3.getTag().setField(FieldKey.DISC_NO, (String) pairs.getValue());
                    mp3.getTag().setField(FieldKey.TRACK, track);
                    try {
                        mp3.commit();
                    } catch (CannotWriteException ex) {
                        System.out.println("mp3 tag commit failed");
                    }
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        } else {
            int count = 0;
            String track = "";
            Iterator it = sortByValue(audioList).entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                MP3File mp3 = new MP3File((File) pairs.getKey());

                count++;
                if (count < 10) {
                    track = "0" + Integer.toString(count);
                } else {
                    track = Integer.toString(count);
                }
                mp3.getTag().setField(FieldKey.TRACK, track);
                try {
                    mp3.commit();
                } catch (CannotWriteException ex) {
                    System.out.println("mp3 tag commit failed");
                }
            }
        }
    }

    private boolean folderArtworkExists() {
        return new File(folderContent[0].getParentFile().getPath() + "\\folder.jpg").exists();
    }

    private boolean mp3ArtworkExists(MP3File file) {
        return !file.getTag().getArtworkList().isEmpty();
    }

    private Artwork getArtworkFromFolder() {
        File fileArtwork = new File(folderContent[0].getParentFile().getPath() + "\\folder.jpg");
        Artwork artwork = new StandardArtwork();
        try {
            artwork.setFromFile(fileArtwork);
        } catch (IOException ex) {
            System.out.println("can't create artwork from file");
        }
        return artwork;
    }

    private void renameAudio() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {

        int count = 0;
        String track = "";
        File renamedFile;

        Iterator it = sortByValue(audioList).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            MP3File mp3 = new MP3File((File) pairs.getKey());

            if (!StringUtils.isEmpty(mp3.getTag().getFirst(FieldKey.TITLE))
                    && !StringUtils.isEmpty(mp3.getTag().getFirst(
                                    FieldKey.TRACK))) {
                count++;
                /*if (count < 10) {
                 track = "0" + mp3.getTag().getFirst(FieldKey.TRACK);
                 } else {
                 track = mp3.getTag().getFirst(FieldKey.TRACK);
                 }*/

                track = mp3.getTag().getFirst(FieldKey.TRACK);

                if (va) {
                    renamedFile = new File(mp3.getFile().getParentFile()
                            .getAbsolutePath()
                            + "\\"
                            + track
                            + " - "
                            + validateName(mp3.getTag().getFirst(FieldKey.ARTIST))
                            + " - "
                            + validateName(mp3.getTag().getFirst(FieldKey.TITLE))
                            + FilenameUtils.EXTENSION_SEPARATOR
                            + FilenameUtils.getExtension(mp3.getFile().getName()));
                } else {
                    renamedFile = new File(mp3.getFile().getParentFile()
                            .getAbsolutePath()
                            + "\\"
                            + track
                            + " - "
                            + validateName(mp3.getTag().getFirst(FieldKey.TITLE))
                            + FilenameUtils.EXTENSION_SEPARATOR
                            + FilenameUtils.getExtension(mp3.getFile().getName()));
                }

                if (!mp3.getFile().getName().equals(renamedFile.getName())) {
                    System.out.println(mp3.getFile().getName());
                    System.out.println(renamedFile.getName());

                    try {
                        mp3.getFile().renameTo(renamedFile);
                    } catch (Exception e) {
                        System.out.println("rename() exception occures");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean isVA() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        boolean va = false;
        MP3File previous = null;
        int count = 0;
        for (Iterator<File> i = audioList.keySet().iterator(); i.hasNext();) {
            count++;
            MP3File current = new MP3File((File) i.next());
            if (count > 1) {
                if (!previous.getTag().getFirst(FieldKey.ARTIST)
                        .equals(current.getTag().getFirst(FieldKey.ARTIST))) {
                    va = true;
                    break;
                }
            }
            previous = current;
        }
        return va;
    }

    private String makeArtistString() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        if (va) {
            return "VA";
        } else {
            String artist = validateName(getArtistName());
            if (artist.startsWith("The ")) {
                artist = artist.replaceFirst("The ", "") + ", The";
            }

            if (!country.isEmpty() || !artistGenres.isEmpty()) {
                artist += " [";
            }

            if (!country.isEmpty()) {
                artist += country + " ";
            }

            if (!artistGenres.isEmpty()) {
                artist += artistGenres;
            }

            if (!country.isEmpty() || !artistGenres.isEmpty()) {
                artist += "]";
            }

            return artist;
        }
    }

    private String makeYearAlbumString() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        String albumString = "";

        HashMap<String, String> yearAlbum = new HashMap<>();
        for (Iterator<File> i = audioList.keySet().iterator(); i.hasNext();) {
            MP3File file = new MP3File((File) i.next());
            yearAlbum.put(file.getTag().getFirst(FieldKey.YEAR),
                    validateName(file.getTag().getFirst(FieldKey.ALBUM)));
        }

        Set<Map.Entry<String, String>> set = yearAlbum.entrySet();

        if (!va) {
            if (yearAlbum.size() > 1) {
                int count = 0;
                for (Map.Entry<String, String> me : set) {
                    count++;
                    albumString += me.getValue() + " (" + me.getKey() + ")";
                    if (count != set.size()) {
                        albumString += " + ";
                    }
                }
            } else {
                for (Map.Entry<String, String> me : set) {
                    albumString = me.getKey() + " - " + me.getValue();
                }
            }

            if (!albumType.isEmpty()
                    && (!albumType.equals("Album")
                    && !albumType.equals("Album, Collaboration"))) {
                albumString += " [" + albumType + "]";
            }

            if (issueChoosed) {
                albumString += " [" + selectedIssue.getIssueYear()
                        + ", " + validateName(selectedIssue.getIssueLabel())
                        + ", " + validateName(selectedIssue.getCatNumber()) + "]";
            }
        } else {
            for (Map.Entry<String, String> me : set) {
                if (me.getKey().isEmpty()) {
                    System.out.println("empty");
                }

                albumString = makeArtistString() + " - " + me.getValue() + " (" + me.getKey() + ")";
            }
        }

        //System.out.println(albumString);
        return albumString;
    }

    private void move() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        File artistFolder;
        File albumFolder;

        boolean moved = false;
        boolean artistFolderCreated = false;
        boolean albumFolderCreated = false;
        removeJunkFiles(folderContent[0].getParentFile());

        if (va) {
            albumFolder = new File(initFolder + "\\" + makeYearAlbumString());

            albumFolderCreated = albumFolder.mkdir();

            for (File file : folderContent) {
                try {
                    if (file.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(file, albumFolder, false);
                        moved = true;
                    } else {
                        FileUtils.moveFileToDirectory(file, albumFolder, false);
                        moved = true;
                    }
                } catch (IOException e) {
                    moved = false;
                }
            }

            try {
                if (moved) {
                    FileUtils.deleteDirectory(parentFolder);
                } else if (moved && !artistFolderCreated) {
                    FileUtils.deleteDirectory(folderContent[0].getParentFile());
                }
            } catch (IOException e) {
                System.out.println("move() va exception occures");
            }
        } else {

            artistFolder = new File(initFolder + "\\" + makeArtistString());
            albumFolder = new File(artistFolder + "\\" + makeYearAlbumString());

            artistFolderCreated = artistFolder.mkdir();
            albumFolderCreated = albumFolder.mkdir();

            for (File file : folderContent) {
                try {
                    if (file.isDirectory()) {
                        FileUtils.moveDirectoryToDirectory(file, albumFolder, false);
                        moved = true;
                    } else {
                        FileUtils.moveFileToDirectory(file, albumFolder, false);
                        moved = true;
                    }
                } catch (IOException e) {
                    System.out.println("move() exception occures");
                    moved = false;
                }
            }

            try {
                if (moved && artistFolderCreated) {
                    FileUtils.deleteDirectory(parentFolder);
                } else if (moved && !artistFolderCreated) {
                    FileUtils.deleteDirectory(folderContent[0].getParentFile());
                }

            } catch (IOException e) {
                System.out.println("move() exception occures");
            }

            //System.out.println(artistFolder.getAbsolutePath());
            //System.out.println(albumFolder.getAbsolutePath());
        }

    }

    private void removeJunkFiles(File dir) {
        String[] filter = {"log", "cue", "db", "m3u"};
        LinkedList<File> list = (LinkedList) FileUtils.listFiles(dir, filter, false);
        for (File file : list) {
            file.delete();
        }
    }

    private String validateName(String name) {
        return name.replaceAll("$", "")
                .replaceAll("—", "")
                .replaceAll("`", "'")
                .replaceAll("<", "")
                .replaceAll(">", "")
                .replaceAll("/", "-")
                .replaceAll("\\\\", "")
                .replaceAll("\\\\", "")
                .replaceAll("\\*", "")
                .replaceAll(":", " -")
                .replaceAll("\"", "")
                .replaceAll("\\?", "");
    }

    public void setInitFolder(File initFolder) {
        this.initFolder = initFolder;
    }

    public void setParentFolder(File parentFolder) {
        this.parentFolder = parentFolder;
    }

    private String getArtistName() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        String artistName = "";
        int count = 0;
        for (Iterator<File> i = audioList.keySet().iterator(); i.hasNext();) {
            MP3File file = new MP3File((File) i.next());
            count++;
            if (!file.getTag().getFirst(FieldKey.ARTIST).isEmpty()) {
                if (count == 1) {
                    artistName = file.getTag().getFirst(FieldKey.ARTIST);
                }
                if (count > 1) {
                    if (!file.getTag().getFirst(FieldKey.ARTIST).equals(artistName)) {
                        artistName = VA;
                        break;
                    } else {
                        artistName = file.getTag().getFirst(FieldKey.ARTIST);
                    }
                }
            }
        }
        return artistName;
    }

    private String getAlbumName() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        String albumName = "";
        int count = 0;
        for (Iterator<File> i = audioList.keySet().iterator(); i.hasNext();) {
            MP3File file = new MP3File((File) i.next());
            count++;
            if (!file.getTag().getFirst(FieldKey.ALBUM).isEmpty()) {
                if (count == 1) {
                    albumName = file.getTag().getFirst(FieldKey.ALBUM);
                }
                if (count > 1) {
                    if (!file.getTag().getFirst(FieldKey.ALBUM).equals(albumName)) {
                        albumName = file.getTag().getFirst(FieldKey.ALBUM);
                    } else {
                        albumName = file.getTag().getFirst(FieldKey.ALBUM);
                    }
                }
            }
        }
        return albumName;
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

    /**
     * @param hasMultiCD the hasMultiCD to set
     */
    public void setHasMultiCD(boolean hasMultiCD) {
        this.hasMultiCD = hasMultiCD;
    }

    public static <K extends Comparable<? super K>, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list
                = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public boolean setCustomTag(AudioFile audioFile, String description, String text) {
        FrameBodyTXXX txxxBody = new FrameBodyTXXX();
        txxxBody.setDescription(description);
        txxxBody.setText(text);

        // Get the tag from the audio file
        // If there is no ID3Tag create an ID3v2.3 tag
        Tag tag = audioFile.getTagOrCreateAndSetDefault();
        // If there is only a ID3v1 tag, copy data into new ID3v2.3 tag
        if (!(tag instanceof ID3v23Tag || tag instanceof ID3v24Tag)) {
            Tag newTagV23 = null;
            if (tag instanceof ID3v1Tag) {
                newTagV23 = new ID3v23Tag((ID3v1Tag) audioFile.getTag()); // Copy old tag data               
            }
            if (tag instanceof ID3v22Tag) {
                newTagV23 = new ID3v23Tag((ID3v11Tag) audioFile.getTag()); // Copy old tag data              
            }
            audioFile.setTag(newTagV23);
        }

        AbstractID3v2Frame frame = null;
        if (tag instanceof ID3v23Tag) {
            frame = new ID3v23Frame("TXXX");
        } else if (tag instanceof ID3v24Tag) {
            frame = new ID3v24Frame("TXXX");
        }

        frame.setBody(txxxBody);

        try {
            tag.addField(frame);
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            return false;
        }

        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @param folderContent the folderContent to set
     */
    public void setFolderContent(File[] folderContent) {
        this.folderContent = folderContent;
    }

}
