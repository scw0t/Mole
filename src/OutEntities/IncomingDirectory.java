package OutEntities;

import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class IncomingDirectory {
    
    private final ObjectProperty<File> currentDirectory;

    /**
     *
     * @param directory
     */
    public IncomingDirectory(File directory) {
        currentDirectory = new SimpleObjectProperty<>(this, "dir");
        currentDirectory.set(directory);
    }

    /**
     *
     * @return
     */
    public ObjectProperty<File> currentDirectoryProperty() {
        return currentDirectory;
    }
    
    /**
     *
     * @return
     */
    public File getValue(){
        return currentDirectory.getValue();
    }
}
