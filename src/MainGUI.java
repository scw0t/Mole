import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainGUI extends BorderPane {

    private LogOutput logOutput;

    private final String initDirPath = "G:\\test";
    private TextField pathTextArea;
    static ArrayList<File> dirList;
    TextArea textArea;

    public MainGUI() {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        setId("background");
        
        pathTextArea = new TextField(initDirPath);
        HBox.setHgrow(pathTextArea, Priority.ALWAYS);
        
        textArea = new TextArea();
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        dirList = new ArrayList<>();
        
        initElements();
    }

    private void initElements() {
        Button openButton = new Button("Open");
        openButton.getStyleClass().addAll("first");
        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                DirTreeView treeView = new DirTreeView();
                try {
                    treeView.drawGUI();
                } catch (IOException ex) {
                    Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        Button runButton = new Button("Run");
        runButton.getStyleClass().addAll("last");
        //runButton.setPrefSize(10, 10);
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread mainProcessThread = new Thread(new MainTask());
                mainProcessThread.setDaemon(true);
                mainProcessThread.start();
            }
        });

        Region spacer = new Region();
        spacer.getStyleClass().setAll("spacer");
        
        HBox buttonBar = new HBox();
        buttonBar.getStyleClass().setAll("segmented-button-bar");
        buttonBar.getChildren().addAll(openButton, runButton, pathTextArea);
        
        ToolBar toolbar = new ToolBar();
        setTop(toolbar);
        toolbar.getItems().addAll(spacer, buttonBar, pathTextArea);
        
        VBox tb = new VBox();
        tb.setPadding(new Insets(10));
        tb.setMaxHeight(Double.MAX_VALUE);
        tb.getChildren().add(textArea);
        //tb.setAlignment(Pos.CENTER);
        VBox.setVgrow(tb, Priority.ALWAYS);
        
        
        VBox vBox = new VBox();
        vBox.getChildren().addAll(toolbar, tb);
        VBox.setVgrow(vBox, Priority.ALWAYS);
        vBox.prefHeightProperty().bind(heightProperty());
        setCenter(vBox);
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

    class MainTask extends Task {

        @Override
        protected Object call() throws Exception {
            File initDir = new File(pathTextArea.getText());
            try {
                if (initDir.exists()) {
                    /*DirWorker dirWorker = new DirWorker(initDir);
                     dirWorker.setLogOutput(logOutput);
                     dirWorker.searchDirs();*/

                    DirProcessor dp = new DirProcessor(initDir);
                    dp.init();
                } else {
                    System.out.println(initDir.getAbsolutePath() + " does not exists");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

}
