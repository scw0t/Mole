package Gears;

import Entities.Issue;
import Entities.Record;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import java.io.IOException;
import static java.lang.System.out;
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseGroupResultWs2;

public class ParseFactory {

    private final ItemProperties rootItem;
    private final RYMParser rymp;
    private final SimpleListProperty<Issue> issueListProperty;
    private final ObservableList<Issue> issueList;
    private StringProperty message;

    /**
     *
     * @param rootItem
     */
    public ParseFactory(ItemProperties rootItem) {
        this.rootItem = rootItem;
        message = new SimpleStringProperty();
        issueList = observableArrayList();
        issueListProperty = new SimpleListProperty<>(issueList);
        rymp = new RYMParser();
        rymp.setMessage(message);
    }

    public void launch() {
        if (rootItem.hasVaAttribute()) {                      //СБОРНИК
            if (rootItem.getCdN() > 0) {
                //CD>1
            } else {
                //1
            }

        } else {                                                //ОБЫЧНЫЙ
            /*if (rootItem.getCdN() > 0) {                        //CD>1
             iterateItem(rootItem);

             } else {                                            //1
             if (!rootItem.getChildList().isEmpty()) {
             for (ItemProperties child : rootItem.getChildList()) {
             for (Medium medium : child.getMediumList()) {
             searchCommonRelease(medium);
             }
             }
             } else {
             for (Medium medium : rootItem.getMediumList()) {
             searchCommonRelease(medium);
             }
             }
             }*/
            iterateItem(rootItem);
        }
    }

    private void iterateItem(ItemProperties item) {
        boolean passed = false;

        if (!item.getChildList().isEmpty() && item.getMediumList().isEmpty()) {
            for (ItemProperties child : item.getChildList()) {
                if (child.hasAudioAttribute()) {
                    searchCommonRelease(child.getMediumList().get(0));
                    //passed = true;
                    break;
                }
            }

            /*if (!passed && !item.getMediumList().isEmpty()) {
             searchCommonRelease(item.getMediumList().get(0));
             }*/
        } else {
            if (item.hasAudioAttribute()) {
                searchCommonRelease(item.getMediumList().get(0));
            }
        }
    }

