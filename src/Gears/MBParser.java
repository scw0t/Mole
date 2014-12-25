package Gears;

import OutEntities.Medium;
import static java.lang.System.out;
import java.util.List;
import org.musicbrainz.MBWS2Exception;
import org.musicbrainz.controller.ReleaseGroup;
import org.musicbrainz.model.entity.ReleaseGroupWs2;
import org.musicbrainz.model.entity.ReleaseWs2;
import org.musicbrainz.model.searchresult.ReleaseGroupResultWs2;

public class MBParser {

    public MBParser() {
        
    }
    
    public void parse(Medium medium) throws MBWS2Exception {

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
                    if (clearified(match, albumName, artistName)) {
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

    private boolean clearified(ReleaseGroupResultWs2 match, String albumName, String artistName) {
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
    
}
