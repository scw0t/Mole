package Gears;

import Entities.Issue;
import Entities.Record;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

public class TagProcessor {

    private ItemProperties itemsProps;
    private final RYMParser rymp;
    private SimpleListProperty<Issue> issueListProperty;
    private ObservableList<Issue> issueList;
    private StringProperty message;

    public TagProcessor(ItemProperties itemsProps) {
        this.itemsProps = itemsProps;
        message = new SimpleStringProperty();
        issueList = FXCollections.observableArrayList();
        issueListProperty = new SimpleListProperty<>(issueList);
        rymp = new RYMParser();
        rymp.setMessage(message);
    }

    public void launch() {
        if (itemsProps.hasVaAttribute()) {                      //СБОРНИК
            if (itemsProps.getCdN() > 0) {                      //CD>1
                System.out.println("Not supported yet");
            } else {                                            //1
                System.out.println("Not supported yet");
            }

        } else {                                                //ОБЫЧНЫЙ
            if (itemsProps.getCdN() > 0) {                      //CD>1
                System.out.println("Not supported yet");
            } else {                                            //1
                for (Medium medium : itemsProps.getMediumList()) {
                    searchCommonRelease(medium);
                }
            }
        }
    }

    private void searchCommonRelease(Medium medium) {
        rymp.setInputArtistNameAndInitUrl(medium.getArtist());
        rymp.setInputAlbumNameAndInitUrl(medium.getAlbum());

        //поиск по имени релиза
        if (rymp.parseAlbumInfo()) {
            rymp.parseArtistInfo(rymp.getRymArtistName());
        } else {
            if (rymp.parseArtistInfo(medium.getArtist())) {
                try {
                    checkDiscography();
                } catch (KeyNotFoundException |
                        IOException |
                        TagException |
                        ReadOnlyFileException |
                        InvalidAudioFrameException ex) {
                    Logger.getLogger(TagProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                
            }
        }

        if (rymp.getCurrentRecord() != null) {
            if (rymp.getCurrentRecord().getIssues() != null && !rymp.getCurrentRecord().getIssues().isEmpty()) {
                issueList.addAll(rymp.getCurrentRecord().getIssues());
            }
        }
    }

    private void checkDiscography() throws KeyNotFoundException,
            FieldDataInvalidException,
            IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException {
        int recordQuantityMatches = 0;

        // Проверяем альбомы
        for (Record record : rymp.getLpRecords()) {
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем live albums
        for (Record record : rymp.getLiveRecords()) {
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем compilations
        for (Record record : rymp.getCompRecords()) {
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        for (Record record : rymp.getBootlegRecords()) {
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем va
        for (Record record : rymp.getVaRecords()) {
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        //Проверяем ep
        for (Record record : rymp.getEpRecords()) {

            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(rymp.getInputAlbumName())) {
                        recordQuantityMatches++;
                        System.out.println(singlePart);
                    }
                }
            }

            System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        //Проверяем синглы
        for (Record record : rymp.getSingleRecords()) {

            // Проверяем, есть ли в имени записи на RYM разделитель " / "
            // если есть, разделяем строку и сравниваем ее с именем альбома
            // скорее всего перед нами сингл или EP
            if (record.getName().contains(" / ")) {
                for (String singlePart : record.getName().split(" / ")) {
                    if (singlePart.equals(rymp.getInputAlbumName())) {
                        recordQuantityMatches++;
                        System.out.println(singlePart);
                    }
                }
            }

            if (StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                System.out.println("Lev distance = " + StringUtils.getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()));
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

    }

    public ObservableList<Issue> getIssueList() {
        return issueList;
    }

    public SimpleListProperty<Issue> getIssueListProperty() {
        return issueListProperty;
    }

    public StringProperty getMessageProperty() {
        return message;
    }

    public void setMessageProperty(StringProperty message) {
        this.message = message;
    }
    
    public String getMessageValue() {
        return message.getValue();
    }

    public void setMessageValue(String message) {
        this.message.setValue(message);
    }

    public RYMParser getRymp() {
        return rymp;
    }


    class TestTask extends Task {

        @Override
        protected Object call() throws Exception {

            return null;
        }
    }

}
