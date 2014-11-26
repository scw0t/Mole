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

    /**
     *
     * @param name
     */
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
    public ArrayList<Person> getMembers() {
        return members;
    }

    /**
     *
     * @param members
     */
    public void setMembers(ArrayList<Person> members) {
        this.members = members;
    }

    /**
     *
     * @return
     */
    public ArrayList<Artist> getRelated() {
        return related;
    }

    /**
     *
     * @param related
     */
    public void setRelated(ArrayList<Artist> related) {
        this.related = related;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getAka() {
        return aka;
    }

    /**
     *
     * @param aka
     */
    public void setAka(ArrayList<String> aka) {
        this.aka = aka;
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
    public String getNotes() {
        return notes;
    }

    /**
     *
     * @param notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     *
     * @return
     */
    public String getFormedDate() {
        return formedDate;
    }

    /**
     *
     * @param formedDate
     */
    public void setFormedDate(String formedDate) {
        this.formedDate = formedDate;
    }

    /**
     *
     * @return
     */
    public String getCountry() {
        if (country.equals("United Kingdom")) {
            country = "UK";
        }
        if (country.equals("United States")) {
            country = "USA";
        }

        return country;
    }

    /**
     *
     * @param country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     * @return
     */
    public String getDiedDate() {
        return diedDate;
    }

    /**
     *
     * @param diedDate
     */
    public void setDiedDate(String diedDate) {
        this.diedDate = diedDate;
    }

    /**
     *
     * @return
     */
    public ArrayList<Artist> getMemberOf() {
        return memberOf;
    }

    /**
     *
     * @param memberOf
     */
    public void setMemberOf(ArrayList<Artist> memberOf) {
        this.memberOf = memberOf;
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
}
