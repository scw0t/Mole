package Gears;

import Entities.Issue;
import Entities.Record;
import OutEntities.ItemProperties;
import OutEntities.Medium;
import java.io.IOException;
import java.util.logging.Logger;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

public class ParseFactory {

    private final ItemProperties rootItem;
    private final RYMParser rymp;
    private final SimpleListProperty<Issue> issueListProperty;
    private final ObservableList<Issue> issueList;
    private StringProperty message;

    /**
     *
     * @param rootItem
     */
    public ParseFactory(ItemProperties rootItem) {
        this.rootItem = rootItem;
        message = new SimpleStringProperty();
        issueList = observableArrayList();
        issueListProperty = new SimpleListProperty<>(issueList);
        rymp = new RYMParser();
        rymp.setMessage(message);
    }

    public void launch() {
        if (rootItem.hasVaAttribute()) {                      //СБОРНИК
            if (rootItem.getCdN() > 0) {
                //CD>1
            } else {
                //1
            }

        } else {                                                //ОБЫЧНЫЙ
            /*if (rootItem.getCdN() > 0) {                        //CD>1
             iterateItem(rootItem);

             } else {                                            //1
             if (!rootItem.getChildList().isEmpty()) {
             for (ItemProperties child : rootItem.getChildList()) {
             for (Medium medium : child.getMediumList()) {
             searchCommonRelease(medium);
             }
             }
             } else {
             for (Medium medium : rootItem.getMediumList()) {
             searchCommonRelease(medium);
             }
             }
             }*/
            iterateItem(rootItem);
        }
    }

    private void iterateItem(ItemProperties item) {
        boolean passed = false;
        
        if (!item.getChildList().isEmpty()) {
            for (ItemProperties child : item.getChildList()) {
                if (child.hasAudioAttribute()) {
                    searchCommonRelease(child.getMediumList().get(0));
                    passed = true;
                    break;
                }
            }
            
            if (!passed && !item.getMediumList().isEmpty()) {
                searchCommonRelease(item.getMediumList().get(0));
            }
        } else {
            if (item.hasAudioAttribute()) {
                searchCommonRelease(item.getMediumList().get(0));
            }
        }
    }

    private void searchCommonRelease(Medium medium) {
        boolean passed = false;
        rymp.setInputArtistNameAndInitUrl(medium.getArtist());
        rymp.setInputAlbumNameAndInitUrl(medium.getAlbum());

        //поиск по имени релиза
        if (rymp.parseAlbumInfo()) {
            rymp.parseArtistInfo(rymp.getRymArtistName());
            passed = true;
        } else {
            if (rymp.parseArtistInfo(medium.getArtist())) {
                try {
                    checkDiscography();
                    passed = true;
                } catch (KeyNotFoundException |
                        IOException |
                        TagException |
                        ReadOnlyFileException |
                        InvalidAudioFrameException ex) {
                }
            } else {
                System.out.println("ParseFactory: artist parsing failure");
            }
        }

        if (rymp.getCurrentRecord() != null) {
            if (rymp.getCurrentRecord().getIssues() != null && !rymp.getCurrentRecord().getIssues().isEmpty()) {
                issueList.addAll(rymp.getCurrentRecord().getIssues());
            }
        }
        
        if (rymp.getCurrentRecord() != null) {
            medium.setType(rymp.getCurrentRecord().getType());
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
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем live albums
        for (Record record : rymp.getLiveRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем compilations
        for (Record record : rymp.getCompRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        for (Record record : rymp.getBootlegRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

        // Проверяем va
        for (Record record : rymp.getVaRecords()) {
            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
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
                    }
                }
            }

            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
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
                    }
                }
            }

            if (getLevenshteinDistance(rymp.getInputAlbumName(), record.getName()) <= 2) {
                rymp.setCurrentAlbumUrl(record.getLink());
                rymp.parseAlbumInfo();
                break;
            }
        }

    }

    /**
     *
     * @return
     */
    public ObservableList<Issue> getIssueList() {
        return issueList;
    }

    /**
     *
     * @return
     */
    public SimpleListProperty<Issue> getIssueListProperty() {
        return issueListProperty;
    }

    /**
     *
     * @return
     */
    public StringProperty getMessageProperty() {
        return message;
    }

    /**
     *
     * @param message
     */
    public void setMessageProperty(StringProperty message) {
        this.message = message;
    }

    /**
     *
     * @return
     */
    public String getMessageValue() {
        return message.getValue();
    }

    /**
     *
     * @param message
     */
    public void setMessageValue(String message) {
        this.message.setValue(message);
    }

    /**
     *
     * @return
     */
    public RYMParser getRymp() {
        return rymp;
    }

    class TestTask extends Task {

        @Override
        protected Object call() throws Exception {

            return null;
        }
    }
    private static final Logger LOG = Logger.getLogger(ParseFactory.class.getName());

}
