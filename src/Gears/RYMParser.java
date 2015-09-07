package Gears;

import Entities.Genre;
import Entities.Artist;
import Entities.Issue;
import Entities.Person;
import Entities.Record;
import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import javafx.beans.property.StringProperty;
import org.jsoup.Jsoup;
import static org.jsoup.Jsoup.connect;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RYMParser {

    private final String rymUrl = "http://rateyourmusic.com";
    private final String rymArtistUrl = "http://rateyourmusic.com/artist/";
    private final String rymAlbumUrl = "http://rateyourmusic.com/release/album/";
    private final String rymSingleUrl = "http://rateyourmusic.com/release/single/";
    private final String rymCompUrl = "http://rateyourmusic.com/release/comp/";
    private final String rymUnauthUrl = "http://rateyourmusic.com/release/unauth/";
    private final String rymEpUrl = "http://rateyourmusic.com/release/ep/";
    private final String rymVideoUrl = "http://rateyourmusic.com/release/video/";
    private final String rymVAUrl = "http://rateyourmusic.com/release/comp/various_artists_f2/";

    private StringProperty message;

    private String inputArtistName = "";
    private String inputAlbumName = "";

    private String rymArtistName = "";

    private String currentAlbumUrl = "";
    private String currentArtistUrl = "";
    private final String currentVAUrl = "";

    private String type = "";

    private ArrayList<Record> lpRecords;
    private ArrayList<Record> liveRecords;
    private ArrayList<Record> epRecords;
    private ArrayList<Record> singleRecords;
    private ArrayList<Record> appearsRecords;
    private ArrayList<Record> compRecords;
    private ArrayList<Record> vaRecords;
    private ArrayList<Record> bootlegRecords;
    private ArrayList<Record> videoRecords;

    private Artist currentArtist;
    private Record currentRecord;

    private int artistPageAttempts = 0;
    private int albumPagesAttempts = 0;
    
    private boolean artistInfoParsed = false;

    /**
     *
     */
    public RYMParser() {
    }
    
    private Document getArtistPage(String artistName) throws IOException {
        String link;
        
        if (currentRecord != null) {
            link = rymUrl + currentRecord.getArtist().getLink();
        } else {
            link = rymArtistUrl + validateUrl(artistName);
        }
        
        
        return connect(link)
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20000).get();
    }

    private Document getAlbumPage(String artistName, String albumTitle) throws IOException {
        String releaseTypeUrl = "";
        switch (type) {
            case "Album":
                releaseTypeUrl = rymAlbumUrl;
                break;
            case "EP":
                releaseTypeUrl = rymEpUrl;
                break;
            case "Single":
                releaseTypeUrl = rymSingleUrl;
                break;
            case "Compilation":
                releaseTypeUrl = rymCompUrl;
                break;
            case "Bootleg":
                releaseTypeUrl = rymUnauthUrl;
                break;
            default:
                releaseTypeUrl = rymAlbumUrl;
                break;
        }

        return connect(releaseTypeUrl + validateUrl(artistName) + "/" + validateUrl(albumTitle))
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20_000).get();
    }

    /*
     Разбираем страницу исполнителя
     */
    /**
     *
     * @param inputArtistName
     * @return
     */
    public boolean parseArtistInfo(String inputArtistName) {
        try {
            Document doc = getArtistPage(inputArtistName);
            System.out.println("Parsing: " + rymArtistUrl + validateUrl(inputArtistName));

            Element contentTable = doc.getElementsByClass("artist_info").first();

            rymArtistName = doc.getElementsByClass("artist_name_hdr").first().text();

            currentArtist = new Artist(rymArtistName);
            currentArtist.setLink(currentArtistUrl);

            if (contentTable != null) {
                System.out.println("-----------------------------------------------------");
                for (int i = 0; i < contentTable.select("td").size(); i++) {
                    Element subTable = contentTable.select("td").get(i);
                    Element subDiv = subTable.getElementsByClass("info_hdr").first();
                    if (subDiv != null) {
                        switch (subDiv.text()) {
                            case "Formed": {
                                System.out.println("-----Formed-----");
                                String cleanedString = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                currentArtist.setFormedDate(parseDate(cleanedString)[0]);
                                currentArtist.setCountry(parseDate(cleanedString)[1]);
                                break;
                            }
                            case "Born": {
                                System.out.println("-----Born-----");
                                String cleanedString = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                currentArtist.setFormedDate(parseDate(cleanedString)[0]);
                                currentArtist.setCountry(parseDate(cleanedString)[1]);
                                break;
                            }
                            case "Disbanded":
                                System.out.println("-----Disbanded-----");
                                String cleanedString = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                currentArtist.setDiedDate(parseDate(cleanedString)[0]);
                                break;
                            case "Died":
                                System.out.println("-----Died-----");
                                String cs = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                currentArtist.setDiedDate(parseDate(cs)[0]);
                                break;
                            case "Members":
                                System.out.println("-----Members-----");
                                String[] splitSubstrings = subTable.html().split(" <br /> <br />");
                                String membersString = "";
                                Document subdoc = Jsoup.parse(splitSubstrings[0]);
                                Element subMembers = subdoc.body();
                                membersString = subMembers.text().replace(subDiv.text() + " ", "");
                                currentArtist.setMembers(parseMembers(membersString));
                                break;
                            case "Member of":
                                System.out.println("-----Member of-----");
                                ArrayList<Artist> membersList = new ArrayList<>();
                                String memberOf[] = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "").split(", ");
                                for (int j = 0; j < memberOf.length; j++) {
                                    if (j == 0) {
                                        memberOf[j] = memberOf[j].replace(subDiv.text(), "");
                                    }
                                    membersList.add(new Artist(memberOf[j]));
                                }
                                currentArtist.setMemberOf(membersList);
                                break;
                            case "Related Artists":
                                System.out.println("-----Related Artists-----");
                                ArrayList<Artist> relatedList = new ArrayList<>();
                                String related[] = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "").split(", ");
                                for (int j = 0; j < related.length; j++) {
                                    if (j == 0) {
                                        related[j] = related[j].replace(subDiv.text(), "");
                                    }
                                    relatedList.add(new Artist(related[j]));
                                }
                                currentArtist.setRelated(relatedList);
                                break;
                            case "Also Known As":
                                System.out.println("-----Also Known As-----");
                                ArrayList<String> akaList = new ArrayList<>();
                                String aka[] = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "").split(", ");
                                for (int j = 0; j < aka.length; j++) {
                                    if (j == 0) {
                                        aka[j] = aka[j].replace(subDiv.text(), "");
                                    }
                                    akaList.add(aka[j]);
                                }
                                currentArtist.setAka(akaList);
                                break;
                            case "Genres":
                                System.out.println("-----Genres-----");
                                ArrayList<Genre> artistGenres = new ArrayList<>();
                                Elements genreElements = subTable.getElementsByClass("genre");
                                for (Element element : genreElements) {
                                    if (!element.text().equals("Rock")) {
                                        Genre genre = new Genre();
                                        genre.setName(element.text());
                                        genre.setLink(element.attr("href"));
                                        artistGenres.add(genre);
                                    }
                                }
                                currentArtist.setGenres(artistGenres);
                                break;
                        }
                    }
                }
                parseArtistDiscography(doc);
                artistInfoParsed = true;
            } else {
                System.out.println("На сайте произошли какие-то изменения. Check this out.");
            }

        } catch (IOException ex) {
            artistPageAttempts++;

            switch (artistPageAttempts) {
                case 1:
                    String fixedArtistName1 = inputArtistName.startsWith("The ") ? inputArtistName.replaceFirst("The ", "") : inputArtistName;
                    parseArtistInfo(fixedArtistName1);
                    break;
                case 2:
                    String fixedArtistName2 = !inputArtistName.startsWith("The ") ? "The " + inputArtistName : inputArtistName;
                    parseArtistInfo(fixedArtistName2);
                    break;
                case 3:
                    System.out.println("URL. Status=404");
                    artistInfoParsed = false;
                    break;
            }
        }
        return artistInfoParsed;
    }

    

    /**
     *
     * @return
     */
    public boolean parseAlbumInfo(String artistName, String albumTitle) {
        try {
            Document doc = getAlbumPage(artistName, albumTitle);

            String fixUrl = fixAlbumUrl(doc);

            message.setValue("Search album: " + albumTitle);

            if (!fixUrl.equals(doc.baseUri())) {
                currentAlbumUrl = fixUrl;
                doc = connect(currentAlbumUrl)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .timeout(20_000).get();
            }

            System.out.println("Parsing: " + currentAlbumUrl);

            currentRecord = new Record();
            currentRecord.setLink(currentAlbumUrl);
            Element titleElement = doc.getElementsByClass("album_title").first();
            if (titleElement != null) {
                currentRecord.setName(titleElement.text());
            }

            Element contentTable = doc.getElementsByClass("album_info").first();
            if (contentTable != null) {
                for (int i = 0; i < contentTable.select("tr").size(); i++) {
                    Element subTableHeader = contentTable.select("th").get(i);
                    if (subTableHeader != null) {
                        switch (subTableHeader.text()) {
                            case "Artist":
                                out.println("-----Artist-----");
                                Elements artistElements = contentTable.select("tr").get(i).getElementsByClass("artist");

                                if (artistElements.size() > 1) {
                                    ArrayList<Artist> subArtists = new ArrayList<>();
                                    artistElements.stream().map((element) -> {
                                        Artist subArtist = new Artist(element.text());
                                        subArtist.setLink(element.attr("href"));
                                        return subArtist;
                                    }).forEach((subArtist) -> {
                                        subArtists.add(subArtist);
                                    });
                                    currentRecord.setArtist(subArtists.get(i));
                                    currentRecord.setSubArtists(subArtists);
                                } else {
                                    Artist recArtist = new Artist(artistElements.first().text());
                                    recArtist.setLink(artistElements.first().attr("href"));
                                    currentRecord.setArtist(recArtist);
                                }

                                rymArtistName = contentTable.select("tr").get(i).getElementsByClass("artist").first()
                                        .attr("href").replaceFirst("/artist/", "");
                                break;

                            case "Type":
                                currentRecord.setType(contentTable.select("tr").get(i).select("td").text());
                                break;

                            case "Released":
                                currentRecord.setYearReleased(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                        .split(" ")[2]);
                                break;

                            case "Recorded":
                                currentRecord.setYearRecorded(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                        .split(" ")[2]);
                                break;

                            case "Genres":
                                ArrayList<Genre> albumGenres = new ArrayList<>();
                                Element element = contentTable.select("tr").get(i).
                                        getElementsByClass("release_pri_genres").first();
                                Elements genresElements = element.getElementsByClass("genre");
                                genresElements.stream().filter((genreElement) -> (!genreElement.text().equals("Rock"))).map((genreElement) -> {
                                    Genre genre = new Genre();
                                    genre.setName(genreElement.text());
                                    genre.setLink(genreElement.attr("href"));
                                    return genre;
                                }).forEach((genre) -> {
                                    albumGenres.add(genre);
                                });
                                currentRecord.setGenres(albumGenres);
                                break;
                        }
                    }
                }
                currentRecord.setIssues(parseIssuesInfo(doc, currentRecord));
                currentRecord.printIssues();
                artistInfoParsed = true;
            } else {
                message.setValue("Unable to parse album " + inputAlbumName);
            }
        } catch (IOException ex) {
            albumPagesAttempts++;
            //String RYMArtistNameLink;

            switch (albumPagesAttempts) {
                case 1:
                    String fixedArtistNameWithoutPrefix_1 = artistName.startsWith("The ") ? artistName.replaceFirst("The ", "") : artistName;
                    String fixedAlbumNameWithoutPrefix_1 = albumTitle.startsWith("The ") ? albumTitle.replaceFirst("The ", "") : albumTitle;
                    message.setValue("Search case: " + fixedArtistNameWithoutPrefix_1 + " - " + fixedAlbumNameWithoutPrefix_1);
                    boolean p1 = parseAlbumInfo(fixedArtistNameWithoutPrefix_1, fixedAlbumNameWithoutPrefix_1);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p1;
                    }
                    break;
                case 2:
                    String fixedArtistNameWithoutPrefix_2 = artistName.startsWith("The ") ? artistName.replaceFirst("The ", "") : artistName;
                    String fixedAlbumNameWithPrefix_2 = !albumTitle.startsWith("The ") ? "The " + albumTitle : albumTitle;
                    message.setValue("Search case: " + fixedArtistNameWithoutPrefix_2 + " - " + fixedAlbumNameWithPrefix_2);
                    boolean p2 = parseAlbumInfo(fixedArtistNameWithoutPrefix_2, fixedAlbumNameWithPrefix_2);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p2;
                    }
                    break;
                case 3:
                    String fixedArtistNameWithPrefix_3 = !artistName.startsWith("The ") ? "The " + artistName : artistName;
                    String fixedAlbumNameWithoutPrefix_3 = albumTitle.startsWith("The ") ? albumTitle.replaceFirst("The ", "") : albumTitle;
                    message.setValue("Search case: " + fixedArtistNameWithPrefix_3 + " - " + fixedAlbumNameWithoutPrefix_3);
                    boolean p3 = parseAlbumInfo(fixedArtistNameWithPrefix_3, fixedAlbumNameWithoutPrefix_3);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p3;
                    }
                    break;
                case 4:
                    String fixedArtistNameWithPrefix_4 = !artistName.startsWith("The ") ? "The " + artistName : artistName;
                    String fixedAlbumNameWithPrefix_4 = !albumTitle.startsWith("The ") ? "The " + albumTitle : albumTitle;
                    message.setValue("Search case: " + fixedArtistNameWithPrefix_4 + " - " + fixedAlbumNameWithPrefix_4);
                    boolean p4 = parseAlbumInfo(fixedArtistNameWithPrefix_4, fixedAlbumNameWithPrefix_4);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p4;
                    }
                    break;
                case 5:
                    message.setValue("Search case: " + inputArtistName + "_f1");
                    //parseAlbumInfo(inputArtistName + "_f1", inputAlbumName);
                    setCurrentArtistUrl(rymArtistUrl + validateUrl(inputArtistName + "_f1"));
                    //boolean p5 = parseArtistInfo(inputArtistName + "_f1");
                    boolean p5 = parseAlbumInfo(inputArtistName + "_f1", inputAlbumName);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p5;
                    }
                    break;
                case 6:
                    message.setValue("Search case: " + inputArtistName + "_f2");
                    boolean p6 = parseAlbumInfo(inputArtistName + "_f2", inputAlbumName);
                    if (!artistInfoParsed) {
                        artistInfoParsed = p6;
                    }
                    break;
                default:
                    artistInfoParsed = false;
                    break;
            }

            /*while (albumPagesAttempts < 2) {

             out.println("count: " + albumPagesAttempts);

             if (albumPagesAttempts == 0) {
             message.setValue("Deep search... " + getInputArtistName());
             Pattern p = compile("The ", CASE_INSENSITIVE);
             Matcher matcher = p.matcher(getInputArtistName());
             if (matcher.find()) {
             RYMArtistNameLink = validateUrl(getInputArtistName().toLowerCase().replaceFirst("the ", ""));
             } else {
             RYMArtistNameLink = validateUrl("The " + getInputArtistName());
             }

             } else {
             message.setValue("Deep search... " + getInputArtistName() + ". Iteration " + albumPagesAttempts);
             RYMArtistNameLink = validateUrl(getInputArtistName()) + "_f" + albumPagesAttempts;
             }
             setCurrentAlbumUrl(rymAlbumUrl + RYMArtistNameLink + "/" + validateUrl(getInputAlbumName()));

             albumPagesAttempts++;

             if (parseAlbumInfo()) {
             parsed = true;
             break;
             } else {
             parsed = false;
             break;
             }
             }*/
        }

        return artistInfoParsed;
    }

    /**
     *
     * @param doc
     * @param record
     * @return
     */
    public ArrayList<Issue> parseIssuesInfo(Document doc, Record record) {
        message.setValue("Parsing issues...");
        Element issuesTable = doc.getElementsByClass("issues").first();
        Elements issuesElements = issuesTable.getElementsByClass("issue_info");

        ArrayList<Issue> issues = new ArrayList<>();

        if (issuesElements != null) {
            int issueIndex = 0;

            if (issuesElements.first().hasClass("issue_info")
                    && issuesElements.first().hasClass("release_view")) {
                issueIndex = 1;
            }

            for (int i = issueIndex; i < issuesElements.size(); i++) {
                message.setValue("Parsing issues...");
                Issue issue = new Issue();
                if (!issuesElements.get(i).getElementsByClass("sametitle").isEmpty()) {
                    issue.setLink(issuesElements.get(i).getElementsByClass("sametitle").first().attr("href"));
                }

                if (!issuesElements.get(i).getElementsByClass("issue_title").isEmpty()) {
                    issue.setLink(issuesElements.get(i).getElementsByClass("issue_title").first().
                            getElementsByTag("a").attr("href"));
                }

                if (!issuesElements.get(i).getElementsByClass("sametitle").isEmpty()) {
                    issue.setIssueTitle(issuesElements.get(i).getElementsByClass("sametitle").first().text());
                }

                if (!issuesElements.get(i).getElementsByClass("issue_title").isEmpty()) {
                    issue.setIssueTitle(issuesElements.get(i).getElementsByClass("issue_title").first().
                            getElementsByTag("a").text());
                }

                if (!issuesElements.get(i).getElementsByClass("attribute").isEmpty()) {
                    issue.setIssueAttributes(issuesElements.get(i).getElementsByClass("attribute").first().text());
                }

                if (!issuesElements.get(i).getElementsByClass("issue_year").isEmpty()) {
                    issue.setIssueYear(issuesElements.get(i).getElementsByClass("issue_year").first().text().replaceAll(" ", ""));
                }

                if (!issuesElements.get(i).getElementsByClass("issue_formats").isEmpty()) {
                    issue.setIssueFormats(issuesElements.get(i).getElementsByClass("issue_formats").first().text());
                }

                if (!issuesElements.get(i).getElementsByClass("label").isEmpty()) {
                    issue.setLabelLink(issuesElements.get(i).getElementsByClass("label").first().attr("href"));
                }

                if (!issuesElements.get(i).getElementsByClass("flag").isEmpty()) {
                    issue.setIssueCountries(issuesElements.get(i).getElementsByClass("flag").first().attr("title"));
                }

                if (issuesElements.get(i).getElementsByClass("issue_label") != null) {
                    String[] labelInfo = issuesElements.get(i).getElementsByClass("issue_label").first().text().replaceAll("n/a / ", "").split(" / ");
                    if (labelInfo.length != 0) {
                        if (labelInfo.length > 1) {
                            if (labelInfo[0] != null) {
                                issue.setIssueLabel(labelInfo[0]);
                            }

                            if (labelInfo[1] != null) {
                                issue.setCatNumber(labelInfo[1].replaceAll(" / ", ", "));
                            }
                        } else {
                            if (labelInfo[0] != null) {
                                issue.setCatNumber(labelInfo[0]);
                                issue.setIssueLabel("Unknown");
                            }
                        }

                    }
                }

                if (!issuesElements.get(i).getElementsByClass("primary_indicator").isEmpty()) {
                    issue.setIsPrimary(true);
                } else {
                    issue.setIsPrimary(false);
                }
                issues.add(issue);
            }
        } else {
        }
        return issues;
    }

    private String fixAlbumUrl(Document doc) {
        String url = "";
        Element issuesTable = doc.getElementsByClass("issues").first();
        Elements issuesElements = issuesTable.getElementsByClass("issue_info");

        if (issuesElements != null) {
            int issueIndex = 0;

            if (issuesElements.first().hasClass("issue_info")
                    && issuesElements.first().hasClass("release_view")) {
                issueIndex = 1;
            }

            if (!issuesElements.get(issueIndex).getElementsByClass("sametitle").isEmpty()) {
                url = issuesElements.get(issueIndex).getElementsByClass("sametitle").first().attr("href");
            }
            if (url.isEmpty()) {
                if (!issuesElements.get(issueIndex).getElementsByClass("issue_title").isEmpty()) {
                    url = issuesElements.get(issueIndex).getElementsByClass("issue_title").first().
                            getElementsByTag("a").attr("href");
                }
            }
        } else {
        }

        if (!url.isEmpty()) {
            return rymUrl + url;
        } else {
            return url;
        }
    }

    /**
     *
     * @return
     */
    public boolean parseVAInfo() {
        boolean parsed = false;
        try {
            setCurrentAlbumUrl(rymVAUrl + validateUrl(inputAlbumName));

            Document doc = connect(currentAlbumUrl)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20_000).get();
            System.out.println("Parsing: " + currentAlbumUrl);

            setCurrentRecord(new Record());

            Element tracklistTable = doc.getElementsByClass("tracklisting").first();

            if (tracklistTable != null) {
                ArrayList<Artist> subArtists = new ArrayList<>();
                Elements tracklist = tracklistTable.getElementsByClass("track");
                tracklist.stream().map((track) -> track.getElementsByClass("artist").first()).map((artistElement) -> {
                    Artist subArtist = new Artist(artistElement.text());
                    subArtist.setLink(artistElement.attr("href"));
                    return subArtist;
                }).forEach((subArtist) -> {
                    subArtists.add(subArtist);
                });
                currentRecord.setSubArtists(subArtists);
            }

            Element contentTable = doc.getElementsByClass("album_info").first();

            if (contentTable != null) {
                if (currentRecord == null) {
                    setCurrentRecord(new Record());
                }
                currentRecord.setArtist(new Artist("VA"));
                currentRecord.setType("Compilation");

                for (int i = 0; i < contentTable.select("tr").size(); i++) {
                    Element subTableHeader = contentTable.select("th").get(i);
                    if (subTableHeader != null) {
                        if (subTableHeader.text().equals("Released")) {
                            currentRecord.setYearReleased(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Recorded")) {
                            currentRecord.setYearRecorded(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Genres")) {
                            ArrayList<Genre> albumGenres = new ArrayList<>();
                            Element element = contentTable.select("tr").get(i).
                                    getElementsByClass("release_pri_genres").first();
                            Elements genresElements = element.getElementsByClass("genre");
                            genresElements.stream().map((genreElement) -> {
                                Genre genre = new Genre();
                                genre.setName(genreElement.text());
                                genre.setLink(genreElement.attr("href"));
                                return genre;
                            }).forEach((genre) -> {
                                albumGenres.add(genre);
                            });
                            currentRecord.setGenres(albumGenres);
                        }
                    }
                }
                parsed = true;
            } else {
            }
        } catch (IOException ex) {
        }

        return parsed;
    }

    /**
     *
     * @param name
     * @return
     */
    public boolean parseArtistDiscography(String name) {
        boolean parsed;
        try {
            message.setValue("Parsing discography...");
            Document doc = connect(rymArtistUrl + validateUrl(name))
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20_000).get();
            Element discographyDiv = doc.getElementById("discography");
            Element albumsDiv = discographyDiv.getElementById("disco_type_s");
            out.println("-^-^-^-^- ALBUMS -^-^-^-^-");
            lpRecords = parseArtistRecordInfo(albumsDiv, "Album");
            out.println("-^-^-^-^- LIVES -^-^-^-^-");
            Element liveAlbumsDiv = discographyDiv.getElementById("disco_type_l");
            liveRecords = parseArtistRecordInfo(liveAlbumsDiv, "Live Album");
            out.println("-^-^-^-^- EP -^-^-^-^-");
            Element epsDiv = discographyDiv.getElementById("disco_type_e");
            epRecords = parseArtistRecordInfo(epsDiv, "EP");
            out.println("-^-^-^-^- SINGLES -^-^-^-^-");
            Element silglesDiv = discographyDiv.getElementById("disco_type_i");
            singleRecords = parseArtistRecordInfo(silglesDiv, "Single");
            out.println("-^-^-^-^- APPEARS ON -^-^-^-^-");
            Element appearsOnDiv = discographyDiv.getElementById("disco_type_a");
            appearsRecords = parseArtistRecordInfo(appearsOnDiv, "Appears on");
            out.println("-^-^-^-^- COMPILATIONS -^-^-^-^-");
            Element compilationDiv = discographyDiv.getElementById("disco_type_c");
            compRecords = parseArtistRecordInfo(compilationDiv, "Compilation");
            out.println("-^-^-^-^- VA -^-^-^-^-");
            Element vaDiv = discographyDiv.getElementById("disco_type_v");
            vaRecords = parseArtistRecordInfo(vaDiv, "VA");
            out.println("-^-^-^-^- BOOTLEGS -^-^-^-^-");
            Element bootlegDiv = discographyDiv.getElementById("disco_type_b");
            bootlegRecords = parseArtistRecordInfo(bootlegDiv, "Bootleg");
            Element videoDiv = discographyDiv.getElementById("disco_type_d");
            videoRecords = parseArtistRecordInfo(videoDiv, "Video");
            parsed = true;
        } catch (IOException ex) {
            parsed = false;
        }
        return parsed;
    }

    /**
     *
     * @param doc
     */
    public void parseArtistDiscography(Document doc) {
        System.out.println(doc.baseUri());
        message.setValue("Parsing discography...");
        Element discographyDiv = doc.getElementById("discography");
        Element albumsDiv = discographyDiv.getElementById("disco_type_s");
        out.println("-^-^-^-^- ALBUMS -^-^-^-^-");
        lpRecords = parseArtistRecordInfo(albumsDiv, "Album");
        out.println("-^-^-^-^- LIVES -^-^-^-^-");
        Element liveAlbumsDiv = discographyDiv.getElementById("disco_type_l");
        liveRecords = parseArtistRecordInfo(liveAlbumsDiv, "Live Album");
        out.println("-^-^-^-^- EP -^-^-^-^-");
        Element epsDiv = discographyDiv.getElementById("disco_type_e");
        epRecords = parseArtistRecordInfo(epsDiv, "EP");
        out.println("-^-^-^-^- SINGLES -^-^-^-^-");
        Element silglesDiv = discographyDiv.getElementById("disco_type_i");
        singleRecords = parseArtistRecordInfo(silglesDiv, "Single");
        out.println("-^-^-^-^- APPEARS ON -^-^-^-^-");
        Element appearsOnDiv = discographyDiv.getElementById("disco_type_a");
        appearsRecords = parseArtistRecordInfo(appearsOnDiv, "Appears on");
        out.println("-^-^-^-^- COMPILATIONS -^-^-^-^-");
        Element compilationDiv = discographyDiv.getElementById("disco_type_c");
        compRecords = parseArtistRecordInfo(compilationDiv, "Compilation");
        out.println("-^-^-^-^- VA -^-^-^-^-");
        Element vaDiv = discographyDiv.getElementById("disco_type_v");
        vaRecords = parseArtistRecordInfo(vaDiv, "VA");
        out.println("-^-^-^-^- BOOTLEGS -^-^-^-^-");
        Element bootlegDiv = discographyDiv.getElementById("disco_type_b");
        bootlegRecords = parseArtistRecordInfo(bootlegDiv, "Bootleg");
        Element videoDiv = discographyDiv.getElementById("disco_type_d");
        videoRecords = parseArtistRecordInfo(videoDiv, "Video");
    }

    private ArrayList<Record> parseArtistRecordInfo(Element el, String recordType) {

        ArrayList<Record> records = new ArrayList<>();

        if (el != null) {
            Elements subDivs = el.getElementsByClass("disco_release");
            if (subDivs != null) {
                subDivs.stream().map((element) -> {
                    Record record = new Record(recordType);
                    Element discoInfoElement = element.getElementsByClass("disco_info").first();
                    if (discoInfoElement != null) {
                        Element mainlineElement = discoInfoElement.getElementsByClass("disco_mainline").first();
                        if (mainlineElement != null) {
                            record.setName(mainlineElement.getElementsByClass("album").first().text());
                            record.setLink(rymUrl + mainlineElement.getElementsByClass("album").first().attr("href"));
                        }
                        Element sublineElement = discoInfoElement.getElementsByClass("disco_subline").first();
                        if (sublineElement != null) {
                            Element yearSpan = discoInfoElement.getElementsByClass("disco_year_ymd").first();
                            if (yearSpan == null) {
                                yearSpan = discoInfoElement.getElementsByClass("disco_year_ym").first();
                            }
                            if (yearSpan == null) {
                                yearSpan = discoInfoElement.getElementsByClass("disco_year_y").first();
                            }
                            if (yearSpan != null) {
                                record.setYearReleased(yearSpan.text());
                            }
                            Elements subArtists = sublineElement.getElementsByClass("disco_sub_artist");
                            if (!subArtists.isEmpty()) {
                                ArrayList<Artist> subArtistsList = new ArrayList<>();
                                subArtists.stream().map((subArtistSpan) -> {
                                    Artist artist = new Artist(subArtistSpan.text());
                                    artist.setLink(subArtistSpan.absUrl("href"));
                                    return artist;
                                }).forEach((artist) -> {
                                    subArtistsList.add(artist);
                                });
                                record.setSubArtists(subArtistsList);
                            }
                        }
                    }
                    return record;
                }).forEach((record) -> {
                    records.add(record);
                    //record.printRecordContent();
                });
            }
        }
        return records;
    }

    /**
     *
     * @param membersString
     * @return
     */
    public ArrayList<Person> parseMembers(String membersString) {
        ArrayList<Person> persons = new ArrayList<>();
        String periodPattern = "\\d{4}-\\d{2}";
        Pattern p = compile(periodPattern, CASE_INSENSITIVE);

        String[] membersArray = membersString.split("\\), ");
        if (membersArray.length == 1) {
            String[] membersArray2 = membersArray[0].split(", ");
            for (String string : membersArray2) {
                Person person = new Person(string);
                persons.add(person);
            }
        } else {
            for (String string : membersArray) {
                string = string.replaceAll("\\)", "");
                String period = "";
                Matcher m = p.matcher(string);
                while (m.find()) {
                    period = m.group();
                    string = string.replace(period, "");
                }

                String subStr[] = string.split("\\(");

                Person person = new Person(string.split("\\(")[0].trim());
                ArrayList<String> instruments = new ArrayList<>();

                if (subStr != null && string.split("\\(").length > 1) {
                    String instrs = string.split("\\(")[1].trim();

                    if (instrs.endsWith(",")) {
                        instrs = instrs.substring(0, instrs.length() - 1);
                    }

                    String[] strArr = instrs.split(", ");

                    if (strArr.length == 0) {
                        instruments.add(instrs);
                    } else {
                        instruments.addAll(Arrays.asList(instrs.split(", ")));
                    }
                    person.setInstruments(instruments);
                }
                persons.add(person);
            }
        }

        return persons;
    }

    /**
     *
     * @param RYMFormedString
     * @return
     */
    public String[] parseDate(String RYMFormedString) {
        String country = "";
        String date = "";
        String year = "";
        String monthNum = "";
        String month = "";

        String yearPattern = "\\d{4}";
        String monthNumPattern = "\\d{1,2}";
        String monthPattern = "January|February|March|April|May|June|July|August|September|October|November|December";

        Pattern p = compile(yearPattern, CASE_INSENSITIVE);
        Matcher m = p.matcher(RYMFormedString);

        while (m.find()) {
            year = m.group();
            RYMFormedString = RYMFormedString.replace(year, "");
        }

        p = compile(monthNumPattern, CASE_INSENSITIVE);
        m = p.matcher(RYMFormedString);
        while (m.find()) {
            monthNum = m.group();
            RYMFormedString = RYMFormedString.replace(monthNum, "");
        }

        p = compile(monthPattern, CASE_INSENSITIVE);
        m = p.matcher(RYMFormedString);
        while (m.find()) {
            month = m.group();
            RYMFormedString = RYMFormedString.replace(month, "");
        }

        if (!month.isEmpty()) {
            date += month + " ";
        } else {
            date += "null ";
        }
        if (!monthNum.isEmpty()) {
            date += monthNum + " ";
        } else {
            date += "null ";
        }
        if (!year.isEmpty()) {
            date += year;
        } else {
            date += "null";
        }

        if (RYMFormedString.startsWith(" ")) {
            RYMFormedString = RYMFormedString.replaceFirst(quote(" , "), "");
        }

        if (RYMFormedString.startsWith(",")) {
            RYMFormedString = RYMFormedString.replaceFirst(quote(", "), "");
        }

        if (RYMFormedString.startsWith(" ")) {
            RYMFormedString = RYMFormedString.replaceFirst(quote(" "), "");
        }

        country = RYMFormedString.split(", ")[RYMFormedString.split(", ").length - 1];

        String formed[] = new String[2];
        formed[0] = date;
        formed[1] = country;
        return formed;
    }

    /**
     *
     * @return
     */
    public String getArtistUrl() {
        return rymArtistUrl;
    }

    /**
     *
     * @return
     */
    public String getAlbumUrl() {
        return rymAlbumUrl;
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public boolean artistLinkExists(String name) throws IOException {
        Document doc = connect(rymArtistUrl + validateUrl(name))
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20_000).get();
        return doc != null;
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public boolean albumLinkExists(String name) throws IOException {
        Document doc = connect(rymAlbumUrl + validateUrl(name))
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20_000).get();
        return doc != null;
    }

    private String validateUrl(String url) {
        StringBuilder sb = new StringBuilder();
        char[] charArr = url.toLowerCase().toCharArray();
        for (int i = 0; i < charArr.length; i++) {
            if (charArr[i] == 'á') {
                sb.append("a");
            } else if (charArr[i] == 'à') {
                sb.append("a");
            } else if (charArr[i] == 'â') {
                sb.append("a");
            } else if (charArr[i] == 'ã') {
                sb.append("a");
            } else if (charArr[i] == 'ä') {
                sb.append("a");
            } else if (charArr[i] == 'å') {
                sb.append("a");
            } else if (charArr[i] == 'ā') {
                sb.append("a");
            } else if (charArr[i] == 'ă') {
                sb.append("a");
            } else if (charArr[i] == 'ą') {
                sb.append("a");
            } else if (charArr[i] == 'ȁ') {
                sb.append("a");
            } else if (charArr[i] == 'ȃ') {
                sb.append("a");
            } else if (charArr[i] == 'ß') {
                sb.append("b");
            } else if (charArr[i] == 'ć') {
                sb.append("c");
            } else if (charArr[i] == 'ĉ') {
                sb.append("c");
            } else if (charArr[i] == 'ċ') {
                sb.append("c");
            } else if (charArr[i] == 'č') {
                sb.append("c");
            } else if (charArr[i] == 'ç') {
                sb.append("c");
            } else if (charArr[i] == 'è') {
                sb.append("e");
            } else if (charArr[i] == 'é') {
                sb.append("e");
            } else if (charArr[i] == 'ê') {
                sb.append("e");
            } else if (charArr[i] == 'ë') {
                sb.append("e");
            } else if (charArr[i] == 'ē') {
                sb.append("e");
            } else if (charArr[i] == 'ĕ') {
                sb.append("e");
            } else if (charArr[i] == 'ė') {
                sb.append("e");
            } else if (charArr[i] == 'ę') {
                sb.append("e");
            } else if (charArr[i] == 'ě') {
                sb.append("e");
            } else if (charArr[i] == 'ȅ') {
                sb.append("e");
            } else if (charArr[i] == 'ȇ') {
                sb.append("e");
            } else if (charArr[i] == 'ĝ') {
                sb.append("g");
            } else if (charArr[i] == 'ğ') {
                sb.append("g");
            } else if (charArr[i] == 'ġ') {
                sb.append("g");
            } else if (charArr[i] == 'ģ') {
                sb.append("g");
            } else if (charArr[i] == 'ĥ') {
                sb.append("h");
            } else if (charArr[i] == 'ħ') {
                sb.append("h");
            } else if (charArr[i] == 'ì') {
                sb.append("i");
            } else if (charArr[i] == 'í') {
                sb.append("i");
            } else if (charArr[i] == 'î') {
                sb.append("i");
            } else if (charArr[i] == 'ï') {
                sb.append("i");
            } else if (charArr[i] == 'ĩ') {
                sb.append("i");
            } else if (charArr[i] == 'ȉ') {
                sb.append("i");
            } else if (charArr[i] == 'ȉ') {
                sb.append("i");
            } else if (charArr[i] == 'ȋ') {
                sb.append("i");
            } else if (charArr[i] == 'ĭ') {
                sb.append("i");
            } else if (charArr[i] == 'į') {
                sb.append("i");
            } else if (charArr[i] == 'ı') {
                sb.append("i");
            } else if (charArr[i] == 'ĵ') {
                sb.append("j");
            } else if (charArr[i] == 'ķ') {
                sb.append("k");
            } else if (charArr[i] == 'ĺ') {
                sb.append("l");
            } else if (charArr[i] == 'ļ') {
                sb.append("l");
            } else if (charArr[i] == 'ľ') {
                sb.append("l");
            } else if (charArr[i] == 'ŀ') {
                sb.append("l");
            } else if (charArr[i] == 'ł') {
                sb.append("l");
            } else if (charArr[i] == 'ð') {
                sb.append("d");
            } else if (charArr[i] == 'ď') {
                sb.append("d");
            } else if (charArr[i] == 'đ') {
                sb.append("d");
            } else if (charArr[i] == 'þ') {
                sb.append("d");
            } else if (charArr[i] == 'æ') {
                sb.append("ae");
            } else if (charArr[i] == 'ñ') {
                sb.append("n");
            } else if (charArr[i] == 'ń') {
                sb.append("n");
            } else if (charArr[i] == 'ņ') {
                sb.append("n");
            } else if (charArr[i] == 'ň') {
                sb.append("n");
            } else if (charArr[i] == 'ŉ') {
                sb.append("n");
            } else if (charArr[i] == 'ò') {
                sb.append("o");
            } else if (charArr[i] == 'ó') {
                sb.append("o");
            } else if (charArr[i] == 'ô') {
                sb.append("o");
            } else if (charArr[i] == 'õ') {
                sb.append("o");
            } else if (charArr[i] == 'ö') {
                sb.append("o");
            } else if (charArr[i] == 'ō') {
                sb.append("o");
            } else if (charArr[i] == 'ŏ') {
                sb.append("o");
            } else if (charArr[i] == 'ő') {
                sb.append("o");
            } else if (charArr[i] == 'ǒ') {
                sb.append("o");
            } else if (charArr[i] == 'ǫ') {
                sb.append("o");
            } else if (charArr[i] == 'ǭ') {
                sb.append("o");
            } else if (charArr[i] == 'ȍ') {
                sb.append("o");
            } else if (charArr[i] == 'ȏ') {
                sb.append("o");
            } else if (charArr[i] == 'ø') {
                sb.append("o");
            } else if (charArr[i] == 'ǿ') {
                sb.append("o");
            } else if (charArr[i] == 'ŕ') {
                sb.append("r");
            } else if (charArr[i] == 'ŗ') {
                sb.append("r");
            } else if (charArr[i] == 'ř') {
                sb.append("r");
            } else if (charArr[i] == 'ȑ') {
                sb.append("r");
            } else if (charArr[i] == 'ȓ') {
                sb.append("r");
            } else if (charArr[i] == 'ś') {
                sb.append("s");
            } else if (charArr[i] == 'ŝ') {
                sb.append("s");
            } else if (charArr[i] == 'ş') {
                sb.append("s");
            } else if (charArr[i] == 'š') {
                sb.append("s");
            } else if (charArr[i] == 'ţ') {
                sb.append("t");
            } else if (charArr[i] == 'ť') {
                sb.append("t");
            } else if (charArr[i] == 'ŧ') {
                sb.append("t");
            } else if (charArr[i] == 'ù') {
                sb.append("u");
            } else if (charArr[i] == 'ú') {
                sb.append("u");
            } else if (charArr[i] == 'û') {
                sb.append("u");
            } else if (charArr[i] == 'ü') {
                sb.append("u");
            } else if (charArr[i] == 'ũ') {
                sb.append("u");
            } else if (charArr[i] == 'ȕ') {
                sb.append("u");
            } else if (charArr[i] == 'ȗ') {
                sb.append("u");
            } else if (charArr[i] == 'ū') {
                sb.append("u");
            } else if (charArr[i] == 'ŭ') {
                sb.append("u");
            } else if (charArr[i] == 'ů') {
                sb.append("u");
            } else if (charArr[i] == 'ų') {
                sb.append("u");
            } else if (charArr[i] == 'ý') {
                sb.append("y");
            } else if (charArr[i] == 'ÿ') {
                sb.append("y");
            } else if (charArr[i] == 'ź') {
                sb.append("y");
            } else if (charArr[i] == 'ż') {
                sb.append("y");
            } else if (charArr[i] == 'ž') {
                sb.append("y");
            } else if (charArr[i] == '½') {
                sb.append("_");
            } else if (charArr[i] == '¬') {
                sb.append("_");
            } else if (charArr[i] == '#') {
                sb.append("_");
            } else if (charArr[i] == '{') {
                sb.append("_");
            } else if (charArr[i] == '}') {
                sb.append("_");
            } else if (charArr[i] == ']') {
                sb.append("_");
            } else if (charArr[i] == '[') {
                sb.append("_");
            } else if (charArr[i] == ')') {
                sb.append("_");
            } else if (charArr[i] == '(') {
                sb.append("_");
            } else if (charArr[i] == '\'') {
                sb.append("");
            } else if (charArr[i] == '-') {
                sb.append("_");
            } else if (charArr[i] == '+') {
                sb.append("_");
            } else if (charArr[i] == '=') {
                sb.append("_");
            } else if (charArr[i] == '\\') {
                sb.append("_");
            } else if (charArr[i] == '/') {
                sb.append("_");
            } else if (charArr[i] == '?') {
                sb.append("_");
            } else if (charArr[i] == '!') {
                sb.append("_");
            } else if (charArr[i] == ';') {
                sb.append("_");
            } else if (charArr[i] == ':') {
                sb.append("_");
            } else if (charArr[i] == ',') {
                sb.append("_");
            } else if (charArr[i] == '.') {
                sb.append("_");
            } else if (charArr[i] == '^') {
                sb.append("_");
            } else if (charArr[i] == '%') {
                sb.append("_");
            } else if (charArr[i] == '$') {
                sb.append("_");
            } else if (charArr[i] == '№') {
                sb.append("_");
            } else if (charArr[i] == '<') {
                sb.append("_");
            } else if (charArr[i] == '>') {
                sb.append("_");
            } else if (charArr[i] == '~') {
                sb.append("_");
            } else if (charArr[i] == '`') {
                sb.append("_");
            } else if (charArr[i] == '*') {
                sb.append("_");
            } else if (charArr[i] == ' ') {
                sb.append("_");
            } else if (charArr[i] == '¡') {
                sb.append("_");
            } else if (charArr[i] == '"') {
                sb.append("");
            } else if (charArr[i] == '&') {
                sb.append("and");
            } else if (charArr[i] == 'ґ') {
                sb.append("");
            } else {
                sb.append(url.charAt(i));
            }
        }
        return sb.toString().toLowerCase();
    }

    /**
     *
     */
    public void initUrls() {

    }

    /**
     *
     * @return
     */
    public String getRymArtistName() {
        return rymArtistName;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getLpRecords() {
        return lpRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getLiveRecords() {
        return liveRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getEpRecords() {
        return epRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getSingleRecords() {
        return singleRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getAppearsRecords() {
        return appearsRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getCompRecords() {
        return compRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getVaRecords() {
        return vaRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getBootlegRecords() {
        return bootlegRecords;
    }

    /**
     *
     * @return
     */
    public ArrayList<Record> getVideoRecords() {
        return videoRecords;
    }

    /**
     *
     * @param currentAlbumUrl
     */
    public void setCurrentAlbumUrl(String currentAlbumUrl) {
        this.currentAlbumUrl = currentAlbumUrl;
    }

    /**
     *
     * @return
     */
    public Artist getCurrentArtist() {
        return currentArtist;
    }

    /**
     *
     * @return
     */
    public String getInputAlbumName() {
        return inputAlbumName;
    }

    /**
     *
     * @return
     */
    public String getInputArtistName() {
        return inputArtistName;
    }

    /**
     *
     * @param inputArtistName
     */
    public void setInputArtistNameAndInitUrl(String inputArtistName) {
        this.inputArtistName = inputArtistName;
        currentArtistUrl = rymArtistUrl + validateUrl(inputArtistName);
    }

    /**
     *
     * @param inputAlbumName
     */
    public void setInputAlbumNameAndInitUrl(String inputAlbumName) {
        this.inputAlbumName = inputAlbumName;
        currentAlbumUrl = rymAlbumUrl + validateUrl(inputArtistName) + "/" + validateUrl(inputAlbumName);
    }

    /**
     *
     * @return
     */
    public Record getCurrentRecord() {
        return currentRecord;
    }

    /**
     *
     * @param currentRecord
     */
    public void setCurrentRecord(Record currentRecord) {
        this.currentRecord = currentRecord;
    }

    /**
     *
     * @return
     */
    public String getCurrentArtistUrl() {
        return currentArtistUrl;
    }

    /**
     *
     * @param currentArtistUrl
     */
    public void setCurrentArtistUrl(String currentArtistUrl) {
        this.currentArtistUrl = currentArtistUrl;
    }

    /**
     *
     * @param message
     */
    public void setMessage(StringProperty message) {
        this.message = message;
    }
    private static final Logger LOG = Logger.getLogger(RYMParser.class.getName());

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
