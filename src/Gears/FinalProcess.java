package Gears;

import Entities.Issue;
import OutEntities.AudioProperties;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.valueOf;
import static java.lang.System.out;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR;
import static org.apache.commons.io.FilenameUtils.getExtension;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import static org.jaudiotagger.tag.FieldKey.ALBUM;
import static org.jaudiotagger.tag.FieldKey.ARTIST;
import static org.jaudiotagger.tag.FieldKey.ARTIST_SORT;
import static org.jaudiotagger.tag.FieldKey.CATALOG_NO;
import static org.jaudiotagger.tag.FieldKey.COMMENT;
import static org.jaudiotagger.tag.FieldKey.COUNTRY;
import static org.jaudiotagger.tag.FieldKey.DISC_NO;
import static org.jaudiotagger.tag.FieldKey.GENRE;
import static org.jaudiotagger.tag.FieldKey.RECORD_LABEL;
import static org.jaudiotagger.tag.FieldKey.TITLE;
import static org.jaudiotagger.tag.FieldKey.TRACK;
import static org.jaudiotagger.tag.FieldKey.TRACK_TOTAL;
import static org.jaudiotagger.tag.FieldKey.YEAR;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

/**
 *
 * @author p51_evseevvy
 */
public class FinalProcess {

    private final ItemProperties itemProperties;
    private Issue selectedIssue;
    private RYMParser rymp;
    private final Artwork artwork;

    private String outputCountryValue = "";
    private String outputArtistGenresValue = "";
    private String outputRecordGenresValue = "";
    private String outputArtistValue = "";
    private String outputAlbumYearValue = "";
    private String outputAlbumTypeValue = "";

    /**
     *
     * @param itemProperties
     */
    public FinalProcess(ItemProperties itemProperties) {
        this.itemProperties = itemProperties;
        artwork = new StandardArtwork();
    }

    /**
     *
     */
    public void launch() {
        fillOutputValues();

        try {
            fixTracklistAndCDnAndArtwork();
        } catch (KeyNotFoundException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
        }

        out.println(outputArtistValue);
        out.println(outputAlbumYearValue);
        out.println(outputAlbumTypeValue);
        out.println(outputArtistGenresValue);
        out.println(outputRecordGenresValue);
    }
    
    //ПЕРЕДЕЛАТЬ для ChildList, используя listOfAudio, listOfImages

    //
    private void fixTracklistAndCDnAndArtwork() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {
        for (Medium medium : itemProperties.getMediumList()) {

            int trackCount = 1;
            String trackString = "";
            boolean artworkInFolderExists = artworkInFolderExists(medium);

            for (AudioProperties audio : medium.getListOfAudioFiles()) {

                MP3File file = audio.getAudioFile();

                //Запись тега TRACK
                if (trackCount < 10) {
                    trackString = "0" + Integer.toString(trackCount);
                } else {
                    trackString = Integer.toString(trackCount);
                }

                file.getTag().setField(TRACK, trackString);

                //Запись тега TRACK_TOTAL
                file.getTag().setField(TRACK_TOTAL, Integer.toString(medium.getListOfAudioFiles().size()));

                //Запись тега DISC_NO
                if (file.getTag().getFirst(DISC_NO).isEmpty()) {
                    if (medium.getCdN() > 0) {
                        file.getTag().setField(DISC_NO, Integer.toString(audio.getCdN()));
                    }
                }

                //Запись тега ARTIST
                if (!outputArtistValue.isEmpty() && !file.getTag().getFirst(ARTIST).equals(outputArtistValue)) {
                    file.getTag().setField(ARTIST, outputArtistValue);
                }

                //Запись тега ARTIST_SORT
                if (outputArtistValue.startsWith("The ")) {
                    file.getTag().setField(ARTIST_SORT, outputArtistValue.replaceFirst("The ", "") + ", The");
                } else {
                    file.getTag().setField(ARTIST_SORT, outputArtistValue);
                }

                //Запись тега YEAR. Если в поле YEAR уже есть значение, то сохраняется наименьшее
                if (!outputAlbumYearValue.isEmpty() && !file.getTag().getFirst(YEAR).equals(outputAlbumYearValue)) {
                    if (!file.getTag().getFirst(YEAR).isEmpty()) {
                        if (valueOf(outputAlbumYearValue) < valueOf(file.getTag().getFirst(YEAR))) {
                            file.getTag().setField(YEAR, outputAlbumYearValue);
                        }
                    } else {
                        file.getTag().setField(YEAR, outputAlbumYearValue);
                    }
                }

                //Запись тега GENRE
                if (!outputRecordGenresValue.isEmpty()) {
                    file.getTag().setField(GENRE, outputRecordGenresValue);
                }

                //Запись тега COUNTRY
                if (!outputCountryValue.isEmpty()) {
                    file.getTag().setField(COUNTRY, outputCountryValue);
                }

                //Если выбрано издание
                if (selectedIssue != null) {
                    //Запись тега ALBUM
                    if (selectedIssue.getIssueTitle() != null) {
                        file.getTag().setField(ALBUM, selectedIssue.getIssueTitle());
                    }

                    //Запись тега RECORD_LABEL
                    if (selectedIssue.getIssueLabel() != null) {
                        if (!selectedIssue.getIssueLabel().equals("Unknown")) {
                            file.getTag().setField(RECORD_LABEL, selectedIssue.getIssueLabel());
                        }
                    }

                    //Запись тега CATALOG_NO
                    if (selectedIssue.getCatNumber() != null) {
                        if (!selectedIssue.getCatNumber().isEmpty()) {
                            file.getTag().setField(CATALOG_NO, selectedIssue.getCatNumber());
                        }
                    }
                }

                //Запись artwork
                if (artworkInFolderExists && !audioArtworkExists(file)) {
                    file.getTag().setField(artwork);
                }

                //Запись тега COMMENT
                file.getTag().setField(COMMENT, "(c) Scwot");

                //Применение изменений
                try {
                    file.commit();
                } catch (CannotWriteException e) {
                } finally {
                    trackCount++;
                }
            }
        }
    }

