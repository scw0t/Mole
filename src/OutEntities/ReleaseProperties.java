package OutEntities;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

//Сущность, представлюящая набор данных, характерных для отдельного релиза
public class ReleaseProperties {

    private final ObservableList<ObservableList<AudioProperties>> audioList;

    private ObservableMap<String, String> tracklist; //key - trackNumber, value - title
    private ObservableMap<String, String> genres;            //жанры, встречающиеся в кластере
    private ObservableMap<String, String> albumTitle;        //size()>1 - наличие нескольких альбомов в одном кластере
    private ObservableMap<String, String> artistTitle;       //size()>1 - наличие нескольких исполнителей
    //size()==2 - сплит
    //size()>2 - сборник

    public ReleaseProperties() {
        audioList = FXCollections.observableArrayList();
        albumTitle = FXCollections.observableHashMap();
        artistTitle = FXCollections.observableHashMap();
        genres = FXCollections.observableHashMap();
    }

    public void syncReleaseProperties() {
        if (!audioList.isEmpty()) {
            for (ObservableList<AudioProperties> release : audioList) {
                for (AudioProperties medium : release) {
                    albumTitle.put(medium.getAlbumTitle(), medium.getAlbumTitle());
                    artistTitle.put(medium.getArtistTitle(), medium.getArtistTitle());
                    genres.put(medium.getGenres(), medium.getGenres());
                }
            }
        }
    }

    public int artistsQuantity() {
        return artistTitle.size();
    }

    public int albumsQuantity() {
        return albumTitle.size();
    }

    public void addAudioList(ObservableList<AudioProperties> audioList) {
        this.audioList.add(audioList);
    }

    public ObservableList<ObservableList<AudioProperties>> getAudioList() {
        return audioList;
    }

    public ObservableMap<String, String> getAlbumTitle() {
        return albumTitle;
    }

    public ObservableMap<String, String> getArtistTitle() {
        return artistTitle;
    }

}
