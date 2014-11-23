package Gears;

import Entities.Issue;
import OutEntities.ItemProperties;

public class FinalProcess {

    private ItemProperties itemProperties;
    private Issue selectedIssue;
    private RYMParser rymp;

    private String outputCountryValue;
    private String outputArtistGenresValue;
    private String outputRecordGenresValue;
    private String outputArtistValue;
    private String outputAlbumYearValue;
    private String outputAlbumTypeValue;

    public FinalProcess(ItemProperties itemProperties) {
        this.itemProperties = itemProperties;
    }

    public void launch() {
        fillOutputValues();

        System.out.println(rymp.getCurrentRecord().getName());
        System.out.println(rymp.getCurrentRecord().getType());
        System.out.println(rymp.getCurrentRecord().getLink());
    }

    private void fillOutputValues() {
        if (rymp.getCurrentArtist() != null) {
            if (!rymp.getCurrentArtist().getName().isEmpty()) {
                outputArtistValue = rymp.getCurrentArtist().getName();
            }
            if (!rymp.getCurrentArtist().getGenres().isEmpty()) {
                outputArtistGenresValue = rymp.getCurrentArtist().firstThreeGenresToString();
            } else if (rymp.getCurrentRecord() != null && !rymp.getCurrentRecord().getGenres().isEmpty()) {
                outputArtistGenresValue = rymp.getCurrentRecord().firstThreeGenresToString();
            }

            if (!rymp.getCurrentArtist().getCountry().isEmpty()) {
                outputCountryValue = rymp.getCurrentArtist().getCountry();
            }
        } else {
            System.out.println("Current artist = null");
        }

        if (rymp.getCurrentRecord() != null) {
            if (!rymp.getCurrentRecord().getGenres().isEmpty()) {
                outputRecordGenresValue = rymp.getCurrentRecord().allGenresToString();
            } else if (rymp.getCurrentArtist() != null && !rymp.getCurrentArtist().getGenres().isEmpty()) {
                outputRecordGenresValue = rymp.getCurrentArtist().allGenresToString();
            }

            if (!rymp.getCurrentRecord().getYearRecorded().isEmpty()) {
                outputAlbumYearValue = rymp.getCurrentRecord().getYearRecorded();
            } else if (!rymp.getCurrentRecord().getYearReleased().isEmpty()) {
                outputAlbumYearValue = rymp.getCurrentRecord().getYearReleased();
            }

            if (!rymp.getCurrentRecord().getType().isEmpty()) {
                outputAlbumTypeValue = rymp.getCurrentRecord().getType();
            }
        } else {
            System.out.println("Current record = null");
        }
    }

    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    public void setSelectedIssue(Issue selectedIssue) {
        this.selectedIssue = selectedIssue;
    }

    public void setRymp(RYMParser rymp) {
        this.rymp = rymp;
    }

}
