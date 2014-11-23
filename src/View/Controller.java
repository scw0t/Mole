package View;

import Entities.Artist;
import Entities.Issue;
import Entities.Record;
import Gears.DirProcessor;
import Gears.FinalProcess;
import Gears.LogOutput;
import Gears.TagProcessor;
import OutEntities.AudioProperties;
import OutEntities.ItemProperties;
import OutEntities.IncomingDirectory;
import OutEntities.Medium;
import View.CListView.CModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
import javafx.event.EventHandler;
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

    private final String initDirPath = "I:\\Music\\!test\\";
    public static TextField pathTextArea;
    public static ObservableList<IncomingDirectory> initialDirectoryList;
    public static TextArea logTextArea;
    public static ItemTableView<ItemModel> tableView;
    public ObservableList<Record> testRecList;

    public Controller() throws FileNotFoundException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Logger.getLogger("org.controlsfx").setLevel(Level.OFF);
        setId("background");

        initialDirectoryList = FXCollections.observableArrayList();

        pathTextArea = new TextField(initDirPath);
        HBox.setHgrow(pathTextArea, Priority.ALWAYS);

        logTextArea = new TextArea();
        logTextArea.setMaxHeight(Double.MAX_VALUE);
        logTextArea.setStyle("-fx-focus-color: transparent;");

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

        //wrapper for =logTextArea
        VBox textAreaVbox = new VBox();
        textAreaVbox.setPadding(new Insets(10));
        textAreaVbox.setMinHeight(500);
        textAreaVbox.setMaxHeight(Double.MAX_VALUE);
        textAreaVbox.getChildren().add(logTextArea);
        VBox.setVgrow(textAreaVbox, Priority.ALWAYS);
        VBox.setVgrow(logTextArea, Priority.ALWAYS);

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
                    logTextArea.clear();
                    ItemProperties selectedRelease = tableView.getSelectionModel().getSelectedItem().getItemProperty();
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

        createTestRecordList();
    }

    private String buildReleaseInfo(ItemProperties fp) {
        StringBuilder infoStringBuilder = new StringBuilder();
        if (fp.getCdN() > 0) {
            infoStringBuilder.append(fp.getDirectoryName()).append("\n");

            for (ItemProperties childCD : fp.getChildList()) {
                for (Medium childMedium : childCD.getMediumList()) {
                    infoStringBuilder.append("#CD")
                            .append(childMedium.getCdN())
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
                        .append(medium.getAlbum())
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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    DirProcessor processor = new DirProcessor();
                    processor.go();
                    //tableView.getSelectionModel().clearSelection();
                    tableView.getSelectionModel().select(0);
                }
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
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    DirectoryTreeView treeView = new DirectoryTreeView();
                    try {
                        initialDirectoryList.clear();
                        treeView.drawGUI();
                    } catch (IOException ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
                            TagProcessor tagProcessor = new TagProcessor(item.getItemProperty());
                            FinalProcess finalProcess = new FinalProcess(item.getItemProperty());
                            finalProcess.setRymp(tagProcessor.getRymp());

                            ParserTask parserTask = new ParserTask();
                            parserTask.setTagProcessor(tagProcessor);

                            CListView cListView = new CListView();
                            cListView.setFinalProcess(finalProcess);
                            cListView.getcTable().itemsProperty().bind(parserTask.valueProperty());
                            cListView.getIndicator().progressProperty().bind(parserTask.progressProperty());
                            cListView.getStateLabel().textProperty().bind(parserTask.messageProperty());

                            //runAndWait(parserTask);
                            new Thread(parserTask).start();
                            cListView.showAndWait();
                        }
                    }
                }

                /*if (!testRecList.isEmpty() && testRecList != null) {
                 for (Record rec : testRecList) {
                 CListView cListView = new CListView();
                 cListView.setContent(rec.getIssues());
                 cListView.showAndWait();
                 if (cListView.isTerminated()) {
                 break;
                 }
                 }
                 } else {
                 System.out.println("trl = null");
                 }*/
            });

            return null;
        }
    }

    class ParserTask extends Task<ObservableList<CModel>> {

        private TagProcessor tagProcessor;

        @Override
        protected ObservableList<CModel> call() throws Exception {
            ObservableList<CModel> resultList = FXCollections.observableArrayList();

            tagProcessor.getMessageProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                updateMessage(newValue);
            });

            try {
                tagProcessor.launch();
                if (tagProcessor.getIssueList().isEmpty()) {
                    updateMessage("Nothing founded");
                } else {
                    updateMessage("Searching complete");
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                updateProgress(1.0f, 1.0f);
            }

            for (Issue issue : tagProcessor.getIssueList()) {
                resultList.add(new CModel(issue));
            }

            return resultList;
        }

        public void setTagProcessor(TagProcessor tagProcessor) {
            this.tagProcessor = tagProcessor;
        }

    }

    private void createTestRecordList() {
        Artist a1 = new Artist("The Who");
        a1.setCountry("UK");
        Artist a2 = new Artist("Jimi hendrix");
        a2.setCountry("USA");

        Issue i1 = new Issue();
        i1.setIssueTitle("Issue1");
        i1.setIssueAttributes("CD, Remastered");
        i1.setIssueCountries("USA");
        i1.setIssueYear("1968");
        i1.setIssueLabel("decca");
        i1.setCatNumber("I123");

        Issue i2 = new Issue();
        i2.setIssueTitle("Issue2");
        i2.setIssueAttributes("Vinil");
        i2.setIssueCountries("Belgium");
        i2.setIssueYear("1970");
        i2.setIssueLabel("RCA");
        i2.setCatNumber("rc64-541");

        Issue i3 = new Issue();
        i3.setIssueTitle("Issue3");
        i3.setIssueAttributes("SDCD");
        i3.setIssueCountries("Zambia");
        i3.setIssueYear("1971");
        i3.setIssueLabel("radioactive");
        i3.setCatNumber("RA-1542");

        Issue i4 = new Issue();
        i4.setIssueTitle("Issue4");
        i4.setIssueAttributes("Vinil 12''");
        i4.setIssueCountries("Russia");
        i4.setIssueYear("1975");
        i4.setIssueLabel("EMI");
        i4.setCatNumber("8-4546-451-474");

        ArrayList<Issue> issueList1 = new ArrayList<>();
        issueList1.add(i1);
        issueList1.add(i2);

        ArrayList<Issue> issueList2 = new ArrayList<>();
        issueList2.add(i3);
        issueList2.add(i4);

        Record r1 = new Record("My Generation");
        r1.setArtist(a1);
        r1.setIssues(issueList1);

        Record r2 = new Record("Experience");
        r2.setArtist(a2);
        r2.setIssues(issueList2);

        testRecList = FXCollections.observableArrayList();
        testRecList.add(r1);
        testRecList.add(r2);

    }

}
