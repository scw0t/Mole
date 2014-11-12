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
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public final class Entity {

    //private SmartDirectory parent; //родительская директория
    private ObservableList<Entity> childList; //список потомков

    private final File dir;
    private SimpleBooleanProperty parent;
    private ObjectProperty<File> parentDir; //родительская директория
    private ObjectProperty<File> currentDir; //текущая директория

    private ObservableList<File> listOfAudioFiles;
    private ObservableList<File> listOfImageFiles;
    private ObservableList<File> listOfOtherFiles;

    private final SimpleStringProperty directoryName; //Имя директории
    private final SimpleBooleanProperty audioAttribute; //Наличие аудио
    private final SimpleBooleanProperty multiCDAttribute; //наличие нескольких дисков
    private final SimpleBooleanProperty imageAttribute; //наличие картинок
    private final SimpleBooleanProperty VaAttribute; //признак сборника

    //принимает директорию из parsedDirList
    public Entity(File dir) {
        this.dir = dir;
        currentDir = new SimpleObjectProperty(this, "currentDir");
        directoryName = new SimpleStringProperty(this, "directoryName");
        audioAttribute = new SimpleBooleanProperty(this, "audioAttribute");
        multiCDAttribute = new SimpleBooleanProperty(this, "multiCDAttribute");
        imageAttribute = new SimpleBooleanProperty(this, "imageAttribute");
        VaAttribute = new SimpleBooleanProperty(this, "VaAttribute");

        listOfAudioFiles = FXCollections.observableArrayList();
        listOfImageFiles = FXCollections.observableArrayList();
        listOfOtherFiles = FXCollections.observableArrayList();

        directoryName.setValue(dir.getName());

        try {
            setAudioAttribute(DirProcessor.hasAudio(dir));
            setImageAttribute(DirProcessor.hasImages(dir));
            setMultiCDAttribute(DirProcessor.hasMultiCD(dir));
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    // сканирование директории
    // добавление потомков
    public void lookForChildEntities() {
        parentDir = new SimpleObjectProperty<>(this, "parentDir");
        parentDir.setValue(dir);

        //System.out.println(parentDir.getValue().getName());
        fillListsOfInnerFiles(dir);
        childList = FXCollections.observableArrayList();
        LinkedList<File> childLinkedList = (LinkedList) FileUtils.listFilesAndDirs(parentDir.getValue(),
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        childLinkedList.removeFirst();

        for (File child : childLinkedList) {
            try {
                Entity ocd = new Entity(child);
                ocd.setCurrentDir(child);
                ocd.setParentDir(parentDir);
                ocd.setAudioAttribute(DirProcessor.hasAudio(child));
                ocd.setImageAttribute(DirProcessor.hasImages(child));
                ocd.setMultiCDAttribute(DirProcessor.hasMultiCD(child));
                childList.add(ocd);
                //System.out.print("└-");
                ocd.fillListsOfInnerFiles(ocd.getCurrentDir().getValue());
            } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
        removeUselessChilds();
        
        System.out.println(getDirectoryName());
        testLists();
        
        for (Entity child : childList) {
            child.fillListsOfInnerFiles(child.getCurrentDir().getValue());
            System.out.print("└-");
            System.out.println(child.getCurrentDir().getValue().getName());
            child.testLists();
        }

        
        
    }

    public boolean removeUselessChilds() {
        if (hasAudioAttribute()) {
            //System.out.println("Parent: + | " + getParentDir().getValue().getAbsolutePath());
        } else {
            //System.out.println("Parent: - | " + getParentDir().getValue().getAbsolutePath());
        }
        for (int i = 0; i < childList.size(); i++) {
            if (childList.get(i).hasAudioAttribute()) {
                //System.out.println("Child: A+ | " + childList.get(i).getCurrentDir().getValue().getAbsolutePath());
            } else if (childList.get(i).hasImageAttribute()) {
                //System.out.println("Child: I+ | " + childList.get(i).getCurrentDir().getValue().getAbsolutePath());
            } else {
                childList.remove(i);
                //System.out.println("Child: - | " + child.getCurrentDir().getValue().getAbsolutePath());
            }
        }

        //System.out.println("-----------------");
        return true;
    }

    public void fillListsOfInnerFiles(File dir) {
        //System.out.println(dir.getName());
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (isAudio(f)) {
                        listOfAudioFiles.add(f);
                    } else if (isImage(f)) {
                        listOfImageFiles.add(f);
                    } else {
                        listOfOtherFiles.add(f);
                    }
                }
            }
        }

    }

    private void testLists() {
        if (!listOfAudioFiles.isEmpty()) {
            System.out.println("--Audio:");
            for (File file : listOfAudioFiles) {
                System.out.println(file.getAbsolutePath());
            }
        }

        if (!listOfImageFiles.isEmpty()) {
            System.out.println("--Images:");
            for (File file : listOfImageFiles) {
                System.out.println(file.getAbsolutePath());
            }
        }

        if (!listOfOtherFiles.isEmpty()) {
            System.out.println("--Others:");
            for (File file : listOfOtherFiles) {
                System.out.println(file.getAbsolutePath());
            }
            
        }
        System.out.println("-----");
    }

    private boolean isAudio(File file) {
        return FilenameUtils.getExtension(file.toString()).toLowerCase().equals("mp3");
    }

    private boolean isImage(File file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        String type = mimetype.split("/")[0];
        return type.equals("image");
    }

    public String getDirectoryName() {
        return directoryName.getValue();
    }

    public boolean hasAudioAttribute() {
        return audioAttribute.getValue();
    }

    public boolean hasMultiCDAttribute() {
        return multiCDAttribute.getValue();
    }

    public boolean hasImageAttribute() {
        return imageAttribute.getValue();
    }

    public boolean hasVaAttribute() {
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

    public ObservableList<Entity> getChildList() {
        return childList;
    }

    public ObservableList<File> getListOfAudioFiles() {
        return listOfAudioFiles;
    }

    public void setListOfAudioFiles(ObservableList<File> listOfAudioFiles) {
        this.listOfAudioFiles = listOfAudioFiles;
    }

    public ObservableList<File> getListOfImageFiles() {
        return listOfImageFiles;
    }

    public void setListOfImageFiles(ObservableList<File> listOfImageFiles) {
        this.listOfImageFiles = listOfImageFiles;
    }

    public ObservableList<File> getListOfOtherFiles() {
        return listOfOtherFiles;
    }

    public void setListOfOtherFiles(ObservableList<File> listOfOtherFiles) {
        this.listOfOtherFiles = listOfOtherFiles;
    }

    public ObjectProperty<File> getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File dir) {
        this.currentDir.set(dir);
    }

}
