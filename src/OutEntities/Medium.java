package OutEntities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;

public class Medium {

    public static <E> E get(Collection<E> collection, int index) {
        Iterator<E> i = collection.iterator();
        E element = null;
        while (i.hasNext() && index-- >= 0) {
            element = i.next();
        }
        return element;
    }

    private final ItemProperties currentItem;

    private ObservableList<AudioProperties> audioList;

    private final SimpleIntegerProperty cdN;
    private final SimpleStringProperty album;
    private final SimpleStringProperty artist;
    private final SimpleStringProperty year;
    private final SimpleStringProperty genres;
    private SimpleStringProperty type;
    private final SimpleBooleanProperty hasArtwork;
    private final HashMap<String, String> yearAlbum;

    public Medium(ItemProperties currentItem) {
        this.audioList = currentItem.getListOfAudioFiles();
        this.currentItem = currentItem;
        cdN = new SimpleIntegerProperty(this, "cd_num");
        album = new SimpleStringProperty(this, "album");
        artist = new SimpleStringProperty(this, "artist");
        year = new SimpleStringProperty(this, "year");
        genres = new SimpleStringProperty(this, "genres");
        hasArtwork = new SimpleBooleanProperty(this, "hasArtwork");
        type = new SimpleStringProperty(this, "type");
        yearAlbum = new HashMap<>();
    }

    public void look() {
        SortedSet<String> artists = new TreeSet<>();
        SortedSet<String> albums = new TreeSet<>();
        SortedSet<String> genres = new TreeSet<>();
        SortedSet<String> years = new TreeSet<>();

        audioList.stream().map((audio) -> {
            artists.add(audio.getArtistTitle());
            return audio;
        }).map((audio) -> {
            albums.add(audio.getAlbumTitle());
            return audio;
        }).map((audio) -> {
            years.add(audio.getYear());
            return audio;
        }).map((audio) -> {
            audio.getGenres().stream().forEach((genre) -> {
                genres.add(genre);
            });
            return audio;
        }).map((audio) -> {
            if (audio.getCdN() != 0) {
                cdN.set(audio.getCdN());
            }
            return audio;
        }).forEach((audio) -> {
            yearAlbum.put(audio.getAlbumTitle(), audio.getYear());
        });

        if (artists.size() > 1) {
            artist.set("VA");
        } else {
            artist.set(artists.first());
        }

        if (!years.first().equals("xxxx")) { //years.size() == 1 && 
            year.setValue(years.first());
        }

        if (albums.size() > 1) {
            String albStr = "";
            if (albums.size() == 2) {
                if (StringUtils.getLevenshteinDistance(albums.first(), albums.last()) < 2) {
                    albStr = albums.first();
                } else {
                    System.out.println("Album size = 2: \"" + albums.first() + "\", \"" + albums.last() + "\"");
                    for (int i = 0; i < albums.size(); i++) {
                        albStr += get(albums, i);
                        if (i < albums.size() - 1) {
                            albStr += " + ";
                        }
                    }
                }
            }
            album.set(albStr);
        } else {
            album.set(albums.first());
        }

    }

    public ObservableList<AudioProperties> getAudioList() {
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
        this.artist.setValue(artist);
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

    public ItemProperties getCurrentItem() {
        return currentItem;
    }

    public HashMap<String, String> getYearAlbum() {
        return yearAlbum;
    }

    public String getType() {
        return type.getValue();
    }

    public void setType(String type) {
        this.type.setValue(type);
    }
}
