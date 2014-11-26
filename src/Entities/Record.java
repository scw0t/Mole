package Entities;

import static java.lang.System.out;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Record {

    private String name;
    private String type;
    private String link;
    private String yearRecorded;
    private String yearReleased;
    private String rating;
    private Artist artist;
    private ArrayList<Genre> genres;
    private ArrayList<Artist> subArtists;
    private ArrayList<Issue> issues;

    /**
     *
     * @param type
     */
    public Record(String type) {
        this.rating = "";
        this.yearReleased = "";
        this.yearRecorded = "";
        this.link = "";
        this.type = "";
        this.name = "";
        this.type = type;
        this.genres = new ArrayList<>();
        this.subArtists = new ArrayList<>();
    }

    /**
     *
     */
    public Record() {
        this.rating = "";
        this.yearReleased = "";
        this.yearRecorded = "";
        this.link = "";
        this.type = "";
        this.name = "";
        this.genres = new ArrayList<>();
        this.subArtists = new ArrayList<>();
    }

    /**
     *
     * @return
     */
    public String firstThreeGenresToString() {
        String string = "";
        if (!genres.isEmpty()) {
            if (genres.size() > 3) {
                for (int i = 0; i < 3; i++) {
                    if (i != 2) {
                        string += genres.get(i).getName() + ", ";
                    } else {
                        string += genres.get(i).getName();
                    }
                }
            } else {
                for (int i = 0; i < genres.size(); i++) {
                    if (i != genres.size() - 1) {
                        string += genres.get(i).getName() + ", ";
                    } else {
                        string += genres.get(i).getName();
                    }
                }
            }
            return string;
        } else {
            return string;
        }
    }

    /**
     *
     * @return
     */
    public String allGenresToString() {
        String string = "";
        if (!genres.isEmpty()) {
            for (int i = 0; i < genres.size(); i++) {
                if (i < genres.size() - 1) {
                    string += genres.get(i).getName() + ", ";
                } else {
                    string += genres.get(i).getName();
                }
            }
            return string;
        } else {
            return string;
        }
    }

    /**
     *
     */
    public void printRecordContent() {
        out.println("Record content------------------------------------");
        out.println(getYearRecorded() + " - " + name + " [" + type + "]");
        System.out.println(link);

        /*if (!subArtistNames.isEmpty() && !subArtistLinks.isEmpty()) {
         System.out.println();

         for (int i = 0; i < subArtistNames.size(); i++) {
         System.out.println(subArtistNames.get(i) + " (" + subArtistLinks.get(i) + ")");
         }
         }*/    }

    /**
     *
     */
    public void printIssues() {
        issues.stream().forEach((is) -> {
            is.printIssuesInfo();
        });
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getLink() {
        return link;
    }

    /**
     *
     * @param link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     *
     * @return
     */
    public String getType() {
        /*        if (type.contains("Album")) {
         type = type.replaceAll("Album", "LP");
         }*/
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public String getYearRecorded() {
        return yearRecorded;
    }

    /**
     *
     * @param yearRecorded
     */
    public void setYearRecorded(String yearRecorded) {
        this.yearRecorded = yearRecorded;
    }

    /**
     *
     * @return
     */
    public String getYearReleased() {
        return yearReleased;
    }

    /**
     *
     * @param yearReleased
     */
    public void setYearReleased(String yearReleased) {
        this.yearReleased = yearReleased;
    }

    /**
     *
     * @return
     */
    public ArrayList<Genre> getGenres() {
        return genres;
    }

    /**
     *
     * @param genres
     */
    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    /**
     *
     * @return
     */
    public ArrayList<Artist> getSubArtists() {
        return subArtists;
    }

    /**
     *
     * @param subArtists
     */
    public void setSubArtists(ArrayList<Artist> subArtists) {
        this.subArtists = subArtists;
    }

    /**
     *
     * @return
     */
    public Artist getArtist() {
        return artist;
    }

    /**
     *
     * @param artist
     */
    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    /**
     *
     * @return
     */
    public ArrayList<Issue> getIssues() {
        return issues;
    }

    /**
     *
     * @param issues
     */
    public void setIssues(ArrayList<Issue> issues) {
        this.issues = issues;
    }
    private static final Logger LOG = Logger.getLogger(Record.class.getName());

}
