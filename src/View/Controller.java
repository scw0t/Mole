package View;

import Gears.DirProcessor;
import Gears.FinalProcess;
import Gears.LogOutput;
import Gears.ParseFactory;
import Gears.ParseTask;
import OutEntities.AudioProperties;
import OutEntities.ItemProperties;
import OutEntities.IncomingDirectory;
import OutEntities.Medium;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.controlsfx.control.MasterDetailPane;

public class Controller extends BorderPane {

    private LogOutput logOutput;

    private final String initDirPath = "d:\\Music\\!test2\\";
    public static TextField pathTextArea;
    public static ObservableList<IncomingDirectory> initialDirectoryList;
    public static TextArea infoTextArea;
    public static ItemTableView<ItemModel> tableView;

    public Controller() throws FileNotFoundException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.controlsfx").setLevel(Level.OFF);
        setId("background");

        initialDirectoryList = FXCollections.observableArrayList();

        pathTextArea = new TextField(initDirPath);
        HBox.setHgrow(pathTextArea, Priority.ALWAYS);

        infoTextArea = new TextArea();
        infoTextArea.setMaxHeight(Double.MAX_VALUE);
        infoTextArea.setStyle("-fx-focus-color: transparent;");

        initDragNdropHandler();

        initElements();
    }

    private void initElements() throws FileNotFoundException {
        Button openButton = new Button("Open");
        openButton.getStyleClass().addAll("first");
        openButton.setOnAction((ActionEvent t) -> {
            Thread mainProcessThread = new Thread(new OpenDialogTask());
            mainProcessThread.setDaemon(true);
            mainProcessThread.start();
        });

        Button runButton = new Button("Run");
        runButton.getStyleClass().addAll("last");
        runButton.setOnAction((ActionEvent event) -> {
            Thread mainProcessThread = new Thread(new CDialogTask());
            mainProcessThread.setDaemon(true);
            mainProcessThread.start();
        });

        Button settingsButton = new Button("", new ImageView(new Image(new FileInputStream("settings.png"))));
        settingsButton.getStyleClass().addAll("last");
        settingsButton.setStyle("-fx-focus-color: transparent;"
                + "-fx-background-insets: 0, 0, 1, 2;");

        Region spacer = new Region();
        spacer.getStyleClass().setAll("spacer");

        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().setAll("segmented-button-bar");
        buttonBar.getChildren().addAll(openButton, runButton, pathTextArea);

        //wrapper for =infoTextArea
        VBox textAreaVbox = new VBox();
        textAreaVbox.setPadding(new Insets(10));
        textAreaVbox.setMinHeight(500);
        textAreaVbox.setMaxHeight(Double.MAX_VALUE);
        textAreaVbox.getChildren().add(infoTextArea);
        VBox.setVgrow(textAreaVbox, Priority.ALWAYS);
        VBox.setVgrow(infoTextArea, Priority.ALWAYS);

        tableView = new ItemTableView<ItemModel>();

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

        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemModel>() {
            public void changed(ObservableValue<? extends ItemModel> observable,
                    ItemModel oldValue, ItemModel newValue) {
                try {
                    infoTextArea.clear();
                    ItemProperties selectedRelease = tableView.getSelectionModel().getSelectedItem().getItemProperty();
                    infoTextArea.setText(buildReleaseInfo(selectedRelease));
                } catch (NullPointerException e) {
                    //System.out.println("something happend");
                }
            }
        });

        tableView.setOnKeyPressed((KeyEvent k) -> {
            try {
                if (k.getCode().equals(KeyCode.SPACE) || k.getCode().equals(KeyCode.ENTER)) {
                    if (tableView.getSelectionModel().getSelectedItem().isChecked()) {
                        tableView.getSelectionModel().getSelectedItem().setCheck(false);
                    } else {
                        tableView.getSelectionModel().getSelectedItem().setCheck(true);
                    }
                }

                if (k.getCode().equals(KeyCode.DELETE)) {
                    tableView.getItems().remove(tableView.getSelectionModel().getSelectedItem());
                }
            } catch (NullPointerException e) {
                System.out.println("something happend");
            }
        });

    }

    private String buildReleaseInfo(ItemProperties rootItem) {
        StringBuilder infoStringBuilder = new StringBuilder();
        if (rootItem.getCdN() > 0) {
            infoStringBuilder.append(rootItem.getDirectoryName()).append("\n");

            for (ItemProperties childCD : rootItem.getChildList()) {
                for (Medium childMedium : childCD.getMediumList()) {
                    infoStringBuilder.append("#CD")
                            .append(childMedium.getCdN())
                            .append("\n");
                    for (AudioProperties track : childMedium.getAudioList()) {
                        infoStringBuilder.append(track.getTrackNumber())
                                .append(". ")
                                .append(track.getTrackTitle())
                                .append("\n");
                    }
                }
            }
        } else {
            for (Medium medium : rootItem.getMediumList()) {
                infoStringBuilder.append(medium.getArtist())
                        .append(" - ")
                        .append(medium.getAlbum())
                        .append(" (")
                        .append(medium.getYear())
                        .append(")\n");
                for (AudioProperties track : medium.getAudioList()) {
                    infoStringBuilder.append(track.getTrackNumber())
                            .append(". ")
                            .append(track.getTrackTitle())
                            .append("\n");
                }
            }
        }

        return infoStringBuilder.toString();
    }

    private void initDragNdropHandler() {
        setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.LINK);
            } else {
                event.consume();
            }
        });

        setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                if (!initialDirectoryList.isEmpty()) {
                    initialDirectoryList.clear();
                }
                success = true;
                for (File file : db.getFiles()) {
                    if (file.isDirectory()) {
                        LinkedList<File> folderList = (LinkedList) FileUtils.listFilesAndDirs(file,
                                new NotFileFilter(TrueFileFilter.INSTANCE),
                                DirectoryFileFilter.DIRECTORY);
                        for (File folder : folderList) {
                            initialDirectoryList.add(new IncomingDirectory(folder));
                        }
                    }
                }
            }
            event.setDropCompleted(success);
            if (event.isDropCompleted()) {
                Thread mainProcessThread = new Thread(new ScanTask());
                mainProcessThread.setDaemon(true);
                mainProcessThread.start();
            }
            event.consume();
        });

    }

    class ScanTask extends Task {

        @Override
        protected Object call() throws Exception {
            Platform.runLater(() -> {
                DirProcessor processor = new DirProcessor();
                processor.go();
                tableView.getSelectionModel().select(0);
            });
            return null;
        }
    }

    public static void runAndWait(Runnable action) {
        if (action == null) {
            throw new NullPointerException("action");
        }

        // run synchronously on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        // queue on JavaFX thread and wait for completion
        final CountDownLatch doneLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                doneLatch.countDown();
            }
        });

        try {
            doneLatch.await();
        } catch (InterruptedException e) {
            // ignore exception
        }
    }

    class OpenDialogTask extends Task {

        @Override
        protected Object call() throws Exception {
            Platform.runLater(() -> {
                DirectoryTreeView treeView = new DirectoryTreeView();
                try {
                    initialDirectoryList.clear();
                    treeView.drawGUI();
                } catch (IOException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            return null;
        }
    }

    class CDialogTask extends Task {

        @Override
        protected Object call() throws Exception {

            Platform.runLater(() -> {
                if (!tableView.getItems().isEmpty()) {
                    for (ItemModel item : tableView.getItems()) {
                        if (item.isChecked()) {
                            ParseFactory parseFactory = new ParseFactory(item.getItemProperty());
                            FinalProcess finalProcess = new FinalProcess(item.getItemProperty());
                            finalProcess.setRymp(parseFactory.getRymp());
                            
                            ParseTask parseTask = new ParseTask();
                            parseTask.setParseFactory(parseFactory);

                            CListView cListView = new CListView();
                            cListView.setFinalProcess(finalProcess);
                            cListView.getcTable().itemsProperty().bind(parseTask.valueProperty());
                            cListView.getIndicator().progressProperty().bind(parseTask.progressProperty());
                            cListView.getStateLabel().textProperty().bind(parseTask.messageProperty());
                            cListView.getAlbumTextField().textProperty().bindBidirectional(parseTask.getAlbumProperty());
                            cListView.getArtistTextField().textProperty().bindBidirectional(parseTask.getArtistProperty());
                            cListView.getTypeTextField().textProperty().bindBidirectional(parseTask.getTypeProperty());
                            
                            new Thread(parseTask).start();
                            cListView.showAndWait();
                        }
                    }
                }
            });

            return null;
        }
    }
}
