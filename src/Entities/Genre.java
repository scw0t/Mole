package Entities;

import java.util.logging.Logger;

public class Genre {

    private String name;
    private String link;

    /**
     *
     */
    public Genre() {
        name = "";
        link = "";
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
}
