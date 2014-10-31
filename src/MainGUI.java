import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javax.swing.filechooser.FileSystemView;

public class MainGUI extends VBox {

    private LogOutput logOutput;

    private final String initDirPath = "G:\\test";
    private TextField pathTextArea;

    public MainGUI() {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        init();
    }

    private void init() {
        setPadding(new Insets(10));
        setSpacing(10);
        pathTextArea = new TextField(initDirPath);
        Button runButton = new Button("Run");
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Thread mainProcessThread = new Thread(new MainTask());
                mainProcessThread.setDaemon(true);
                mainProcessThread.start();
            }
        });

        Button openButton = new Button("Open");
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
        getChildren().addAll(openButton, runButton, pathTextArea);
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
