package Gears;

import Entities.Issue;
import OutEntities.AudioProperties;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import View.Controller;
import java.io.File;
import java.io.IOException;
import static java.lang.Integer.valueOf;
import static java.lang.System.out;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR;
import static org.apache.commons.io.FilenameUtils.getExtension;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
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
import static org.jaudiotagger.tag.FieldKey.YEAR;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.StandardArtwork;

public class FinalProcess {

    private final ItemProperties rootItem;
    private Issue selectedIssue;
    private RYMParser rymp;
    private final Artwork artwork;

    private String outputCountryValue = "";
    private String outputArtistGenresValue = "";
    private String outputRecordGenresValue = "";
    private String outputArtistValue = "";
    private String outputAlbumYearValue = "";
    private String outputAlbumTypeValue = "";

    private final File destination;

    /**
     *
     * @param rootItem
     */
    public FinalProcess(ItemProperties rootItem) {
        this.rootItem = rootItem;
        artwork = new StandardArtwork();
        destination = new File(Controller.pathTextArea.getText());
    }

    /**
     *
     * @throws java.io.IOException
     * @throws org.jaudiotagger.tag.TagException
     * @throws org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
     * @throws org.jaudiotagger.tag.FieldDataInvalidException
     * @throws org.jaudiotagger.audio.exceptions.ReadOnlyFileException
     * @throws org.jaudiotagger.audio.exceptions.CannotReadException
     */
    public void launch() throws KeyNotFoundException, IOException, TagException, FieldDataInvalidException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        fillOutputValues();

        try {
            if (rootItem.getChildList().isEmpty()) {
                for (Medium medium : rootItem.getMediumList()) {
                    fixTags(medium);
                }
            } else {
                if (!rootItem.getMediumList().isEmpty()) {
                    for (Medium medium : rootItem.getMediumList()) {
                        fixTags(medium);
                    }
                }

                for (ItemProperties child : rootItem.getChildList()) {
                    for (Medium medium : child.getMediumList()) {
                        fixTags(medium);
                    }
                }
            }

        } catch (KeyNotFoundException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
            System.out.println("Error occured while fixing tags process");
        }

        if (destination.exists()) {
            if (rootItem.getChildList().isEmpty()) {
                moveFactory(rootItem);
                removeFileAndParentsIfEmpty(rootItem.getParentDir().getValue().toPath());
            } else {
                for (ItemProperties child : rootItem.getChildList()) {
                    moveFactory(child);
                    removeFileAndParentsIfEmpty(child.getCurrentDir().getValue().toPath());
                }
                removeFileAndParentsIfEmpty(rootItem.getParentDir().getValue().toPath());
            }

        } else {
            System.out.println(destination.getAbsolutePath() + " not exists");
        }

