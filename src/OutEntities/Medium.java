package OutEntities;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Medium {

    private ObservableList<AudioProperties> audioList;

    private SimpleIntegerProperty cdN;
    private SimpleStringProperty album;
    private SimpleStringProperty artist;
    private SimpleStringProperty year;
    private SimpleStringProperty genres;

    public Medium(ObservableList<AudioProperties> audioList) {
        this.audioList = audioList;
        cdN = new SimpleIntegerProperty(this, "cd_num");
        album = new SimpleStringProperty(this, "album");
        artist = new SimpleStringProperty(this, "artist");
        year = new SimpleStringProperty(this, "year");
        genres = new SimpleStringProperty(this, "genres");
    }

    public void look() {
        SortedSet<String> artists = new TreeSet<>();
        SortedSet<String> albums = new TreeSet<>();
        SortedSet<String> genres = new TreeSet<>();
        SortedSet<String> years = new TreeSet<>();

        for (AudioProperties audio : audioList) {
            artists.add(audio.getArtistTitle());
            albums.add(audio.getAlbumTitle());
            years.add(audio.getYear());
            for (String genre : audio.getGenres()) {
                genres.add(genre);
            }
            if (audio.getCdN() != 0) {
                cdN.set(audio.getCdN());
            }
        }

        if (artists.size() > 1) {
            artist.set("VA");
        } else {
            artist.set(artists.first());
        }

        if (years.size() == 1 && !years.first().equals("xxxx")) {
            year.setValue(years.first());
        }

        if (albums.size() > 1) {
            String albStr = "";
            for (int i = 0; i < albums.size(); i++) {
                albStr += get(albums, i);
                if (i < albums.size() - 1) {
                    albStr += " + ";
                }
            }
            album.set(albStr);
        } else {
            album.set(albums.first());
        }

    }

    public static <E> E get(Collection<E> collection, int index) {
        Iterator<E> i = collection.iterator();
        E element = null;
        while (i.hasNext() && index-- >= 0) {
            element = i.next(); 
        }
        return element;
    }

    public ObservableList<AudioProperties> getListOfAudioFiles() {
        return audioList;
    }

    public void setAudioList(ObservableList<AudioProperties> audioList) {
        this.audioList = audioList;
    }

    public int getCdN() {
        return cdN.getValue();
    }

    public void setCdN(int cdN) {
        this.cdN.setValue(cdN);
    }

    public String getAlbum() {
        return album.getValue();
    }

    public void setAlbum(String album) {
        this.album.setValue(album);
    }

    public String getArtist() {
        return artist.getValue();
    }

    public void setArtist(String artist) {
        this.artist.getValue();
    }

    public String getYear() {
        return year.getValue();
    }

    public void setYear(String year) {
        this.year.setValue(year);
    }

    public String getGenres() {
        return genres.getValue();
    }

    public void setGenres(String genres) {
        this.genres.setValue(genres);
    }
}
