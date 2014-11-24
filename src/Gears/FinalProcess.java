package Gears;

import Entities.Issue;
import OutEntities.AudioProperties;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

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

        System.out.println(outputArtistValue);
        System.out.println(outputAlbumYearValue);
        System.out.println(outputAlbumTypeValue);
        System.out.println(outputArtistGenresValue);
        System.out.println(outputRecordGenresValue);
        System.out.println(outputCountryValue);
    }

    private void normalizeTracklist() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {
        for (Medium medium : itemProperties.getMediumList()) {

            int trackCount = 1;
            String trackString = "";

            for (AudioProperties audio : medium.getListOfAudioFiles()) {

                MP3File file = audio.getAudioFile();

                if (file.getTag().getFirst(FieldKey.DISC_NO).isEmpty()) {
                    if (medium.getCdN() > 0) {
                        file.getTag().setField(FieldKey.DISC_NO, Integer.toString(audio.getCdN()));
                    }
                }

                if (trackCount < 10) {
                    trackString = "0" + Integer.toString(trackCount);
                } else {
                    trackString = Integer.toString(trackCount);
                }

                file.getTag().setField(FieldKey.TRACK, trackString);

                trackCount++;

                try {
                    file.commit();
                } catch (CannotWriteException e) {
                    System.out.println("mp3 tag commit failed");
                }
            }
        }

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
    

    private boolean audioArtworkExists(MP3File file) {
        return !file.getTag().getArtworkList().isEmpty();
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
