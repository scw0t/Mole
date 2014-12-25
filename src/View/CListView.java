package View;

import Entities.Issue;
import Gears.FinalProcess;
import Gears.Mole;
import Gears.ParseFactory;
import Gears.ParseTask;
import OutEntities.ItemProperties;
import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

public class CListView extends Stage {

    private boolean terminated = false;
    private final CTable cTable;
    private final Label stateLabel;
    private ProgressIndicator indicator;
    private FinalProcess finalProcess;
    private ItemProperties rootItem;

    private TextField artistTextField = new TextField();
    private TextField albumTextField = new TextField();
    private TextField typeTextField = new TextField();
    private ComboBox typeBox = new ComboBox();

    public CListView() {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Mole.primaryStage);
        stateLabel = new Label();
        indicator = new ProgressIndicator(-1.0f);
        indicator.setMinWidth(20);
        indicator.setMinHeight(20);
        indicator.setPrefWidth(20);
        indicator.setPrefHeight(20);

        setOnCloseRequest((WindowEvent event) -> {
            terminated = true;
        });

        cTable = new CTable();
        cTable.setOnKeyPressed((KeyEvent e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                terminated = true;
                close();
            }
        });
        VBox.setVgrow(cTable, Priority.ALWAYS);

        Button okButton = new Button("Ok");
        okButton.setPrefSize(70, 30);
        okButton.setStyle("-fx-focus-color: transparent;");
        okButton.setOnAction((ActionEvent event) -> {
            if (!cTable.getItems().isEmpty()) {
                if (!cTable.getSelectionModel().isEmpty()) {
                    Issue selected;
                    if (cTable.getSelectionModel().getSelectedItem() == null) {
                        selected = cTable.getItems().get(0).getIssue();
                    } else {
                        selected = cTable.getSelectionModel().getSelectedItem().getIssue();
                    }
                    finalProcess.setSelectedIssue(selected);
                    try {
                        finalProcess.launch();
                    } catch (KeyNotFoundException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                        System.out.println("Final process exception");
                        ex.printStackTrace();
                    } finally {

                    }
                }
            }
            close();
        });

        Button skipButton = new Button("Skip");
        skipButton.setStyle("-fx-focus-color: transparent;");
        skipButton.setPrefSize(70, 30);
        skipButton.setOnAction((ActionEvent event) -> {
            close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-focus-color: transparent;");
        cancelButton.setPrefSize(70, 30);
        cancelButton.setOnAction((ActionEvent event) -> {
            terminated = true;
            close();
        });

        HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(20);
        buttonsHBox.getChildren().addAll(okButton, skipButton, cancelButton);

        HBox progressHBox = new HBox();
        progressHBox.setSpacing(15);
        progressHBox.getChildren().addAll(indicator, stateLabel);

        VBox bottomVBox = new VBox();
        bottomVBox.setSpacing(10);
        bottomVBox.setPadding(new Insets(10, 0, 0, 0));
        bottomVBox.setAlignment(Pos.CENTER_LEFT);
        bottomVBox.getChildren().addAll(buttonsHBox, progressHBox);

        VBox listVBox = new VBox(5);
        listVBox.setAlignment(Pos.TOP_CENTER);
        listVBox.getChildren().addAll(cTable);

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));
        borderPane.setTop(topGrid());
        borderPane.setCenter(listVBox);
        borderPane.setBottom(bottomVBox);

        setScene(new Scene(borderPane, 600, 500));
    }

    private GridPane topGrid() {
        Button refreshButton = new Button("Refresh");
        refreshButton.setPrefWidth(100);
        refreshButton.setOnAction((ActionEvent event) -> {
            if (!artistTextField.getText().equals("") && !albumTextField.getText().equals("")) {
                ItemProperties newRootItem = new ItemProperties(rootItem);

                newRootItem.refreshData(artistTextField.getText(), albumTextField.getText(), "Album");
                ParseFactory parseFactory = new ParseFactory(newRootItem);

                finalProcess = new FinalProcess(newRootItem);
                finalProcess.setRymp(parseFactory.getRymp());

                ParseTask parseTask = new ParseTask();
                parseTask.setParseFactory(parseFactory);

                cTable.itemsProperty().unbind();
                indicator.progressProperty().unbind();
                stateLabel.textProperty().unbind();
                albumTextField.textProperty().unbind();
                artistTextField.textProperty().unbind();
                //typeTextField.textProperty().unbind();
                typeBox.getEditor().textProperty().unbind();
                
                
                
                cTable.itemsProperty().bind(parseTask.valueProperty());
                indicator.progressProperty().bind(parseTask.progressProperty());
                stateLabel.textProperty().bind(parseTask.messageProperty());
                albumTextField.textProperty().bindBidirectional(parseTask.getAlbumProperty());
                artistTextField.textProperty().bindBidirectional(parseTask.getArtistProperty());
                typeBox.getEditor().textProperty().bind(parseTask.getTypeProperty());
                //typeTextField.textProperty().bindBidirectional(parseTask.getTypeProperty());
                System.out.println(typeBox.getEditor().textProperty().getValue());

                new Thread(parseTask).start();
            }


            /*ParseFactory parseFactory = new ParseFactory(item.getItemProperty());
             FinalProcess finalProcess = new FinalProcess(item.getItemProperty());
             finalProcess.setRymp(parseFactory.getRymp());

             ParseTask parseTask = new ParseTask();
             parseTask.setParseFactory(parseFactory);*/
        });

        Label artistLabel = new Label("Artist");
        Label albumLabel = new Label("Album");
        Label typeLabel = new Label("Type");
        
        ObservableList<String> lst = FXCollections.observableArrayList(
                "Album",
                "EP",
                "Single",
                "Compilation",
                "Bootleg"
        );

        typeBox = new ComboBox(lst);
        typeBox.getSelectionModel().selectFirst();

        artistTextField = new TextField();
        albumTextField = new TextField();

        artistTextField.setEditable(true);
        albumTextField.setEditable(true);
        typeTextField.setEditable(true);

        artistTextField.setPrefWidth(300);
        albumTextField.setPrefWidth(300);
        //typeTextField.setPrefWidth(100);

        GridPane grid = new GridPane();
        HBox.setHgrow(grid, Priority.ALWAYS);
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(artistLabel, 1, 1);        // column=1 row=1
        grid.add(artistTextField, 2, 1);    // column=2 row=1
        grid.add(albumLabel, 1, 2);         // column=1 row=2
        grid.add(albumTextField, 2, 2);     // column=2 row=2
        grid.add(typeLabel, 3, 1);          // column=3 row=1
        grid.add(typeBox, 4, 1);      // column=4 row=1
        grid.add(refreshButton, 4, 2);      // column=4 row=2
        grid.setPadding(new Insets(0, 0, 10, 0));

        return grid;
    }

    public void setContent(ArrayList<Issue> issues) {
        ObservableList<CModel> cModelList = FXCollections.observableArrayList();
        for (Issue iss : issues) {
            cModelList.add(new CModel(iss));
        }
        cTable.setItems(cModelList);
    }

    public boolean isTerminated() {
        return terminated;
    }

    public CTable getcTable() {
        return cTable;
    }

    public Label getStateLabel() {
        return stateLabel;
    }

    public ProgressIndicator getIndicator() {
        return indicator;
    }

    public FinalProcess getFinalProcess() {
        return finalProcess;
    }

    public void setFinalProcess(FinalProcess finalProcess) {
        this.finalProcess = finalProcess;
    }

    public TextField getArtistTextField() {
        return artistTextField;
    }

    public void setArtistTextField(TextField artistTextField) {
        this.artistTextField = artistTextField;
    }

    public TextField getAlbumTextField() {
        return albumTextField;
    }

    public void setAlbumTextField(TextField albumTextField) {
        this.albumTextField = albumTextField;
    }

    public void setRootItem(ItemProperties rootItem) {
        this.rootItem = rootItem;
    }

    public TextField getTypeTextField() {
        return typeTextField;
    }

    public void setTypeTextField(TextField typeTextField) {
        this.typeTextField = typeTextField;
    }

    public ComboBox getTypeBox() {
        return typeBox;
    }

    public void setTypeBox(ComboBox typeBox) {
        this.typeBox = typeBox;
    }

    public class CTable extends TableView<CModel> {

        private final TableColumn<CModel, String> titleCol;
        private final TableColumn<CModel, String> yearCol;
        private final TableColumn<CModel, String> labelCol;
        private final TableColumn<CModel, String> catCol;
        private final TableColumn<CModel, String> countryCol;

        public CTable() {
            titleCol = new TableColumn<>("Title");
            yearCol = new TableColumn<>("Year");
            labelCol = new TableColumn<>("Label");
            catCol = new TableColumn<>("Cat#");
            countryCol = new TableColumn<>("Country");

            titleCol.prefWidthProperty().bind(widthProperty().multiply(0.25));
            yearCol.prefWidthProperty().bind(widthProperty().multiply(0.1));
            labelCol.prefWidthProperty().bind(widthProperty().multiply(0.25));
            catCol.prefWidthProperty().bind(widthProperty().multiply(0.2));
            countryCol.prefWidthProperty().bind(widthProperty().multiply(0.2));

            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
            labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));
            catCol.setCellValueFactory(new PropertyValueFactory<>("cat"));
            countryCol.setCellValueFactory(new PropertyValueFactory<>("country"));

            getColumns().addAll(titleCol, yearCol, labelCol, catCol, countryCol);

            setStyle("-fx-focus-color: transparent;");
        }

        public TableColumn<CModel, String> getTitleCol() {
            return titleCol;
        }

        public TableColumn<CModel, String> getYearCol() {
            return yearCol;
        }

        public TableColumn<CModel, String> getLabelCol() {
            return labelCol;
        }

        public TableColumn<CModel, String> getCatCol() {
            return catCol;
        }

        public TableColumn<CModel, String> getCountryCol() {
            return countryCol;
        }
    }

    public static class CModel {

        private final Issue issue;
        private final StringProperty title;
        private final StringProperty year;
        private final StringProperty label;
        private final StringProperty cat;
        private final StringProperty country;

        public CModel(Issue issue) {
            this.issue = issue;
            title = new SimpleStringProperty(issue.getIssueTitle());
            year = new SimpleStringProperty(issue.getIssueYear());
            label = new SimpleStringProperty(issue.getIssueLabel());
            cat = new SimpleStringProperty(issue.getCatNumber());
            country = new SimpleStringProperty(issue.getIssueCountries());
        }

        public String getTitle() {
            return title.getValue();
        }

        public void setTitle(String title) {
            this.title.setValue(title);
        }

        public String getYear() {
            return year.getValue();
        }

        public void setYear(String year) {
            this.year.setValue(year);
        }

        public String getLabel() {
            return label.getValue();
        }

        public void setLabel(String label) {
            this.label.setValue(label);
        }

        public String getCat() {
            return cat.getValue();
        }

        public void setCat(String cat) {
            this.cat.setValue(cat);
        }

        public String getCountry() {
            return country.getValue();
        }

        public void setCountry(String country) {
            this.country.setValue(country);
        }

        public Issue getIssue() {
            return issue;
        }
    }
}
