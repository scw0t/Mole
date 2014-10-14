
import java.util.ArrayList;

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

    public void printRecordContent() {
        System.out.println("------------------------------------");
        System.out.println(getYearRecorded() + " - " + name + " [" + type + "]");
        System.out.println(link);

        /*if (!subArtistNames.isEmpty() && !subArtistLinks.isEmpty()) {
            System.out.println();

            for (int i = 0; i < subArtistNames.size(); i++) {
                System.out.println(subArtistNames.get(i) + " (" + subArtistLinks.get(i) + ")");
            }
        }*/
    }
    
    public void printIssues(){
        for (Issue is : issues) {
            is.printAll();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        /*        if (type.contains("Album")) {
         type = type.replaceAll("Album", "LP");
         }*/
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getYearRecorded() {
        return yearRecorded;
    }

    public void setYearRecorded(String yearRecorded) {
        this.yearRecorded = yearRecorded;
    }

    public String getYearReleased() {
        return yearReleased;
    }

    public void setYearReleased(String yearReleased) {
        this.yearReleased = yearReleased;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public ArrayList<Artist> getSubArtists() {
        return subArtists;
    }

    public void setSubArtists(ArrayList<Artist> subArtists) {
        this.subArtists = subArtists;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public ArrayList<Issue> getIssues() {
        return issues;
    }

    public void setIssues(ArrayList<Issue> issues) {
        this.issues = issues;
    }

}
