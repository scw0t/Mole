package View;

import Gears.LogOutput;
import OutEntities.AudioProperties;
import OutEntities.ClusterModel;
import OutEntities.FileProperties;
import OutEntities.IncomingDirectory;
import OutEntities.Medium;
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
        pane.setDividerPosition(0.5);
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
                    logTextArea.clear();
                    FileProperties selectedRelease = tableView.getSelectionModel().getSelectedItem().getEntity();
                    logTextArea.setText(buildReleaseInfo(selectedRelease));
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

    private String buildReleaseInfo(FileProperties fp) {
        StringBuilder infoStringBuilder = new StringBuilder();
        if (fp.getCDn() > 0) {
            infoStringBuilder.append(fp.getDirectoryName()).append("\n");
            
            for (FileProperties childCD : fp.getChildList()) {
                for (Medium childMedium : childCD.getMediumList()) {
                    infoStringBuilder.append("#CD")
                            .append(childMedium.getCDn())
                            .append("\n");
                    for (AudioProperties track : childMedium.getListOfAudioFiles()) {
                        infoStringBuilder.append(track.getTrackNumber())
                            .append(". ")
                            .append(track.getTrackTitle())
                            .append("\n");
                    }
                }
            }

        } else {
            for (Medium medium : fp.getMediumList()) {
                infoStringBuilder.append(medium.getArtist())
                        .append(" - ")
                        .append(medium.getTitle())
                        .append(" (")
                        .append(medium.getYear())
                        .append(")\n");
                for (AudioProperties track : medium.getListOfAudioFiles()) {
                    infoStringBuilder.append(track.getTrackNumber())
                            .append(". ")
                            .append(track.getTrackTitle())
                            .append("\n");
                }
            }
        }

        return infoStringBuilder.toString();
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
