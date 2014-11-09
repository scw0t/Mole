package OutEntities;

import java.io.File;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class IncomingDirectory {
    
    private final ObjectProperty<File> currentDirectory;

    public IncomingDirectory(File directory) {
        currentDirectory = new SimpleObjectProperty<>(this, "dir");
        currentDirectory.set(directory);
    }

    public final ObjectProperty<File> currentDirectoryProperty() {
        return currentDirectory;
    }
    
    public final File getValue(){
        return currentDirectory.getValue();
    }
    
}
