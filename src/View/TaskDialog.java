package View;

import Gears.Mole;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TaskDialog extends Stage{
    
    private final Label pathLabel1;
    private final Label pathLabel2;
    private final ProgressBar progressBar;
    
    public TaskDialog() {
        super(StageStyle.TRANSPARENT);
        initModality(Modality.WINDOW_MODAL);
        initOwner(Mole.primaryStage);
        
        progressBar = new ProgressBar(0);
        
        pathLabel1 = new Label();
        pathLabel1.setStyle("-fx-font-weight: bold;");
        
        pathLabel2 = new Label();
        
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(10);
        vBox.getChildren().addAll(pathLabel1, pathLabel2, progressBar);
        
        progressBar.setMinWidth(450);
        
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-focus-color: transparent;"
                + "-fx-background-insets: 0, 0, 1, 2;");
        closeButton.setOnAction((ActionEvent e) -> {
            TaskDialog.this.close();
        });
        
        VBox vBox1 = new VBox(closeButton);
        vBox1.setAlignment(Pos.TOP_LEFT);
        
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        borderPane.setRight(vBox1);
        borderPane.setStyle("-fx-border-color: black;");
        setScene(new Scene(borderPane, 500, 100));
    }

    public Label getPathLabel1() {
        return pathLabel1;
    }

    public void setPathLabel1(String path) {
        this.pathLabel1.setText(path);
    }

    public Label getPathLabel2() {
        return pathLabel2;
    }

    public void setPathLabel2(String path) {
        this.pathLabel2.setText(path);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
