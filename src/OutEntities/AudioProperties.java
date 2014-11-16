package OutEntities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

//Сущность для отображения и изменения данных, содержащихся в отдельном аудиофайле
public class AudioProperties {

    private File file;
    private MP3File audioFile;
    private SimpleStringProperty fileName;
    private SimpleStringProperty artistTitle;
    private SimpleStringProperty albumTitle;
    private SimpleStringProperty trackTitle;
    private SimpleStringProperty trackNumber;
    private SimpleStringProperty year;
    private SimpleIntegerProperty cd_n;
    private SimpleListProperty<String> genres;
    private SimpleBooleanProperty hasArtwork;

    private final String[] genreDelimiters = {", ", ";", "\\", "/"};

    public AudioProperties(File file) {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        this.file = file;
        fileName = new SimpleStringProperty(this, "fileName");
        artistTitle = new SimpleStringProperty(this, "artistTitle");
        albumTitle = new SimpleStringProperty(this, "albumTitle");
        trackNumber = new SimpleStringProperty(this, "trackNumber");
        trackTitle = new SimpleStringProperty(this, "trackTitle");
        year = new SimpleStringProperty(this, "year");
        cd_n = new SimpleIntegerProperty(this, "cd_n");
        genres = new SimpleListProperty(this, "genres");
        hasArtwork = new SimpleBooleanProperty(this, "hasArtwork");

        fileName.setValue(file.getName());

        try {
            audioFile = (MP3File) AudioFileIO.read(file);
            artistTitle.setValue(audioFile.getTag().getFirst(FieldKey.ARTIST));
            albumTitle.setValue(audioFile.getTag().getFirst(FieldKey.ALBUM));
            trackTitle.setValue(audioFile.getTag().getFirst(FieldKey.TITLE));
            trackNumber.setValue(audioFile.getTag().getFirst(FieldKey.TRACK));
            
            //Определение года
            String yearStr = audioFile.getTag().getFirst(FieldKey.YEAR);
            if (!"".equals(yearStr)) {
                year.setValue(yearStr);
            } else {
                year.setValue("xxxx");
            }
            
            //Определение номера CD
            String cdNStr = audioFile.getTag().getFirst(FieldKey.DISC_NO);
            if (!"".equals(cdNStr)) {
                cdNStr = cdNStr.replaceFirst("^0", "").replaceAll("\\/.+", "").replaceAll("\\D", "");
                if (!"".equals(cdNStr)) {
                    cd_n.set(Integer.valueOf(cdNStr));
                } else {
                    cd_n.set(0);
                }
            } else {
                cd_n.set(0);
            }
            
            //Определение списка жанров
            ObservableList<String> genresList = FXCollections.observableArrayList();
            String[] genresArr = audioFile.getTag().getFirst(FieldKey.GENRE).split(genreDelimiters[0]);
            if (genresArr.length > 0) {
                for (String g : genresArr) {
                    genresList.add(g);
                }
            } else {
                genresList.add(audioFile.getTag().getFirst(FieldKey.GENRE));
            }
            genres.set(genresList);
            
            //Наличие обложки
            if (!audioFile.getTag().getArtworkList().isEmpty()) {
                hasArtwork.setValue(true);
            } else {
                hasArtwork.setValue(false);
            }
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
            System.out.println("Ошибка чтения файла " + file.getAbsolutePath());
        }
    }

    public File getFile() {
        return file;
    }

    public MP3File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(MP3File audioFile) {
        this.audioFile = audioFile;
    }

    public SimpleStringProperty getFileName() {
        return fileName;
    }

    public void setFileName(SimpleStringProperty fileName) {
        this.fileName = fileName;
    }

    public String getArtistTitle() {
        return artistTitle.getValue();
    }

    public void setArtistTitle(String artistTitle) {
        this.artistTitle.setValue(artistTitle);
    }

    public String getAlbumTitle() {
        return albumTitle.getValue();
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle.setValue(albumTitle);
    }

    public String getTrackTitle() {
        return trackTitle.getValue();
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle.setValue(trackTitle);
    }

    public String getTrackNumber() {
        return trackNumber.getValue();
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber.setValue(trackNumber);
    }

    public String getYear() {
        return year.getValue();
    }

    public void setYear(String year) {
        this.year.setValue(year);
    }

    public int getCdN() {
        return cd_n.getValue();
    }

    public void setCd_n(int cd_n) {
        this.cd_n.set(cd_n);
    }

    public SimpleListProperty<String> getGenres() {
        return genres;
    }

    public void setGenres(SimpleListProperty genres) {
        this.genres = genres;
    }

    public boolean getHasArtwork() {
        return hasArtwork.getValue();
    }

}