        /*out.println(outputArtistValue);
         out.println(outputAlbumYearValue);
         out.println(outputAlbumTypeValue);
         out.println(outputArtistGenresValue);
         out.println(outputRecordGenresValue);*/
    }

    private void fixTags(Medium medium) throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {

        int trackCount = 1;
        String trackString = "";
        boolean artworkInFolderExists = artworkInFolderExists(medium);

        for (AudioProperties audio : medium.getAudioList()) {

            MP3File file = audio.getAudioFile();

            //Запись тега TRACK
            if (trackCount < 10) {
                trackString = "0" + Integer.toString(trackCount);
            } else {
                trackString = Integer.toString(trackCount);
            }

            file.getTag().setField(TRACK, trackString);

            //Запись тега TRACK_TOTAL
            //file.getTag().setField(TRACK_TOTAL, Integer.toString(medium.getAudioList().size()));
            //Запись тега DISC_NO
            if (file.getTag().getFirst(DISC_NO).isEmpty()) {
                if (audio.getCdN() == 0 && rootItem.getCdN() > 0) {
                    file.getTag().setField(DISC_NO, Integer.toString(medium.getCdN()));
                }
            }

            //Запись тега ARTIST
            if (!outputArtistValue.isEmpty() && !file.getTag().getFirst(ARTIST).equals(outputArtistValue)
                    && !outputArtistValue.equals("Various Artists")) {
                file.getTag().setField(ARTIST, outputArtistValue);
            }

            if (outputArtistValue.equals("Various Artists")) {
                file.getTag().setField(FieldKey.IS_COMPILATION, Integer.toString(1));
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

    private void renameAudio(Medium medium) throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {

        File newFile;
        ObservableList<AudioProperties> renamedAudioList = FXCollections.observableArrayList();

        for (int i = 0; i < medium.getAudioList().size(); i++) {
            MP3File oldFile = medium.getAudioList().get(i).getAudioFile();

            if (!oldFile.getTag().getFirst(TRACK).isEmpty()
                    && !oldFile.getTag().getFirst(TITLE).isEmpty()) {
                if (rootItem.hasVaAttribute()) {
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

                renamedAudioList.add(new AudioProperties(newFile));

                if (!oldFile.getFile().getName().equals(newFile.getName())) {
                    out.println(oldFile.getFile().getName());

                    try {
                        oldFile.getFile().renameTo(newFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                    }

                }
            }
        }
        if (!renamedAudioList.isEmpty()) {
            medium.setAudioList(renamedAudioList);
        }
    }

    private String buildArtistString() {
        if (rootItem.hasVaAttribute()) {
            return "VA";
        } else {
            StringBuilder artistString = new StringBuilder();
            if (outputArtistValue.startsWith("The ")) {
                artistString.append(validateName(outputArtistValue).replaceFirst("The ", "")).append(", The");
            } else {
                artistString.append(validateName(outputArtistValue));
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

        if (!rootItem.getChildList().isEmpty()) {
            for (ItemProperties child : rootItem.getChildList()) {
                for (Medium medium : child.getMediumList()) {
                    if (rootItem.hasVaAttribute()) { //если VA
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
                }
                if (rootItem.getCdN() > 0) {
                    break;
                }
            }
        } else {
            for (Medium medium : rootItem.getMediumList()) {
                if (rootItem.hasVaAttribute()) { //если VA
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
            }
        }

        if (rootItem.getCdN() > 0) {
            albumString.append(" [").append(rootItem.getCdN()).append("CD]");
        }

        return albumString.toString();
    }

    private void moveFactory(ItemProperties item) throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {

        int cdN = 0;

        if (item.getMediumList().size() == 1) {
            cdN = findCdN(item.getMediumList().get(0));
        } else if (item.getMediumList().size() > 1) {
            System.out.println("Unhandeled case: getMediumList().size() > 1");
        }

        if (rootItem.hasVaAttribute()) {
            File newAlbumDirectory = new File(destination + "\\" + buildAlbumString());
            newAlbumDirectory.mkdir();

            if (cdN > 0) {
                File cdDirectory = new File(newAlbumDirectory + "\\" + "CD" + cdN);
                cdDirectory.mkdir();
                organiseAll(item, cdDirectory);
            } else {
                organiseAll(item, newAlbumDirectory);
            }

            if (!rootItem.getChildList().isEmpty()) {
                item = rootItem;
            }

            organiseAll(item, newAlbumDirectory);

        } else {
            File newArtistDirectory = new File(destination + "\\" + buildArtistString());
            File newAlbumDirectory = new File(newArtistDirectory + "\\" + buildAlbumString());

            newArtistDirectory.mkdir();
            newAlbumDirectory.mkdir();

            if (cdN > 0) {
                File cdDirectory = new File(newAlbumDirectory + "\\" + "CD" + cdN);
                cdDirectory.mkdir();
                organiseAll(item, cdDirectory);
            } else {
                organiseAll(item, newAlbumDirectory);
            }

            if (!rootItem.getChildList().isEmpty()) {
                item = rootItem;
            }

            organiseAll(item, newAlbumDirectory);
        }

    }

    private void organiseAll(ItemProperties ip, File to) {
        organiseAudio(ip, to);
        organiseImages(ip, to);
        organiseOthers(ip, to);
    }

    private void organiseAudio(ItemProperties ip, File to) {
        if (!ip.getListOfAudioFiles().isEmpty()) {
            for (AudioProperties audio : ip.getListOfAudioFiles()) {
                move(audio.getFile(), to);
            }
        }
    }

    private void organiseImages(ItemProperties ip, File to) {
        if (!ip.getListOfImageFiles().isEmpty()) {
            if (ip.getListOfImageFiles().size() > 1) {
                File coversFolder = new File(to + "\\" + "Covers");
                coversFolder.mkdir();
                for (File image : ip.getListOfImageFiles()) {
                    move(image, coversFolder);
                }
            } else {
                File origFolderFile = ip.getListOfImageFiles().get(0);
                if (!origFolderFile.getName().contains("folder")) {
                    File newFolderImage = new File(to + "\\" + "folder"
                            + EXTENSION_SEPARATOR + getExtension(ip.getListOfImageFiles().get(0).getName()));
                    ip.getListOfImageFiles().get(0).renameTo(newFolderImage);
                } else {
                    move(origFolderFile, to);
                }
            }
        }
    }

    private void organiseOthers(ItemProperties ip, File to) {
        if (!ip.getListOfOtherFiles().isEmpty()) {
            removeJunkFiles(ip.getListOfOtherFiles());
        }

        if (!ip.getListOfOtherFiles().isEmpty()) {
            for (File otherFile : ip.getListOfOtherFiles()) {
                move(otherFile, to);
            }
        }
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

    private void move(File from, File to) {
        try {
            if (!to.exists()) {
                if (from.isDirectory()) {
                    FileUtils.moveDirectoryToDirectory(from, to, false);
                } else {
                    FileUtils.moveFileToDirectory(from, to, false);
                }
            }
        } catch (IOException e) {
            System.out.println("move() exception\nFrom " + from + "\nTo " + to);
        }
    }

    private int findCdN(Medium medium) {
        int cdN = 0;
        if (medium.getCdN() != 0) {
            cdN = medium.getCdN();
        } else if (rootItem.getCdN() != 0) {
            cdN = rootItem.getCdN();
        }

        return cdN;
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

    public void removeFileAndParentsIfEmpty(Path path)
            throws IOException {
        if (path == null || path.endsWith(rootItem.getParentDir().getValue().getParentFile().getPath())) {
            return;
        }

        if (Files.isDirectory(path)) {
            try {
                Files.delete(path);
            } catch (DirectoryNotEmptyException e) {
                return;
            }
        }

        removeFileAndParentsIfEmpty(path.getParent());
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
                System.out.println("Unhandeled case: ");
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

    /*public static <K extends Comparable<? super K>, V extends Comparable<? super V>> Map<K, V>
     sortByValueThenByKey(Map<K, V> map) {
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
     }/*

     /*public boolean setCustomTag(AudioFile audioFile, String description, String text) {
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
     }*/

    public ItemProperties getRootItem() {
        return rootItem;
    }

    public RYMParser getRymp() {
        return rymp;
    }
}
