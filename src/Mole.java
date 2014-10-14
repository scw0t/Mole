
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public class Mole extends Application {

    private final String initDir = "G:\\test";
    static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        Button btn = new Button();
        btn.setText("Run");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    LogOutput logOutput = new LogOutput();
                    Process process = new Process(new File(initDir));
                    process.setLogOutput(logOutput);
                    process.init();
                    
                } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                    Logger.getLogger(Mole.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        
        primaryStage.show();
        this.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
