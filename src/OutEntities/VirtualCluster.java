package OutEntities;

import Gears.DirProcessor;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public class VirtualCluster {
    
    //private SmartDirectory parent; //родительская директория
    private ObservableList<VirtualCluster> childList; //список потомков
    
    private final File dir;
    private SimpleBooleanProperty parent;
    private ObjectProperty<File> parentDir; //родительская директория
    private ObjectProperty<File> currentDir; //текущая директория
    private final SimpleStringProperty directoryName; //Имя директории
    private final SimpleBooleanProperty audioAttribute; //Наличие аудио
    private final SimpleBooleanProperty multiCDAttribute; //наличие нескольких дисков
    private final SimpleBooleanProperty imageAttribute; //наличие картинок
    private final SimpleBooleanProperty VaAttribute; //признак сборника

    //принимает директорию из parsedDirList
    public VirtualCluster(File dir) {
        this.dir = dir;
        directoryName = new SimpleStringProperty(this, "directoryName");
        audioAttribute = new SimpleBooleanProperty(this, "audioAttribute");
        multiCDAttribute = new SimpleBooleanProperty(this, "multiCDAttribute");
        imageAttribute = new SimpleBooleanProperty(this, "imageAttribute");
        VaAttribute = new SimpleBooleanProperty(this, "VaAttribute");
        
        directoryName.setValue(dir.getName());
        //this.parentDir = new SimpleObjectProperty<>(this, "thisDir");
        
        //this.parentDir.setValue(dir);
        
    }
    
    // сканирование директории
    // добавление потомков
    public void lookUp(){
        parentDir = new SimpleObjectProperty<>(this, "parentDir");
        parentDir.setValue(dir); 
        childList = FXCollections.observableArrayList();
        LinkedList<File> childLinkedList = (LinkedList) FileUtils.listFilesAndDirs(parentDir.getValue(),
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        childLinkedList.removeFirst();
        System.out.println(parentDir.getValue().getName());
        for (File child : childLinkedList) {
            try {
                VirtualCluster ocd = new VirtualCluster(child);
                ocd.setAudioAttribute(DirProcessor.hasAudio(child));
                ocd.setImageAttribute(DirProcessor.hasImages(child));
                ocd.setMultiCDAttribute(DirProcessor.hasMultiCD(child));
                childList.add(ocd);
                System.out.println("└-" + ocd.getDirectoryName());
            } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                Logger.getLogger(VirtualCluster.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public String getDirectoryName() {
        return directoryName.getValue();
    }

    public boolean getAudioAttribute() {
        return audioAttribute.getValue();
    }

    public boolean getMultiCDAttribute() {
        return multiCDAttribute.getValue();
    }

    public boolean getImageAttribute() {
        return imageAttribute.getValue();
    }

    public boolean getVaAttribute() {
        return VaAttribute.getValue();
    }

    public boolean isParent() {
        return parent.getValue();
    }

    public void setParent(boolean parent) {
        this.parent.setValue(parent);
    }

    public ObjectProperty<File> getParentDir() {
        return parentDir;
    }

    public void setParentDir(ObjectProperty<File> parentDir) {
        this.parentDir = parentDir;
    }

    public void setAudioAttribute(boolean isAudio) {
        this.audioAttribute.setValue(isAudio);
    }

    public void setMultiCDAttribute(boolean isMultiCD) {
        this.multiCDAttribute.setValue(isMultiCD);
    }

    public void setImageAttribute(boolean isImage) {
        this.imageAttribute.setValue(isImage);
    }

    public void setVaAttribute(boolean isVa) {
        this.VaAttribute.setValue(isVa);
    }
    
    
}
