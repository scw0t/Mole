package Gears;

import Entities.Issue;
import View.CListView;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class ParseTask extends Task<ObservableList<CListView.CModel>> {

    private ParseFactory parseFactory;
    private SimpleStringProperty artistProperty = new SimpleStringProperty(this, "artistProperty");
    private SimpleStringProperty albumProperty = new SimpleStringProperty(this, "albumProperty");
    private SimpleStringProperty typeProperty = new SimpleStringProperty(this, "typeProperty");

    @Override
    protected ObservableList<CListView.CModel> call() throws Exception {

        ObservableList<CListView.CModel> resultList = FXCollections.observableArrayList();

        parseFactory.getMessageProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            updateMessage(newValue);
        });

        try {
            parseFactory.launch();
            if (parseFactory.getIssueList().isEmpty()) {
                Platform.runLater(() -> {
                    artistProperty.set(parseFactory.getRymp().getInputArtistName());
                    albumProperty.set(parseFactory.getRymp().getInputAlbumName());
                });
                updateMessage("Nothing to found");
            } else {
                Platform.runLater(() -> {
                    artistProperty.set(parseFactory.getRymp().getCurrentArtist().getName());
                    albumProperty.set(parseFactory.getRymp().getCurrentRecord().getName());
                    typeProperty.set(parseFactory.getRymp().getCurrentRecord().getType());
                });
                updateMessage("Searching complete");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            updateProgress(1.0f, 1.0f);
        }

        for (Issue issue : parseFactory.getIssueList()) {
            resultList.add(new CListView.CModel(issue));
        }

        return resultList;
    }
    
    public void setParseFactory(ParseFactory parseFactory) {
        this.parseFactory = parseFactory;
    }

    public SimpleStringProperty getArtistProperty() {
        return artistProperty;
    }

    public SimpleStringProperty getAlbumProperty() {
        return albumProperty;
    }

    public SimpleStringProperty getTypeProperty() {
        return typeProperty;
    }

}
