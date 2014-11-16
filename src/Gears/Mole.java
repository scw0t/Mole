package Gears;

import View.MainGUI;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.Release;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.model.MediumListWs2;
import org.musicbrainz.model.PuidWs2;
import org.musicbrainz.model.TrackWs2;
import org.musicbrainz.model.entity.ArtistWs2;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ArtistResultWs2;

public class Mole extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        
        Scene scene = new Scene(new MainGUI(), 650, 900);
        scene.getStylesheets().add(getClass().getResource("toolbarStyle.css").toExternalForm());

        primaryStage.setTitle("hi!");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
            }
        });

        primaryStage.show();
        this.primaryStage = primaryStage;

        /*try {
         musicbrainzTest();
         } catch (MBWS2Exception ex) {
         Logger.getLogger(Mole.class.getName()).log(Level.SEVERE, null, ex);
         }*/
        //gracenoteTest();
        /*try {
         echonestTest();
         } catch (EchoNestException ex) {
         Logger.getLogger(Mole.class.getName()).log(Level.SEVERE, null, ex);
         //        }*/
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void musicbrainzTest() throws MBWS2Exception {
        String artistName = "Aguaturbia";
        String albumName = "Aguaturbia";
        String album_id = null;

        //Поиск исполнителя
        org.musicbrainz.controller.Artist artistsearch = new org.musicbrainz.controller.Artist();
        artistsearch.search(artistName);

        //Получение списка исполнителей и поиск совпадения
        List<ArtistResultWs2> result = artistsearch.getFullSearchResultList();
        ArtistWs2 artist = new ArtistWs2();

        for (ArtistResultWs2 x : result) {
            if (x.getArtist().toString().toLowerCase().equals(artistName.toLowerCase())) {
                artist = x.getArtist();
                break;
            }
        }

        artistsearch = new org.musicbrainz.controller.Artist();
        artistsearch.lookUp(artist);

        //Получение списка релизов и поиск совпадений
        List<ReleaseGroupWs2> release_groups = artistsearch.getFullReleaseGroupList();
        ReleaseGroupWs2 releasegroup = null;

        for (ReleaseGroupWs2 x : release_groups) {
            if (x.getTitle().toLowerCase().equals(albumName.toLowerCase())) {
                releasegroup = x;
            }
        }

        ReleaseGroup releasegroupsearch = new ReleaseGroup();
        releasegroupsearch.lookUp(releasegroup);

        //Список изданий релиза
        List<ReleaseWs2> releases = releasegroupsearch.getFullReleaseList();
        /*releases.sort(new Comparator<ReleaseWs2>() {
            @Override
            public int compare(ReleaseWs2 t, ReleaseWs2 t1) {
                return t.getYear().compareTo(t1.getYear());
            }
        });*/

        for (ReleaseWs2 release : releases) {
            System.out.println("-----------------------------------");
            System.out.println(release.getId());
            System.out.println(release.getIdUri());
            System.out.println("Artist credit: " + release.getArtistCredit());
            System.out.println("Asin: " + release.getAsin());
            System.out.println("Barcode: " + release.getBarcode());
            System.out.println("CountryId: " + release.getCountryId());
            System.out.println("DateStr: " + release.getDateStr());
            System.out.println("Disambiguation: " + release.getDisambiguation());
            System.out.println("Duration: " + release.getDuration());
            System.out.println("Format: " + release.getFormat());
            System.out.println("LabelInfo: " + release.getLabelInfoString());
            System.out.println("Quality: " + release.getQualityStr());
            System.out.println("Status: " + release.getStatus());
            System.out.println("Year: " + release.getYear());
        }

        ReleaseWs2 album = releases.get(0);

        Release releaselist = new Release();
        releaselist.lookUp(album);

        MediumListWs2 releaselist1 = releaselist.getComplete(album).getMediumList();

        System.out.println(releaselist.getComplete(album).getId());
        System.out.println(releaselist.getComplete(album).getUserRating().getAverageRating());

        List<TrackWs2> tracklist = releaselist1.getCompleteTrackList();

        List<PuidWs2> puids = tracklist.get(0).getRecording().getPuids();

        for (PuidWs2 id : puids) {
            System.out.println(id.getId());
        }

        System.out.println("artist: " + artist);
        System.out.println("album: " + album);
        System.out.println("title: " + tracklist.get(0).getRecording().getTitle());
        System.out.println("genre: " + tracklist.get(0).getRecording().getTags().get(0).getName());
        System.out.println("track: " + tracklist.get(0).getPosition());
        System.out.println("year: " + album.getYear());
        System.out.println("disc no.: " + releaselist1.getMedia().get(0));
        System.out.println("label: " + album.getLabelInfoString());
        System.out.println("artist sort : " + tracklist.get(0).getRecording().getArtistCreditString());
        System.out.println("comment: " + tracklist.get(0).getRecording().getDisambiguation());
    }

    /*private void gracenoteTest() throws GracenoteException {
        String clientID = "16430336";
        String clientTag = "F0C6EBDA21CDC1480EA44F1C3D504F9D";
        String userID = "264116179042661687-1A9143576C10CBAFB5E42829DCF98066";
        GracenoteWebAPI api = new GracenoteWebAPI(clientID, clientTag, userID);
        //GracenoteMetadata results = api.searchTrack("King Crimson", "The Great Deceiver", "The Talking Drum");
        GracenoteMetadata test2 = api.searchAlbum("Aguaturbia", "Aguaturbia");
    }*/

    private void echonestTest() throws EchoNestException {
        String APIKey = "97SNZ1U81BZI1MTHR";

        /*ArtistExamples sse = new ArtistExamples(APIKey);
         sse.searchArtistByName("Arco Iris", 10);
         sse.stats();*/
        EchoNestAPI echoNest = new EchoNestAPI(APIKey);
        echoNest.setTraceSends(true);
        List<com.echonest.api.v4.Artist> artists = echoNest.searchArtists("After All");
        if (artists.size() > 0) {
            com.echonest.api.v4.Artist artist = artists.get(0);
            System.out.println("Similar artists for " + artist.getName());
            for (com.echonest.api.v4.Artist simArtist : artist.getSimilar(10)) {
                System.out.println(" " + simArtist.getName());
            }
        }

    }

}
