package Gears;

import Entities.Genre;
import Entities.Artist;
import Entities.Issue;
import Entities.Person;
import Entities.Record;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RYMParser implements Runnable {

    private LogOutput logOutput;

    private final String initArtistUrl = "http://rateyourmusic.com/artist/";
    private final String initAlbumUrl = "http://rateyourmusic.com/release/album/";
    private final String initVAUrl = "http://rateyourmusic.com/release/comp/various_artists_f2/";
    private final String rymLink = "http://rateyourmusic.com";

    private int iterateCount; // кол-во итераций для посика нужного исполнителя

    private String audioArtistName = "";
    private String audioAlbumName = "";

    private String RYMArtistName = "";

    private String currentAlbumUrl = "";
    private String currentArtistUrl = "";
    private String currentVAUrl = "";

    private ArrayList<Record> albumRecords;
    private ArrayList<Record> liveRecords;
    private ArrayList<Record> epRecords;
    private ArrayList<Record> singleRecords;
    private ArrayList<Record> appearsRecords;
    private ArrayList<Record> compRecords;
    private ArrayList<Record> vaRecords;
    private ArrayList<Record> bootlegRecords;
    private ArrayList<Record> videoRecords;

    private Artist artist;
    private Record record;

    public RYMParser() {
        iterateCount = 0;
    }

    /*
     Разбираем страницу исполнителя
     name - имя исполнителя и формате "Artist Name"
     */
    public boolean parseArtistInfo(String name) {
        boolean parsed = false;
        try {
            Document doc = Jsoup.connect(initArtistUrl + validateUrl(name))
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20000).get();
            Element contentTable = doc.getElementsByClass("artist_info").first();

            String pattern = "\\d{4}-(\\d{2,4}|present)|\\d{4}";
            Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

            RYMArtistName = doc.getElementsByClass("artist_name_hdr").first().text();

            artist = new Artist(RYMArtistName);

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
                                getArtist().setFormedDate(parseDate(cleanedString)[0]);
                                getArtist().setCountry(parseDate(cleanedString)[1]);
                                break;
                            }
                            case "Born": {
                                System.out.println("-----Born-----");
                                String cleanedString = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                getArtist().setFormedDate(parseDate(cleanedString)[0]);
                                getArtist().setCountry(parseDate(cleanedString)[1]);
                                break;
                            }
                            case "Disbanded":
                                System.out.println("-----Disbanded-----");
                                String cleanedString = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                getArtist().setDiedDate(parseDate(cleanedString)[0]);
                                break;
                            case "Died":
                                System.out.println("-----Died-----");
                                String cs = subTable.text().replace(subDiv.text() + " ", "").replace(subDiv.text(), "");
                                getArtist().setDiedDate(parseDate(cs)[0]);
                                break;
                            case "Members":
                                System.out.println("-----Members-----");
                                String[] splitSubstrings = subTable.html().split(" <br /> <br />");
                                String membersString = "";
                                Document subdoc = Jsoup.parse(splitSubstrings[0]);
                                Element subMembers = subdoc.body();
                                membersString = subMembers.text().replace(subDiv.text() + " ", "");
                                getArtist().setMembers(parseMembers(membersString));
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
                                getArtist().setMemberOf(membersList);
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
                                getArtist().setRelated(relatedList);
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
                                getArtist().setAka(akaList);
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
                                getArtist().setGenres(artistGenres);
                                break;
                        }

                    }
                }
                parseArtistDiscography(doc);
                parsed = true;
            } else {
                System.out.println("На сайте произошли какие-то изменения. Check this out.");
            }

        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println("URL. Status=404");
            parsed = false;
        } 
        return parsed;
    }

    public boolean parseAlbumInfo() {

        boolean parsed = false;
        try {

            Document doc = Jsoup.connect(currentAlbumUrl)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20000).get();

            String fixUrl = fixAlbumUrl(doc);

            if (!fixUrl.equals(doc.baseUri())) {
                currentAlbumUrl = fixUrl;
                System.out.println(currentAlbumUrl);
                doc = Jsoup.connect(currentAlbumUrl)
                        .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .timeout(20000).get();
            }

            Element contentTable = doc.getElementsByClass("album_info").first();

            if (contentTable != null) {
                System.out.println("-----------------------------------------------------");
                setRecord(new Record());
                for (int i = 0; i < contentTable.select("tr").size(); i++) {
                    Element subTableHeader = contentTable.select("th").get(i);
                    if (subTableHeader != null) {
                        if (subTableHeader.text().equals("Artist")) {
                            System.out.println("-----Artist-----");
                            //artistName = contentTable.select("tr").get(i).getElementsByClass("artist").first().text();
                            Elements artistElements = contentTable.select("tr").get(i).getElementsByClass("artist");

                            if (artistElements.size() > 1) {
                                ArrayList<Artist> subArtists = new ArrayList<>();
                                for (Element element : artistElements) {
                                    Artist subArtist = new Artist(element.text());
                                    subArtist.setLink(element.attr("href"));
                                    subArtists.add(subArtist);
                                }
                                record.setArtist(subArtists.get(i));
                                record.setSubArtists(subArtists);
                            } else {
                                Artist recArtist = new Artist(artistElements.first().text());
                                recArtist.setLink(artistElements.first().attr("href"));
                                record.setArtist(recArtist);
                            }

                            RYMArtistName = contentTable.select("tr").get(i).getElementsByClass("artist").first()
                                    .attr("href").replaceFirst("/artist/", "");
                            System.out.println(RYMArtistName);
                        }

                        if (subTableHeader.text().equals("Type")) {
                            System.out.println("-----Type-----");
                            record.setType(contentTable.select("tr").get(i).select("td").text());
                        }

                        if (subTableHeader.text().equals("Released")) {
                            System.out.println("-----Released-----");
                            record.setYearReleased(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Recorded")) {
                            System.out.println("-----Recorded-----");
                            record.setYearRecorded(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Genres")) {
                            System.out.println("-----Genres-----");
                            ArrayList<Genre> albumGenres = new ArrayList<>();
                            Element element = contentTable.select("tr").get(i).
                                    getElementsByClass("release_pri_genres").first();
                            Elements genresElements = element.getElementsByClass("genre");
                            for (Element genreElement : genresElements) {
                                if (!genreElement.text().equals("Rock")) {
                                    Genre genre = new Genre();
                                    genre.setName(genreElement.text());
                                    genre.setLink(genreElement.attr("href"));
                                    albumGenres.add(genre);
                                }
                            }
                            record.setGenres(albumGenres);
                        }

                        if (parsed) {

                        }

                    }
                }
                record.setIssues(parseIssuesInfo(doc, record));
                record.printIssues();
                parsed = true;
            } else {
                System.out.println("На сайте произошли какие-то изменения. Check this out.");
            }
        } catch (IOException ex) {
            String RYMArtistNameLink;
            while (iterateCount < 5) {

                System.out.println("count: " + iterateCount);

                if (iterateCount == 0) {
                    Pattern p = Pattern.compile("The ", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = p.matcher(getAudioArtistName());
                    if (matcher.find()) {
                        RYMArtistNameLink = validateUrl(getAudioArtistName().toLowerCase().replaceFirst("the ", ""));
                        System.out.println(matcher.group(0)); //prints /{item}/
                    } else {
                        RYMArtistNameLink = validateUrl("The " + getAudioArtistName());
                    }

                } else {
                    RYMArtistNameLink = validateUrl(getAudioArtistName()) + "_f" + iterateCount;
                }
                setCurrentAlbumUrl(initAlbumUrl + RYMArtistNameLink + "/" + validateUrl(getAudioAlbumName()));
                System.out.println(currentAlbumUrl);

                iterateCount++;

                if (parseAlbumInfo()) {
                    parsed = true;
                    break;
                } else {
                    parsed = false;
                    break;
                }
            }
        }

        return parsed;
    }

    public ArrayList<Issue> parseIssuesInfo(Document doc, Record record) {
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
                Issue issue = new Issue();
                if (!issuesElements.get(i).getElementsByClass("sametitle").isEmpty()) {
                    issue.setLink(issuesElements.get(i).getElementsByClass("sametitle").first().attr("href"));
                }

                if (!issuesElements.get(i).getElementsByClass("issue_title").isEmpty()) {
                    issue.setLink(issuesElements.get(i).getElementsByClass("issue_title").first().
                            getElementsByTag("a").attr("href"));
                }

                if (!issuesElements.get(i).getElementsByClass("sametitle").isEmpty()) {
                    issue.setIssueName(issuesElements.get(i).getElementsByClass("sametitle").first().text());
                }

                if (!issuesElements.get(i).getElementsByClass("issue_title").isEmpty()) {
                    issue.setIssueName(issuesElements.get(i).getElementsByClass("issue_title").first().
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
            System.out.println("На сайте произошли какие-то изменения. Check this out.");
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
            System.out.println("CheckAlbumURL: На сайте произошли какие-то изменения. Check this out.");
        }

        if (!url.isEmpty()) {
            return rymLink + url;
        } else {
            return url;
        }
    }

    public boolean parseVAInfo() {

        boolean parsed = false;
        try {
            setCurrentAlbumUrl(initVAUrl + validateUrl(audioAlbumName));

            Document doc = Jsoup.connect(currentAlbumUrl)
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20000).get();

            setRecord(new Record());

            Element tracklistTable = doc.getElementsByClass("tracklisting").first();

            if (tracklistTable != null) {
                System.out.println("---------- Tracklist -------------");
                ArrayList<Artist> subArtists = new ArrayList<>();
                Elements tracklist = tracklistTable.getElementsByClass("track");
                for (Element track : tracklist) {
                    Element artistElement = track.getElementsByClass("artist").first();
                    Artist subArtist = new Artist(artistElement.text());
                    subArtist.setLink(artistElement.attr("href"));
                    subArtists.add(subArtist);
                }
                getRecord().setSubArtists(subArtists);
            }

            Element contentTable = doc.getElementsByClass("album_info").first();

            if (contentTable != null) {
                System.out.println("-----------------------------------------------------");
                if (getRecord() == null) {
                    setRecord(new Record());
                }
                getRecord().setArtist(new Artist("VA"));
                getRecord().setType("Compilation");

                for (int i = 0; i < contentTable.select("tr").size(); i++) {
                    Element subTableHeader = contentTable.select("th").get(i);
                    if (subTableHeader != null) {
                        if (subTableHeader.text().equals("Released")) {
                            System.out.println("-----Released-----");
                            getRecord().setYearReleased(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Recorded")) {
                            System.out.println("-----Recorded-----");
                            getRecord().setYearRecorded(parseDate(contentTable.select("tr").get(i).select("td").text())[0]
                                    .split(" ")[2]);
                        }

                        if (subTableHeader.text().equals("Genres")) {
                            System.out.println("-----Genres-----");
                            ArrayList<Genre> albumGenres = new ArrayList<>();
                            Element element = contentTable.select("tr").get(i).
                                    getElementsByClass("release_pri_genres").first();
                            Elements genresElements = element.getElementsByClass("genre");
                            for (Element genreElement : genresElements) {
                                Genre genre = new Genre();
                                genre.setName(genreElement.text());
                                genre.setLink(genreElement.attr("href"));
                                albumGenres.add(genre);
                            }
                            getRecord().setGenres(albumGenres);
                        }
                    }
                }
                parsed = true;
            } else {
                System.out.println("На сайте произошли какие-то изменения. Check this out.");
            }
        } catch (IOException ex) {
            System.out.println("Не удалось получить информацию о VA релизе");
        }

        return parsed;
    }

    public boolean parseArtistDiscography(String name) {
        boolean parsed;
        try {
            Document doc = Jsoup.connect(initArtistUrl + validateUrl(name))
                    .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                    .timeout(20000).get();
            Element discographyDiv = doc.getElementById("discography");
            Element albumsDiv = discographyDiv.getElementById("disco_type_s");
            System.out.println("-^-^-^-^- ALBUMS -^-^-^-^-");
            albumRecords = parseArtistRecordInfo(albumsDiv, "Album");
            System.out.println("-^-^-^-^- LIVES -^-^-^-^-");
            Element liveAlbumsDiv = discographyDiv.getElementById("disco_type_l");
            liveRecords = parseArtistRecordInfo(liveAlbumsDiv, "Live Album");
            System.out.println("-^-^-^-^- EP -^-^-^-^-");
            Element epsDiv = discographyDiv.getElementById("disco_type_e");
            epRecords = parseArtistRecordInfo(epsDiv, "EP");
            System.out.println("-^-^-^-^- SINGLES -^-^-^-^-");
            Element silglesDiv = discographyDiv.getElementById("disco_type_i");
            singleRecords = parseArtistRecordInfo(silglesDiv, "Single");
            System.out.println("-^-^-^-^- APPEARS ON -^-^-^-^-");
            Element appearsOnDiv = discographyDiv.getElementById("disco_type_a");
            appearsRecords = parseArtistRecordInfo(appearsOnDiv, "Appears on");
            System.out.println("-^-^-^-^- COMPILATIONS -^-^-^-^-");
            Element compilationDiv = discographyDiv.getElementById("disco_type_c");
            compRecords = parseArtistRecordInfo(compilationDiv, "Compilation");
            System.out.println("-^-^-^-^- VA -^-^-^-^-");
            Element vaDiv = discographyDiv.getElementById("disco_type_v");
            vaRecords = parseArtistRecordInfo(vaDiv, "VA");
            System.out.println("-^-^-^-^- BOOTLEGS -^-^-^-^-");
            Element bootlegDiv = discographyDiv.getElementById("disco_type_b");
            bootlegRecords = parseArtistRecordInfo(bootlegDiv, "Bootleg");
            System.out.println("-^-^-^-^- VIDEO -^-^-^-^-");
            Element videoDiv = discographyDiv.getElementById("disco_type_d");
            videoRecords = parseArtistRecordInfo(videoDiv, "Video");
            parsed = true;
        } catch (IOException ex) {
            parsed = false;
            System.out.println("artist link doesn't exists");
        }
        return parsed;
    }

    public void parseArtistDiscography(Document doc) {
        Element discographyDiv = doc.getElementById("discography");
        Element albumsDiv = discographyDiv.getElementById("disco_type_s");
        System.out.println("-^-^-^-^- ALBUMS -^-^-^-^-");
        albumRecords = parseArtistRecordInfo(albumsDiv, "Album");
        System.out.println("-^-^-^-^- LIVES -^-^-^-^-");
        Element liveAlbumsDiv = discographyDiv.getElementById("disco_type_l");
        liveRecords = parseArtistRecordInfo(liveAlbumsDiv, "Live Album");
        System.out.println("-^-^-^-^- EP -^-^-^-^-");
        Element epsDiv = discographyDiv.getElementById("disco_type_e");
        epRecords = parseArtistRecordInfo(epsDiv, "EP");
        System.out.println("-^-^-^-^- SINGLES -^-^-^-^-");
        Element silglesDiv = discographyDiv.getElementById("disco_type_i");
        singleRecords = parseArtistRecordInfo(silglesDiv, "Single");
        System.out.println("-^-^-^-^- APPEARS ON -^-^-^-^-");
        Element appearsOnDiv = discographyDiv.getElementById("disco_type_a");
        appearsRecords = parseArtistRecordInfo(appearsOnDiv, "Appears on");
        System.out.println("-^-^-^-^- COMPILATIONS -^-^-^-^-");
        Element compilationDiv = discographyDiv.getElementById("disco_type_c");
        compRecords = parseArtistRecordInfo(compilationDiv, "Compilation");
        System.out.println("-^-^-^-^- VA -^-^-^-^-");
        Element vaDiv = discographyDiv.getElementById("disco_type_v");
        vaRecords = parseArtistRecordInfo(vaDiv, "VA");
        System.out.println("-^-^-^-^- BOOTLEGS -^-^-^-^-");
        Element bootlegDiv = discographyDiv.getElementById("disco_type_b");
        bootlegRecords = parseArtistRecordInfo(bootlegDiv, "Bootleg");
        System.out.println("-^-^-^-^- VIDEO -^-^-^-^-");
        Element videoDiv = discographyDiv.getElementById("disco_type_d");
        videoRecords = parseArtistRecordInfo(videoDiv, "Video");
    }

    private ArrayList<Record> parseArtistRecordInfo(Element el, String recordType) {

        ArrayList<Record> records = new ArrayList<>();

        if (el != null) {
            Elements subDivs = el.getElementsByClass("disco_release");
            if (subDivs != null) {
                for (Element element : subDivs) {
                    Record record = new Record(recordType);
                    Element discoInfoElement = element.getElementsByClass("disco_info").first();
                    if (discoInfoElement != null) {
                        Element mainlineElement = discoInfoElement.getElementsByClass("disco_mainline").first();
                        if (mainlineElement != null) {
                            record.setName(mainlineElement.getElementsByClass("album").first().text());
                            record.setLink(rymLink + mainlineElement.getElementsByClass("album").first().attr("href"));
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
                                for (Element subArtistSpan : subArtists) {
                                    Artist artist = new Artist(subArtistSpan.text());
                                    artist.setLink(subArtistSpan.absUrl("href"));
                                    subArtistsList.add(artist);
                                }
                                record.setSubArtists(subArtistsList);
                            }
                        }
                    }
                    records.add(record);
                    //record.printRecordContent();
                }
            }
        }
        return records;
    }

    private String getReleaseViewLink(Document doc) {
        String releaseLink = "";

        Element content = doc.getElementsByClass("issues").first();

        if (content != null) {
            Element linkElement = content.getElementsByClass("issue_info").first().getElementsByAttribute("href").first();
            if (linkElement != null) {
                releaseLink = linkElement.attr("href");
            } else {
                releaseLink = "";
            }

        }

        return releaseLink;
    }

    public ArrayList<Person> parseMembers(String membersString) {
        ArrayList<Person> persons = new ArrayList<>();
        String periodPattern = "\\d{4}-\\d{2}";
        Pattern p = Pattern.compile(periodPattern, Pattern.CASE_INSENSITIVE);

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
                        for (String instr : instrs.split(", ")) {
                            instruments.add(instr);
                        }
                    }
                    person.setInstruments(instruments);
                }
                persons.add(person);
            }
        }

        return persons;

    }

    public String[] parseDate(String RYMFormedString) {
        String country = "";
        String date = "";
        String year = "";
        String monthNum = "";
        String month = "";

        String yearPattern = "\\d{4}";
        String monthNumPattern = "\\d{1,2}";
        String monthPattern = "January|February|March|April|May|June|July|August|September|October|November|December";

        Pattern p = Pattern.compile(yearPattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(RYMFormedString);

        while (m.find()) {
            year = m.group();
            RYMFormedString = RYMFormedString.replace(year, "");
        }

        p = Pattern.compile(monthNumPattern, Pattern.CASE_INSENSITIVE);
        m = p.matcher(RYMFormedString);
        while (m.find()) {
            monthNum = m.group();
            RYMFormedString = RYMFormedString.replace(monthNum, "");
        }

        p = Pattern.compile(monthPattern, Pattern.CASE_INSENSITIVE);
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
            RYMFormedString = RYMFormedString.replaceFirst(Pattern.quote(" , "), "");
        }

        if (RYMFormedString.startsWith(",")) {
            RYMFormedString = RYMFormedString.replaceFirst(Pattern.quote(", "), "");
        }

        if (RYMFormedString.startsWith(" ")) {
            RYMFormedString = RYMFormedString.replaceFirst(Pattern.quote(" "), "");
        }

        country = RYMFormedString.split(", ")[RYMFormedString.split(", ").length - 1];

        String formed[] = new String[2];
        formed[0] = date;
        formed[1] = country;

        /*System.out.println(resultString);
         System.out.println(country);*/
        return formed;
    }

    private ArrayList<String> parseLocation(String string) {
        return new ArrayList<>(Arrays.asList(string.split(", ")));
    }

    public String getArtistUrl() {
        return initArtistUrl;
    }

    public String getAlbumUrl() {
        return initAlbumUrl;
    }

    public boolean artistLinkExists(String name) throws IOException {
        Document doc = Jsoup.connect(initArtistUrl + validateUrl(name))
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20000).get();
        return doc != null;
    }

    public boolean albumLinkExists(String name) throws IOException {
        Document doc = Jsoup.connect(initAlbumUrl + validateUrl(name))
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                .timeout(20000).get();
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

    public void initUrls() {
        setCurrentAlbumUrl(initAlbumUrl + validateUrl(getAudioArtistName()) + "/" + validateUrl(getAudioAlbumName()));
        setCurrentArtistUrl(initArtistUrl + validateUrl(getAudioArtistName()));
    }

    public String getRYMArtistName() {
        return RYMArtistName;
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

    public ArrayList<Record> getAlbumRecords() {
        return albumRecords;
    }

    public ArrayList<Record> getLiveRecords() {
        return liveRecords;
    }

    public ArrayList<Record> getEpRecords() {
        return epRecords;
    }

    public ArrayList<Record> getSingleRecords() {
        return singleRecords;
    }

    public ArrayList<Record> getAppearsRecords() {
        return appearsRecords;
    }

    public ArrayList<Record> getCompRecords() {
        return compRecords;
    }

    public ArrayList<Record> getVaRecords() {
        return vaRecords;
    }

    public ArrayList<Record> getBootlegRecords() {
        return bootlegRecords;
    }

    public ArrayList<Record> getVideoRecords() {
        return videoRecords;
    }

    public void setCurrentAlbumUrl(String currentAlbumUrl) {
        this.currentAlbumUrl = currentAlbumUrl;
    }

    public Artist getArtist() {
        return artist;
    }

    public String getAudioAlbumName() {
        return audioAlbumName;
    }

    public void setAudioAlbumName(String audioAlbumName) {
        this.audioAlbumName = audioAlbumName;
    }

    public String getAudioArtistName() {
        return audioArtistName;
    }

    public void setAudioArtistName(String audioArtistName) {
        this.audioArtistName = audioArtistName;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getCurrentArtistUrl() {
        return currentArtistUrl;
    }

    public void setCurrentArtistUrl(String currentArtistUrl) {
        this.currentArtistUrl = currentArtistUrl;
    }

    @Override
    public void run() {

    }
}