    private void searchCommonRelease(Medium medium) {
        boolean passed = false;
        rymp.setInputArtistNameAndInitUrl(medium.getArtist());
        rymp.setInputAlbumNameAndInitUrl(medium.getAlbum());

        //поиск по имени релиза
        if (rymp.parseAlbumInfo()) {
            rymp.parseArtistInfo(rymp.getRymArtistName());
            passed = true;
        } else {
            if (rymp.parseArtistInfo(medium.getArtist())) {
                try {
                    checkDiscography();
                    passed = true;
                } catch (KeyNotFoundException |
                        IOException |
                        TagException |
                        ReadOnlyFileException |
                        InvalidAudioFrameException ex) {
                }
            } else {
                System.out.println("ParseFactory: artist parsing failure");
            }
        }

        if (rymp.getCurrentRecord() != null) {
            if (rymp.getCurrentRecord().getIssues() != null && !rymp.getCurrentRecord().getIssues().isEmpty()) {
                issueList.addAll(rymp.getCurrentRecord().getIssues());
            }
        }

        if (rymp.getCurrentRecord() != null) {
            medium.setType(rymp.getCurrentRecord().getType());
        }

        try {
            musicbrainzTest(medium);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //gracenoteTest();
//        try {
//            echonestTest(medium);
//        } catch (EchoNestException ex) {
//            Logger.getLogger(Mole.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void musicbrainzTest(Medium medium) throws MBWS2Exception {

        String artistName = medium.getArtist();
        String albumName = medium.getAlbum();

        try {
            if (medium.getAlbum() != null) {
                System.out.println("-----------MusicBrainz Trace---------------");

                org.musicbrainz.controller.ReleaseGroup rg = new org.musicbrainz.controller.ReleaseGroup();
                rg.search(albumName); //получаем список релизов с МВ

                List<ReleaseGroupResultWs2> rgList = rg.getFirstSearchResultPage(); //берем первую страницу
                ReleaseGroupWs2 matchedRelease = new ReleaseGroupWs2();

                //сравниваем группы релизов из МВ с текущим именем релиза, 
                //запоминаем полученную группу релизов
                for (ReleaseGroupResultWs2 match : rgList) {
                    if (checkMusicbrainzReleases(match, albumName, artistName)) {
                        out.println(match.getReleaseGroup().getArtistCreditString());
                        out.println(match.getReleaseGroup().toString());
                        matchedRelease = match.getReleaseGroup();
                        break;
                    }
                }

                //в группе релизов ищем отдельные издания
                if (matchedRelease != null) {
                    rg = new ReleaseGroup();
                    rg.lookUp(matchedRelease);
                    List<ReleaseWs2> releaseList = rg.getFirstReleaseListPage();

                    if (releaseList != null) {
                        for (ReleaseWs2 release : releaseList) {
                            out.println("Artist credit: " + release.getArtistCredit());
                            out.println("Asin: " + release.getAsin());
                            out.println("Barcode: " + release.getBarcode());
                            out.println("CountryId: " + release.getCountryId());
                            out.println("DateStr: " + release.getDateStr());
                            out.println("Disambiguation: " + release.getDisambiguation());
                            //out.println("Duration: " + release.getDuration());
                            out.println("Format: " + release.getFormat());
                            out.println("LabelInfo: " + release.getLabelInfoString());
                            out.println("Status: " + release.getStatus());
                            out.println("Year: " + release.getYear());
                            out.println("#################");
                        }
                    }
                } else {
                    System.out.println("matchedRelease == null");
                }

                out.println("-----------End of MusicBrainz Trace---------------");
            } else {
                System.out.println("Medium.getAlbum() == null");
            }

        } catch (org.musicbrainz.webservice.RequestException | NullPointerException e) {
            System.out.println(e.getMessage());
        }

    }

    private boolean checkMusicbrainzReleases(ReleaseGroupResultWs2 match, String albumName, String artistName) {
        boolean result = false;
        
        String fixedArtistName1 = artistName.startsWith("The ") ? artistName.replaceFirst("The ", "") : artistName;
        String fixedArtistName2 = !artistName.startsWith("The ") ? "The " + artistName : artistName;

        String fixedAlbumName1 = albumName.startsWith("The ") ? albumName.replaceFirst("The ", "") : albumName;
        String fixedAlbumName2 = !albumName.startsWith("The ") ? "The " + albumName : albumName;

        if (match.getReleaseGroup().toString().toLowerCase().equals(fixedAlbumName1.toLowerCase())
                || match.getReleaseGroup().toString().toLowerCase().equals(fixedAlbumName2.toLowerCase())) {
            if (match.getReleaseGroup().getArtistCreditString().toLowerCase().equals(fixedArtistName1.toLowerCase())
                    || match.getReleaseGroup().getArtistCreditString().toLowerCase().equals(fixedArtistName2.toLowerCase())) {
                result = true;
            }
        }

        return result;
    }

    /*private void echonestTest(Medium medium) throws EchoNestException {
     String APIKey = "97SNZ1U81BZI1MTHR";

     //        ArtistExamples sse = new ArtistExamples(APIKey);
     //         sse.searchArtistByName("Arco Iris", 10);
     //         sse.stats();
     System.out.println("-----------Echonest Trace---------------");
     EchoNestAPI echoNest = new EchoNestAPI(APIKey);
     //echoNest.setTraceSends(true);
     Params p = new Params();
     p.add("name", medium.getArtist());
     p.add("results", 1);
     List<com.echonest.api.v4.Artist> artists = echoNest.searchArtists(p);
     for (com.echonest.api.v4.Artist artist : artists) {
     System.out.println();
     }

     System.out.println("---------Echonest Trace End-------------");

     }*/
    private void checkDiscography() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        int recordQuantityMatches = 0;

        // Проверяем альбомы
        for (Record record : rymp.getLpRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем live albums
        for (Record record : rymp.getLiveRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем compilations
        for (Record record : rymp.getCompRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        for (Record record : rymp.getBootlegRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем va
        for (Record record : rymp.getVaRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        //Проверяем ep
        for (Record record : rymp.getEpRecords()) {

            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(rymp.getInputAlbumName())) {
                        recordQuantityMatches++;
                    }
                }
            }

            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        //Проверяем синглы
        for (Record record : rymp.getSingleRecords()) {

            // Проверяем, есть ли в имени записи на RYM разделитель " / "
            // если есть, разделяем строку и сравниваем ее с именем альбома
            // скорее всего перед нами сингл или EP
            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(rymp.getInputAlbumName())) {
                        recordQuantityMatches++;
                    }
                }
            }

            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

    }

    /**
     *
     * @return
     */
    public ObservableList<Issue> getIssueList() {
        return issueList;
    }

    /**
     *
     * @return
     */
    public SimpleListProperty<Issue> getIssueListProperty() {
        return issueListProperty;
    }

    /**
     *
     * @return
     */
    public StringProperty getMessageProperty() {
        return message;
    }

    /**
     *
     * @param message
     */
    public void setMessageProperty(StringProperty message) {
        this.message = message;
    }

    /**
     *
     * @return
     */
    public String getMessageValue() {
        return message.getValue();
    }

    /**
     *
     * @param message
     */
    public void setMessageValue(String message) {
        this.message.setValue(message);
    }

    /**
     *
     * @return
     */
    public RYMParser getRymp() {
        return rymp;
    }

    class TestTask extends Task {

        @Override
        protected Object call() throws Exception {

            return null;
        }
    }
    private static final Logger LOG = Logger.getLogger(ParseFactory.class.getName());

}