    private void renameAudio() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {

        File newFile;

        for (Medium medium : itemProperties.getMediumList()) {
            for (AudioProperties audio : medium.getListOfAudioFiles()) {
                MP3File oldFile = audio.getAudioFile();

                if (!oldFile.getTag().getFirst(TRACK).isEmpty()
                        && !oldFile.getTag().getFirst(TITLE).isEmpty()) {
                    if (itemProperties.hasVaAttribute()) {
                        newFile = new File(oldFile.getFile().getParentFile()
                                .getAbsolutePath()
                                + "\\"
                                + oldFile.getTag().getFirst(TRACK)
                                + " - "
                                + validateName(oldFile.getTag().getFirst(ARTIST))
                                + " - "
                                + validateName(oldFile.getTag().getFirst(TITLE))
                                + EXTENSION_SEPARATOR
                                + getExtension(oldFile.getFile().getName()));
                    } else {
                        newFile = new File(oldFile.getFile().getParentFile()
                                .getAbsolutePath()
                                + "\\"
                                + oldFile.getTag().getFirst(TRACK)
                                + " - "
                                + validateName(oldFile.getTag().getFirst(TITLE))
                                + EXTENSION_SEPARATOR
                                + getExtension(oldFile.getFile().getName()));
                    }

                    if (!oldFile.getFile().getName().equals(newFile.getName())) {
                        out.println(oldFile.getFile().getName());

                        try {
                            oldFile.getFile().renameTo(newFile);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private String buildArtistString() {
        if (itemProperties.hasVaAttribute()) {
            return "VA";
        } else {
            StringBuilder artistString = new StringBuilder();
            artistString.append(validateName(outputArtistValue));

            if (artistString.toString().startsWith("The ")) {
                artistString.append(artistString.toString().replaceFirst("The ", "")).append(", The");
            }

            if (!outputCountryValue.isEmpty() || !outputArtistGenresValue.isEmpty()) {
                artistString.append(" [");
            }

            if (!outputCountryValue.isEmpty()) {
                artistString.append(outputCountryValue).append(" ");
            }

            if (!outputArtistGenresValue.isEmpty()) {
                artistString.append(validateName(outputArtistGenresValue));
            }

            if (!outputCountryValue.isEmpty() || !outputArtistGenresValue.isEmpty()) {
                artistString.append("]");
            }
            return artistString.toString();
        }
    }

    private String buildAlbumString() {
        StringBuilder albumString = new StringBuilder();
        
        

        itemProperties.getMediumList().stream().map((medium) -> {
            Set<Map.Entry<String, String>> set = medium.getYearAlbum().entrySet();
            return medium;
        }).forEach((medium) -> {
            if (itemProperties.hasVaAttribute()) { //если VA
                albumString.append("VA")
                        .append(" - ")
                        .append(medium.getAlbum())
                        .append(" (")
                        .append(medium.getYear())
                        .append(")");
            } else { //если обычный релиз
                if (selectedIssue != null) { //если выбрано издание
                    albumString.append(selectedIssue.getIssueYear())
                            .append(" - ")
                            .append(selectedIssue.getIssueTitle());
                } else { //если ничего не выбрано
                    albumString.append(medium.getYear())
                            .append(" - ")
                            .append(medium.getAlbum());
                }
            }
        });

        return albumString.toString();
    }

    private void move() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {
        File newArtistFolder;
        File newAlbumFolder;

        boolean moved = false;
        boolean artistFolderCreated = false;
        boolean albumFolderCreated = false;

        itemProperties.getMediumList().stream().map((medium) -> {
            if (medium.getCurrentItem().getListOfOtherFiles().size() > 0) {
                removeJunkFiles(medium.getCurrentItem().getListOfOtherFiles());
            }
            return medium;            
        }).forEach((_item) -> {
            if (itemProperties.hasVaAttribute()) {
                
            } else {
                
            }
        }); /*removeJunkFiles(folderContent[0].getParentFile());
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
        } catch (Exception e) {
        System.out.println("move() exception occures");
        //e.printStackTrace();
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
        }*/

    }

    private void removeJunkFiles(ObservableList<File> listOfOtherFiles) {
        for (int i = 0; i < listOfOtherFiles.size(); i++) {
            String extention = getExtension(listOfOtherFiles.get(i).getName());
            if (extention.equals("log")
                    || extention.equals("cue")
                    || extention.equals("db")
                    || extention.equals("m3u")
                    || extention.equals("accurip")) {
                listOfOtherFiles.get(i).delete();
                listOfOtherFiles.remove(i);
            }
        }
    }

    //Определение переменных для записи в теги
    private void fillOutputValues() {
        if (rymp.getCurrentArtist() != null) {
            if (!rymp.getCurrentArtist().getName().isEmpty()) {
                outputArtistValue = rymp.getCurrentArtist().getName();
            }
            if (!rymp.getCurrentArtist().getGenres().isEmpty()) {
                outputArtistGenresValue = rymp.getCurrentArtist().firstThreeGenresToString();
            } else if (rymp.getCurrentRecord() != null && !rymp.getCurrentRecord().getGenres().isEmpty()) {
                outputArtistGenresValue = rymp.getCurrentRecord().firstThreeGenresToString();
            }

            if (!rymp.getCurrentArtist().getCountry().isEmpty()) {
                outputCountryValue = rymp.getCurrentArtist().getCountry();
            }
        } else {
        }

        if (rymp.getCurrentRecord() != null) {
            if (!rymp.getCurrentRecord().getGenres().isEmpty()) {
                outputRecordGenresValue = rymp.getCurrentRecord().allGenresToString();
            } else if (rymp.getCurrentArtist() != null && !rymp.getCurrentArtist().getGenres().isEmpty()) {
                outputRecordGenresValue = rymp.getCurrentArtist().allGenresToString();
            }

            if (!rymp.getCurrentRecord().getYearRecorded().isEmpty()) {
                outputAlbumYearValue = rymp.getCurrentRecord().getYearRecorded();
            } else if (!rymp.getCurrentRecord().getYearReleased().isEmpty()) {
                outputAlbumYearValue = rymp.getCurrentRecord().getYearReleased();
            }

            if (!rymp.getCurrentRecord().getType().isEmpty()) {
                outputAlbumTypeValue = rymp.getCurrentRecord().getType();
            }
        } else {
        }
    }

    //Проверка существования обложки внутри аудиофайла
    private boolean audioArtworkExists(MP3File file) {
        return !file.getTag().getArtworkList().isEmpty();
    }

    //Проверка существования обложки в виде файла в папке
    private boolean artworkInFolderExists(Medium medium) {
        boolean exists = false;
        if (medium.getCurrentItem().hasImageAttribute()) {
            for (File imageFile : medium.getCurrentItem().getListOfImageFiles()) {
                if (imageFile.getName().contains("folder")) {
                    try {
                        artwork.setFromFile(imageFile);
                        exists = true;
                        break;
                    } catch (IOException ex) {
                    }
                }
            }
        } else {
            if (medium.getCurrentItem().getCdN() > 0) {

            }
        }
        return exists;
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

    /**
     *
     * @return
     */
    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    /**
     *
     * @param selectedIssue
     */
    public void setSelectedIssue(Issue selectedIssue) {
        this.selectedIssue = selectedIssue;
    }

    /**
     *
     * @param rymp
     */
    public void setRymp(RYMParser rymp) {
        this.rymp = rymp;
    }
    private static final Logger LOG = Logger.getLogger(FinalProcess.class.getName());

}
