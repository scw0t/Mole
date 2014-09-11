
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

public class AudioWorker {

    private LogOutput logOutput;

    private LinkedList<MP3File> mp3List;
    private File initFolder;
    private File parentFolder;
    private File currentFolder;
    private boolean va;

    private String country;
    private String artistGenres;
    private String recordGenres;
    private String artist;
    private String albumYear;
    private String albumType;

    private RYMParser parser;

    private File[] folderContent;

    public AudioWorker(LinkedList<File> audioBunch) throws IOException,
            TagException, ReadOnlyFileException, InvalidAudioFrameException,
            CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        mp3List = new LinkedList<>();
        country = "";
        artistGenres = "";
        recordGenres = "";
        artist = "";
        albumYear = "";
        albumType = "";
        for (File file : audioBunch) {
            mp3List.add((MP3File) AudioFileIO.read(file));
        }

        folderContent = mp3List.getFirst().getFile().getParentFile().listFiles();
    }

    public void process() throws KeyNotFoundException,
            FieldDataInvalidException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        va = isVA();

        parser = new RYMParser();

        parser.setLogOutput(logOutput);
        parser.setAudioArtistName(getArtistName(1));
        parser.setAudioAlbumName(getAlbumName(1));
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
            } else if(!parser.getRecord().getGenres().isEmpty()){
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

            normalizeTracklist();

            Collections.sort(mp3List, new Comparator<MP3File>() {
                @Override
                public int compare(MP3File o1, MP3File o2) {
                    return o1.getTag().getFirst(FieldKey.TRACK).compareTo(o2.getTag().getFirst(FieldKey.TRACK));
                }
            });

            editTags();
            renameAudio();
            move();
        } else {
            System.out.println("Запись не найдена");
        }

    }

    private void checkDiscography(RYMParser parser) {
        int recordQuantityMatches = 0;

        // Проверяем альбомы
        for (Record record : parser.getAlbumRecords()) {
            if (StringUtils.getLevenshteinDistance(parser.getAudioAlbumName(), record.getName()) < 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(getAlbumName(1), record.getName()));
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
            FieldDataInvalidException {
        int count = 0;
        String track = "";
        for (MP3File mp3 : mp3List) {

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

    private void editTags() throws KeyNotFoundException, FieldDataInvalidException {

        for (MP3File mp3 : mp3List) {
            if (!recordGenres.isEmpty()) {
                mp3.getTag().setField(FieldKey.GENRE, recordGenres);
            }

            if (!artist.isEmpty() && !mp3.getTag().getFirst(FieldKey.ARTIST).equals(artist)) {
                mp3.getTag().setField(FieldKey.ARTIST, artist);
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

            try {
                mp3.commit();
            } catch (CannotWriteException ex) {
                System.out.println("mp3 tag commit failed");
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

    private void renameAudio() {

        int count = 0;
        String track = "";
        File renamedFile;

        for (MP3File mp3 : mp3List) {

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

    private boolean isVA() {
        boolean va = false;
        for (int i = 1; i < mp3List.size(); i++) {
            if (!mp3List
                    .get(i)
                    .getTag()
                    .getFirst(FieldKey.ARTIST)
                    .equals(mp3List.get(i - 1).getTag()
                            .getFirst(FieldKey.ARTIST))) {
                va = true;
                break;
            }
        }
        return va;
    }

    private String makeArtistString() {
        if (va) {
            return "VA";
        } else {
            String artist = validateName(mp3List.getFirst().getTag().getFirst(FieldKey.ARTIST));
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

    private String makeYearAlbumString() {
        String albumString = "";

        HashMap<String, String> yearAlbum = new HashMap<String, String>();
        for (MP3File file : mp3List) {
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

    private void tagsAlright() {
        for (MP3File file : mp3List) {
            String artist = file.getTag().getFirst(FieldKey.ARTIST);
            String album = file.getTag().getFirst(FieldKey.ALBUM);
            String year = file.getTag().getFirst(FieldKey.YEAR);

            if (artist.equals("")) {
                System.out.println("Artist tag not exists");
            }

            if (album.equals("")) {
                System.out.println("Album tag not exists");
            }

            if (year.equals("")) {
                System.out.println("Year tag not exists");
            }
        }
    }

    private void move() {
        File artistFolder;
        File albumFolder;

        boolean moved = false;
        boolean artistFolderCreated = false;
        boolean albumFolderCreated = false;
        removeJunkFiles(mp3List.getFirst().getFile().getParentFile());

        folderContent = mp3List.getFirst().getFile().getParentFile().listFiles();

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
                    FileUtils.deleteDirectory(mp3List.getFirst().getFile().getParentFile());
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
                    FileUtils.deleteDirectory(mp3List.getFirst().getFile().getParentFile());
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

    private String getArtistName(int track) {
        return mp3List.get(track).getTag().getFirst(FieldKey.ARTIST);
    }

    private String getAlbumName(int track) {
        return mp3List.get(track).getTag().getFirst(FieldKey.ALBUM);
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

}
