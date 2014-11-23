package View;

import Entities.Issue;
import Gears.FinalProcess;
import Gears.Mole;
import OutEntities.ItemProperties;
import java.util.ArrayList;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CListView extends Stage {
    
    private final CTable cTable;
    private final Label stateLabel;
    private FinalProcess finalProcess;
    private ProgressIndicator indicator;
    private boolean terminated = false;
    
    public CListView() {
        initModality(Modality.WINDOW_MODAL);
        initOwner(Mole.primaryStage);
        stateLabel = new Label("");
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
                    finalProcess.setSelectedIssue(cTable.getSelectionModel().getSelectedItem().getIssue());
                    finalProcess.launch();
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
        //buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        //buttonsBox.setAlignment(Pos.CENTER_LEFT);
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
        borderPane.setCenter(listVBox);
        borderPane.setBottom(bottomVBox);
        
        setScene(new Scene(borderPane, 500, 400));
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
    
    class CBox extends VBox {
        
        private final Issue issue;
        
        public CBox(Issue issue) {
            this.issue = issue;
            init();
        }
        
        private void init() {
            Label nameLabel = new Label(getIssue().getIssueTitle());
            Label attrLabel = new Label(getIssue().getIssueAttributes());
            Label yearLabel = new Label(getIssue().getIssueYear());
            Label formLabel = new Label(getIssue().getIssueFormats());
            Label lablLabel = new Label(getIssue().getIssueLabel());
            Label catlLabel = new Label(getIssue().getCatNumber());
            Label cntrLabel = new Label(getIssue().getIssueCountries());

            //catlLabel.setFont(font);
            //lablLabel.setFont(font);
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
    
    class CList extends ListView<CBox> {
        
        public CList(ObservableList<CBox> items) {
            super(items);
            setStyle("-fx-focus-color: transparent;");
            setCellFactory((ListView<CBox> p) -> new CustomCellFactory());
        }
        
        public class CustomCellFactory extends ListCell<CBox> {
            
            public CustomCellFactory() {
                this.setOnMouseClicked((MouseEvent event) -> {
                    //System.out.println("clicked");
                });
            }
            
            @Override
            protected void updateItem(CBox cb, boolean b) {
                super.updateItem(cb, b);
                setGraphic(cb);
            }
        }
    }
}
