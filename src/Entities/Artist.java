package Entities;


import java.util.ArrayList;

public class Artist {

    private String name;
    private String formedDate;
    private String diedDate;
    private String country;
    private String notes;
    private String link;
    private ArrayList<Person> members;
    private ArrayList<Artist> memberOf;
    private ArrayList<Artist> related;
    private ArrayList<String> aka;
    private ArrayList<Genre> genres;

    public Artist(String name) {
        this.link = "";
        this.notes = "";
        this.country = "";
        this.diedDate = "";
        this.formedDate = "";
        this.name = name;
        members = new ArrayList<>();
        memberOf = new ArrayList<>();
        related = new ArrayList<>();
        aka = new ArrayList<>();
        genres = new ArrayList<>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Person> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Person> members) {
        this.members = members;
    }

    public ArrayList<Artist> getRelated() {
        return related;
    }

    public void setRelated(ArrayList<Artist> related) {
        this.related = related;
    }

    public ArrayList<String> getAka() {
        return aka;
    }

    public void setAka(ArrayList<String> aka) {
        this.aka = aka;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormedDate() {
        return formedDate;
    }

    public void setFormedDate(String formedDate) {
        this.formedDate = formedDate;
    }

    public String getCountry() {
        if (country.equals("United Kingdom")) {
            country = "UK";
        }
        if (country.equals("United States")) {
            country = "USA";
        }

        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDiedDate() {
        return diedDate;
    }

    public void setDiedDate(String diedDate) {
        this.diedDate = diedDate;
    }

    public ArrayList<Artist> getMemberOf() {
        return memberOf;
    }

    public void setMemberOf(ArrayList<Artist> memberOf) {
        this.memberOf = memberOf;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
