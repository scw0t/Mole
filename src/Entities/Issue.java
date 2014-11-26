package Entities;

import static java.lang.System.out;
import java.util.logging.Logger;

public class Issue {

    private boolean isPrimary;
    private String issueTitle;
    private String issueAttributes;
    private String issueYear;
    private String issueFormats;
    private String issueLabel;
    private String catNumber;
    private String issueCountries;
    private String link;
    private String labelLink;

    /**
     *
     */
    public Issue() {
    }

    /**
     *
     */
    public void printIssuesInfo() {
        out.println(issueTitle + " | " + issueAttributes);
    }

    /**
     *
     * @return
     */
    public boolean isIsPrimary() {
        return isPrimary;
    }

    /**
     *
     * @param isPrimary
     */
    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    /**
     *
     * @return
     */
    public String getIssueTitle() {
        return issueTitle;
    }

    /**
     *
     * @param issueTitle
     */
    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    /**
     *
     * @return
     */
    public String getIssueAttributes() {
        return issueAttributes;
    }

    /**
     *
     * @param issueAttributes
     */
    public void setIssueAttributes(String issueAttributes) {
        this.issueAttributes = issueAttributes;
    }

    /**
     *
     * @return
     */
    public String getIssueYear() {
        return issueYear;
    }

    /**
     *
     * @param issueYear
     */
    public void setIssueYear(String issueYear) {
        this.issueYear = issueYear;
    }

    /**
     *
     * @return
     */
    public String getIssueFormats() {
        return issueFormats;
    }

    /**
     *
     * @param issueFormats
     */
    public void setIssueFormats(String issueFormats) {
        this.issueFormats = issueFormats;
    }

    /**
     *
     * @return
     */
    public String getIssueLabel() {
        return issueLabel;
    }

    /**
     *
     * @param issueLabel
     */
    public void setIssueLabel(String issueLabel) {
        this.issueLabel = issueLabel;
    }

    /**
     *
     * @return
     */
    public String getIssueCountries() {
        return issueCountries;
    }

    /**
     *
     * @param issueCountries
     */
    public void setIssueCountries(String issueCountries) {
        this.issueCountries = issueCountries;
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
    public String getLabelLink() {
        return labelLink;
    }

    /**
     *
     * @param labelLink
     */
    public void setLabelLink(String labelLink) {
        this.labelLink = labelLink;
    }

    /**
     *
     * @return
     */
    public String getCatNumber() {
        return catNumber;
    }

    /**
     *
     * @param catNumber
     */
    public void setCatNumber(String catNumber) {
        this.catNumber = catNumber;
    }
}
