package Entities;

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

    public Issue() {
    }
    
    public void printIssuesInfo(){
        System.out.println(issueTitle + " | " + issueAttributes);
        System.out.println(issueYear + " | " + issueFormats + " | " + issueLabel + "/" + catNumber + " | " + issueCountries);
    }

    public boolean isIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getIssueAttributes() {
        return issueAttributes;
    }

    public void setIssueAttributes(String issueAttributes) {
        this.issueAttributes = issueAttributes;
    }

    public String getIssueYear() {
        return issueYear;
    }

    public void setIssueYear(String issueYear) {
        this.issueYear = issueYear;
    }

    public String getIssueFormats() {
        return issueFormats;
    }

    public void setIssueFormats(String issueFormats) {
        this.issueFormats = issueFormats;
    }

    public String getIssueLabel() {
        return issueLabel;
    }

    public void setIssueLabel(String issueLabel) {
        this.issueLabel = issueLabel;
    }

    public String getIssueCountries() {
        return issueCountries;
    }

    public void setIssueCountries(String issueCountries) {
        this.issueCountries = issueCountries;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLabelLink() {
        return labelLink;
    }

    public void setLabelLink(String labelLink) {
        this.labelLink = labelLink;
    }

    public String getCatNumber() {
        return catNumber;
    }

    public void setCatNumber(String catNumber) {
        this.catNumber = catNumber;
    }
    
}
