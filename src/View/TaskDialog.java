package View;

import static Gears.Mole.primaryStage;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_LEFT;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import static javafx.stage.Modality.WINDOW_MODAL;
import javafx.stage.Stage;
import static javafx.stage.StageStyle.TRANSPARENT;

public class TaskDialog extends Stage {

    private final Label pathLabel1;
    private final Label pathLabel2;
    private final ProgressBar progressBar;

    /**
     *
     */
    public TaskDialog() {
        super(TRANSPARENT);
        initModality(WINDOW_MODAL);
        initOwner(primaryStage);

        progressBar = new ProgressBar(0);

        pathLabel1 = new Label();
        pathLabel1.setStyle("-fx-font-weight: bold;");

        pathLabel2 = new Label();

        VBox vBox = new VBox();
        vBox.setAlignment(CENTER_LEFT);
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
        vBox1.setAlignment(TOP_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(vBox);
        borderPane.setRight(vBox1);
        borderPane.setStyle("-fx-border-color: black;");
        setScene(new Scene(borderPane, 500, 100));
    }

    /**
     *
     * @return
     */
    public Label getPathLabel1() {
        return pathLabel1;
    }

    /**
     *
     * @param path
     */
    public void setPathLabel1(String path) {
        this.pathLabel1.setText(path);
    }

    /**
     *
     * @return
     */
    public Label getPathLabel2() {
        return pathLabel2;
    }

    /**
     *
     * @param path
     */
    public void setPathLabel2(String path) {
        this.pathLabel2.setText(path);
    }

    /**
     *
     * @return
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }
}
