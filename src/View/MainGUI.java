package View;

import Gears.LogOutput;
import OutEntities.AudioProperties;
import OutEntities.ClusterModel;
import OutEntities.FileProperties;
import OutEntities.IncomingDirectory;
import OutEntities.ReleaseProperties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MasterDetailPane;

public class MainGUI extends BorderPane {

    private LogOutput logOutput;

    private final String initDirPath = "G:\\test";
    private final TextField pathTextArea;
    public static ObservableList<IncomingDirectory> initialDirectoryList;
    public static TextArea logTextArea;
    public static ClusterTableView<ClusterModel> tableView;

    public MainGUI() throws FileNotFoundException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.controlsfx").setLevel(Level.OFF);

        initialDirectoryList = FXCollections.observableArrayList();

        pathTextArea = new TextField(initDirPath);
        HBox.setHgrow(pathTextArea, Priority.ALWAYS);

        logTextArea = new TextArea();
        logTextArea.setMaxHeight(Double.MAX_VALUE);
        logTextArea.setStyle("-fx-focus-color: transparent;");

        setId("background");
        initElements();
    }

    private void initElements() throws FileNotFoundException {
        Button openButton = new Button("Open");
        openButton.getStyleClass().addAll("first");
        openButton.setOnAction((ActionEvent t) -> {
            DirTreeView treeView = new DirTreeView();
            try {
                initialDirectoryList.clear();
                treeView.drawGUI();
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        Button runButton = new Button("Run");
        runButton.getStyleClass().addAll("last");
        runButton.setOnAction((ActionEvent event) -> {
            Thread mainProcessThread = new Thread(new MainTask());
            mainProcessThread.setDaemon(true);
            mainProcessThread.start();
        });

        Button settingsButton = new Button("", new ImageView(new Image(new FileInputStream("settings.png"))));
        settingsButton.getStyleClass().addAll("last");

        Region spacer = new Region();
        spacer.getStyleClass().setAll("spacer");

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().setAll("segmented-button-bar");
        buttonBar.getChildren().addAll(openButton, runButton, pathTextArea);

        //wrapper for =logTextArea
        VBox textAreaVbox = new VBox();
        textAreaVbox.setPadding(new Insets(10));
        textAreaVbox.setMinHeight(500);
        textAreaVbox.setMaxHeight(Double.MAX_VALUE);
        textAreaVbox.getChildren().add(logTextArea);
        VBox.setVgrow(textAreaVbox, Priority.ALWAYS);
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

        tableView = new ClusterTableView<ClusterModel>();
        tableView.setTextArea(logTextArea);

        //wrapper for =tableView
        VBox tableViewVbox = new VBox();
        tableViewVbox.setPadding(new Insets(10));
        tableViewVbox.setMaxHeight(Double.MAX_VALUE);
        tableViewVbox.getChildren().add(tableView);
        VBox.setVgrow(tableViewVbox, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        MasterDetailPane pane = new MasterDetailPane();
        pane.setDividerPosition(0.6);
        pane.setMasterNode(tableViewVbox);
        pane.setDetailNode(textAreaVbox);
        pane.setDetailSide(Side.BOTTOM);
        pane.setShowDetailNode(true);
        setCenter(pane);

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(spacer, buttonBar, pathTextArea, settingsButton);
        setTop(toolbar);

        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ClusterModel>() {
            public void changed(ObservableValue<? extends ClusterModel> observable,
                    ClusterModel oldValue, ClusterModel newValue) {
                try {
                    
                    FileProperties entity = tableView.getSelectionModel().getSelectedItem().getEntity();
                    

                    //logTextArea.setText(sb.toString());

                } catch (NullPointerException e) {
                    //System.out.println("something happend");
                }

            }
        });

        tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent k) {
                try {
                    if (k.getCode().equals(KeyCode.SPACE) || k.getCode().equals(KeyCode.ENTER)) {
                        if (tableView.getSelectionModel().getSelectedItem().isChecked()) {
                            tableView.getSelectionModel().getSelectedItem().setCheck(false);
                        } else {
                            tableView.getSelectionModel().getSelectedItem().setCheck(true);
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println("something happend");
                }

            }
        });
    }
    
    private void buildReleaseInfo(FileProperties fp){
        StringBuilder sb = new StringBuilder();
        ReleaseProperties releaseProperties = fp.getReleaseProperties();
        if (!releaseProperties.getAudioList().isEmpty()) {
            int cdNum = 0;
            
            if (releaseProperties.artistsQuantity() > 1) {
                sb.append("VA");
            } else {
                //sb.append(releaseProperties.getAlbumTitle().g);
            }
            
            
            for (ObservableList<AudioProperties> release : releaseProperties.getAudioList()) {
                if (fp.hasMultiCDAttribute()) {
                    sb.append("CD").append(cdNum++).append(":");
                }
                
            }
        }
        
        
        
        /*if (!entity.hasMultiCDAttribute()) {
                        sb.append(entity.getListOfAudioFiles().get(0).getArtistTitle())
                                .append(" - ")
                                .append(entity.getListOfAudioFiles().get(0).getAlbumTitle())
                                .append(" (")
                                .append(entity.getListOfAudioFiles().get(0).getYear())
                                .append(")\n");
                        for (AudioProperties ae : entity.getListOfAudioFiles()) {
                            sb.append(ae.getTrackNumber())
                                    .append(". ")
                                    .append(ae.getTrackTitle())
                                    .append("\n");
                        }
                    } else {

                    }*/
    }

    class MainTask extends Task {

        @Override
        protected Object call() throws Exception {
            /*DirProcessor dp = new DirProcessor();
             dp.init();*/

            System.out.println(tableView.getSelectionModel().getSelectedItems());

            /*for (ClusterModel cl : tableView.getItems()) {
             if (cl.isChecked()) {
             System.out.println(cl.getName());
             }

             }*/
            return null;
        }
    }

}
