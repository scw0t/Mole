package View;

import Entities.Issue;
import Gears.Mole;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

public class CatDialog extends Stage {

    private Button okButton;
    private Button cancelButton;
    private ObservableList<CatBox> observableList;
    private CatList catList;
    private Font font;
    private boolean keyPressed;

    public CatDialog() {
        super(StageStyle.DECORATED);
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        this.initModality(Modality.WINDOW_MODAL);
        this.initOwner(Mole.primaryStage);

        font = Font.loadFont(ClassLoader.class.getResourceAsStream("/QuicksandFamily/Quicksand_Bold.otf") , 16);
        
        if (font == null) {
            font = new Font("Cambria", 16);
        }
    }

    public void initGUI() {
        setOkButton(new Button("OK"));
        setCancelButton(new Button("Cancel"));
        catList = new CatList(null);

        okButton.setPrefWidth(70);
        okButton.setStyle("-fx-focus-color: transparent;");

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
        buttonsBox.getChildren().addAll(getOkButton(), getCancelButton());

        VBox vBox = new VBox(5);
        VBox.setVgrow(getCatList(), Priority.ALWAYS);
        VBox.setVgrow(buttonsBox, Priority.NEVER);
        vBox.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane();

        vBox.getChildren().addAll(getCatList(), buttonsBox);

        scrollPane.setContent(vBox);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        this.setScene(new Scene(scrollPane, 500, 700));
        this.setTitle("Choose Issue");
    }

    public void setContent(ArrayList<Issue> issues) {
        ArrayList<CatBox> catBoxArrayList = new ArrayList<>();
        for (Issue is : issues) {
            catBoxArrayList.add(new CatBox(is));
        }
        observableList = FXCollections.observableArrayList(catBoxArrayList);
        getCatList().setItems(observableList);
    }

    public Button getOkButton() {
        return okButton;
    }

    public void setOkButton(Button okButton) {
        this.okButton = okButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(Button cancelButton) {
        this.cancelButton = cancelButton;
    }

    public CatList getCatList() {
        return catList;
    }

    public boolean isKeyPressed() {
        return keyPressed;
    }

    public void setKeyPressed(boolean keyPressed) {
        this.keyPressed = keyPressed;
    }

    class CatBox extends VBox {

        private final Issue issue;

        public CatBox(Issue issue) {
            this.issue = issue;
            init();
        }

        private void init() {
            Label nameLabel = new Label(getIssue().getIssueName());
            Label attrLabel = new Label(getIssue().getIssueAttributes());
            Label yearLabel = new Label(getIssue().getIssueYear());
            Label formLabel = new Label(getIssue().getIssueFormats());
            Label lablLabel = new Label(getIssue().getIssueLabel());
            Label catlLabel = new Label(getIssue().getCatNumber());
            Label cntrLabel = new Label(getIssue().getIssueCountries());

            catlLabel.setFont(font);
            lablLabel.setFont(font);
            catlLabel.setTextFill(Color.web("#355D7F"));
            lablLabel.setTextFill(Color.web("#7B564E"));

            HBox hiHBox = new HBox();
            hiHBox.setSpacing(10);

            hiHBox.getChildren().addAll(nameLabel, attrLabel, cntrLabel);
            hiHBox.setAlignment(Pos.CENTER_LEFT);
            HBox loHBox = new HBox();
            loHBox.setSpacing(10);
            loHBox.setAlignment(Pos.CENTER_LEFT);
            loHBox.getChildren().addAll(yearLabel, formLabel, lablLabel, catlLabel);
            getChildren().addAll(hiHBox, loHBox);
        }

        public Issue getIssue() {
            return issue;
        }
    }

    class CatList extends ListView<CatBox> {

        public CatList(ObservableList<CatBox> items) {
            super(items);
            setStyle("-fx-focus-color: transparent;");
            setCellFactory(new Callback<ListView<CatBox>, ListCell<CatBox>>() {
                @Override
                public ListCell<CatBox> call(ListView<CatBox> p) {
                    return new CustomCellFactory();
                }
            });

        }

        public class CustomCellFactory extends ListCell<CatBox> {

            private CatBox catBox;

            public CustomCellFactory() {
                this.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        //System.out.println("clicked");
                    }
                });
            }

            @Override
            protected void updateItem(CatBox cb, boolean b) {
                super.updateItem(cb, b);
                setGraphic(cb);
                catBox = cb;
            }
        }

    }

}
