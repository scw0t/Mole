package View;

import Gears.LogOutput;
import OutEntities.IncomingDirectory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MasterDetailPane;

public class MainGUI extends BorderPane {

    private LogOutput logOutput;

    private final String initDirPath = "G:\\test";
    private TextField pathTextArea;
    public static ObservableList<IncomingDirectory> initialDirectoryList;
    TextArea logTextArea;

    public MainGUI() throws FileNotFoundException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.controlsfx").setLevel(Level.OFF);
        setId("background");

        pathTextArea = new TextField(initDirPath);
        HBox.setHgrow(pathTextArea, Priority.ALWAYS);

        logTextArea = new TextArea();
        logTextArea.setMaxHeight(Double.MAX_VALUE);

        initialDirectoryList = FXCollections.observableArrayList();

        initElements();
    }

    private void initElements() throws FileNotFoundException {
        Button openButton = new Button("Open");
        openButton.getStyleClass().addAll("first");
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DirTreeView treeView = new DirTreeView();
                try {
                    initialDirectoryList.clear();
                    treeView.drawGUI();
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        Button runButton = new Button("Run");
        runButton.getStyleClass().addAll("last");
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread mainProcessThread = new Thread(new MainTask());
                mainProcessThread.setDaemon(true);
                mainProcessThread.start();
            }
        });

        Button settingsButton = new Button("", new ImageView(new Image(new FileInputStream("settings.png"))));
        settingsButton.getStyleClass().addAll("last");

        Region spacer = new Region();
        spacer.getStyleClass().setAll("spacer");

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().setAll("segmented-button-bar");
        buttonBar.getChildren().addAll(openButton, runButton, pathTextArea);

        ToolBar toolbar = new ToolBar();
        setTop(toolbar);
        toolbar.getItems().addAll(spacer, buttonBar, pathTextArea, settingsButton);

        //wrapper for =logTextArea
        VBox textAreaVbox = new VBox();
        textAreaVbox.setPadding(new Insets(10));
        textAreaVbox.setMaxHeight(Double.MAX_VALUE);
        textAreaVbox.getChildren().add(logTextArea);
        VBox.setVgrow(textAreaVbox, Priority.ALWAYS);
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

        CustomTableView tableView = new CustomTableView();

        //wrapper for =tableView
        VBox tableViewVbox = new VBox();
        tableViewVbox.setPadding(new Insets(10));
        tableViewVbox.setMaxHeight(Double.MAX_VALUE);
        tableViewVbox.getChildren().add(tableView);
        VBox.setVgrow(tableViewVbox, Priority.ALWAYS);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        MasterDetailPane pane = new MasterDetailPane();
        pane.setMasterNode(tableViewVbox);
        pane.setDetailNode(textAreaVbox);
        pane.setDetailSide(Side.BOTTOM);
        pane.setShowDetailNode(true);

        setCenter(pane);
        //setBottom(textAreaVbox);
    }

    class CustomTableView extends TableView {

        public CustomTableView() {

        }

    }

    class MainTask extends Task {

        @Override
        protected Object call() throws Exception {
            /*DirProcessor dp = new DirProcessor();
            dp.init();*/

            return null;
        }
    }

}
