package OutEntities;

import Gears.DirProcessor;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javax.activation.MimetypesFileTypeMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;

public final class Entity {

    private final File dir;

    private ObservableList<Entity> childList; //список потомков

    private ObjectProperty<File> parentDir; //родительская директория
    private ObjectProperty<File> currentDir; //текущая директория

    private ObservableList<AudioEntity> listOfAudioFiles; //список аудиофайлов
    private ObservableList<File> listOfImageFiles; //список изображений
    private ObservableList<File> listOfOtherFiles;

    private final SimpleStringProperty directoryName; //Имя директории
    private final SimpleBooleanProperty audioAttribute; //Наличие аудио
    private final SimpleBooleanProperty multiCDAttribute; //наличие нескольких дисков
    private final SimpleBooleanProperty imageAttribute; //наличие картинок
    private final SimpleBooleanProperty VaAttribute; //признак сборника

    private ObservableMap<String, String> tracklist; //key - trackNumber, value - title
    private ObservableList<String> albumTitle; //size()>1 - наличие нескольких альбомов в одном кластере
    private ObservableList<String> genres; //жанры, встречающиеся в кластере
    private ObservableList<String> artistTitle; //size()>1 - наличие нескольких исполнителей
                                                //size()==2 - сплит
                                                //size()>2 - сборник

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
            System.out.println("Ошибка при добавлении атрибутов");
        }

    }

    // сканирование директории
    // добавление потомков
    public void lookForChildEntities() {
        parentDir = new SimpleObjectProperty<>(this, "parentDir"); //инициализируем родителя при сканировании нового кластера
        parentDir.setValue(dir);
        fillListsOfInnerFiles(dir); //сортируем файлы в родительской папке

        System.out.println(getDirectoryName());
        testLists();

        addChilds();

        System.out.println("-----");
    }
    
    public void collectAudioData(Entity entity){
        if (entity.hasAudioAttribute()) {
            for (AudioEntity ae : entity.getListOfAudioFiles()) {
                //ae.
            }
        }
    }


    private void addChilds() {
        childList = FXCollections.observableArrayList();
        LinkedList<File> childLinkedList = (LinkedList) FileUtils.listFilesAndDirs(parentDir.getValue(),
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        childLinkedList.removeFirst();

        for (File child : childLinkedList) {
            Entity ocd = new Entity(child);
            ocd.setCurrentDir(child);
            ocd.setParentDir(parentDir);
            ocd.fillListsOfInnerFiles(ocd.getCurrentDir().getValue());
            childList.add(ocd);
        }

        removeUselessChilds();

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
                        listOfAudioFiles.add(new AudioEntity(f));
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
            for (AudioEntity file : listOfAudioFiles) {
                System.out.println(file.getFile().getAbsolutePath());
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

    public ObservableList<AudioEntity> getListOfAudioFiles() {
        return listOfAudioFiles;
    }

    public void setListOfAudioFiles(ObservableList<AudioEntity> listOfAudioFiles) {
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
