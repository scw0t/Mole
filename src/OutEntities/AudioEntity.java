package OutEntities;

import java.io.File;
import java.io.IOException;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

public class AudioEntity {
    
    private File file;
    private MP3File audioFile;
    private SimpleStringProperty fileName;
    private SimpleStringProperty artistTitle;
    private SimpleStringProperty albumTitle;
    private SimpleStringProperty trackTitle;
    private SimpleStringProperty trackNumber;
    private SimpleStringProperty year;
    private SimpleStringProperty cd_n;
    private SimpleStringProperty genres;
    private SimpleBooleanProperty hasArtwork;
    

    public AudioEntity(File file) {
        this.file = file;
        fileName = new SimpleStringProperty(this, "fileName");
        artistTitle = new SimpleStringProperty(this, "artistTitle");
        albumTitle = new SimpleStringProperty(this, "albumTitle");
        trackNumber = new SimpleStringProperty(this, "trackNumber");
        trackTitle = new SimpleStringProperty(this, "trackTitle");
        year = new SimpleStringProperty(this, "year");
        cd_n = new SimpleStringProperty(this, "cd_n");
        genres = new SimpleStringProperty(this, "genres");
        hasArtwork = new SimpleBooleanProperty(this, "hasArtwork");
        
        fileName.setValue(file.getName());
        
        try {
            audioFile = (MP3File) AudioFileIO.read(file);
            artistTitle.setValue(audioFile.getTag().getFirst(FieldKey.ARTIST));
            albumTitle.setValue(audioFile.getTag().getFirst(FieldKey.ALBUM));
            trackTitle.setValue(audioFile.getTag().getFirst(FieldKey.TITLE));
            trackNumber.setValue(audioFile.getTag().getFirst(FieldKey.TRACK));
            year.setValue(audioFile.getTag().getFirst(FieldKey.YEAR));
            cd_n.setValue(audioFile.getTag().getFirst(FieldKey.DISC_NO));
            genres.setValue(audioFile.getTag().getFirst(FieldKey.GENRE));
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

    public String getCd_n() {
        return cd_n.getValue();
    }

    public void setCd_n(String cd_n) {
        this.cd_n.setValue(cd_n);
    }

    public String getGenres() {
        return genres.getValue();
    }

    public void setGenres(String genres) {
        this.genres.setValue(genres);
    }

    public boolean getHasArtwork() {
        return hasArtwork.getValue();
    }
    
}
