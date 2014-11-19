package View;

import Gears.Mole;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TestView extends Stage {

    private Label artistLabel;
    private Label albumLabel;

    public TestView() {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Mole.primaryStage);

        artistLabel = new Label();
        albumLabel = new Label();

        Button okButton = new Button("Ok");
        okButton.setPrefWidth(70);
        okButton.setStyle("-fx-focus-color: transparent;");
        okButton.setOnAction(new EventHandler<ActionEvent>() { 
            @Override
            public void handle(ActionEvent event) {
                System.out.println(artistLabel.getText() + " - " + albumLabel.getText());
                close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-focus-color: transparent;");
        cancelButton.setPrefWidth(70);
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                close();
            }
        });

        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(50);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10));
        buttonsBox.getChildren().addAll(okButton, cancelButton);

        VBox labelVbox = new VBox(5);
        labelVbox.setPadding(new Insets(10));
        labelVbox.setAlignment(Pos.TOP_CENTER);
        labelVbox.getChildren().addAll(artistLabel, albumLabel);

        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(buttonsBox);
        borderPane.setCenter(labelVbox);

        setScene(new Scene(borderPane, 300, 300));
        //show();
    }
    
    public void initProcess(){
        for (int i = 0; i < 10; i++) {
            System.out.println("item" + i);
        }
    }

    public void setArtistLabel(String artist) {
        this.artistLabel.setText(artist);
    }

    public void setAlbumLabel(String album) {
        this.albumLabel.setText(album); 
    }

    public Label getArtistLabel() {
        return artistLabel;
    }

    public Label getAlbumLabel() {
        return albumLabel;
    }

}
