package OutEntities;

import Gears.DirProcessor;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

//Сущность, систематизирующая папки релиза, предназначенные для обработки и исключающая из нее лишние файлов 
public final class FileProperties {

    private final File dir;

    private int CDn;
    private ObservableList<Medium> mediumList;
    private ObservableList<FileProperties> childList; //список потомков

    private ObjectProperty<File> parentDir; //родительская директория
    private ObjectProperty<File> currentDir; //текущая директория

    private ObservableList<AudioProperties> listOfAudioFiles; //список аудиофайлов
    private ObservableList<File> listOfImageFiles; //список изображений
    private ObservableList<File> listOfOtherFiles; //список других файлов

    private final SimpleStringProperty directoryName; //Имя директории
    private final SimpleBooleanProperty audioAttribute; //Наличие аудио
    private final SimpleIntegerProperty numOfCD; //наличие нескольких дисков
    private final SimpleBooleanProperty imageAttribute; //наличие картинок
    private final SimpleBooleanProperty VaAttribute; //признак сборника

    //принимает директорию из parsedDirList
    public FileProperties(File dir) {
        this.dir = dir;

        CDn = 0;
        currentDir = new SimpleObjectProperty(this, "currentDir");
        directoryName = new SimpleStringProperty(this, "directoryName");
        audioAttribute = new SimpleBooleanProperty(this, "audioAttribute");
        numOfCD = new SimpleIntegerProperty(this, "numOfCD");
        imageAttribute = new SimpleBooleanProperty(this, "imageAttribute");
        VaAttribute = new SimpleBooleanProperty(this, "VaAttribute");

        mediumList = FXCollections.observableArrayList();
        listOfAudioFiles = FXCollections.observableArrayList();
        listOfImageFiles = FXCollections.observableArrayList();
        listOfOtherFiles = FXCollections.observableArrayList();

        directoryName.setValue(dir.getName());

        try {
            setAudioAttribute(DirProcessor.hasAudio(dir));
            setImageAttribute(DirProcessor.hasImages(dir));
            setNumOfCD(DirProcessor.numberOfCD(dir));
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
            System.out.println("Ошибка при добавлении атрибутов");
        }

    }

    // сканирование директории
    // добавление потомков
    public void lookForChildEntities() {
        parentDir = new SimpleObjectProperty<>(this, "parentDir"); //инициализируем родителя при сканировании нового кластера
        parentDir.setValue(dir);
        currentDir.setValue(dir);
        fillListsOfInnerFiles(this); //сортируем файлы в родительской папке

        System.out.println(getDirectoryName());
        testLists();

        addChilds();

        System.out.println("-----");
    }

    private void addChilds() {
        childList = FXCollections.observableArrayList();
        LinkedList<File> childLinkedList = (LinkedList) FileUtils.listFilesAndDirs(parentDir.getValue(),
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        childLinkedList.removeFirst();

        int cdNotProcessed = getCDn();

        for (File child : childLinkedList) {
            FileProperties ocd = new FileProperties(child);
            ocd.setParentDir(parentDir);
            ocd.setCurrentDir(child);
            if (getCDn() > 0) {
                cdNotProcessed--;
                CDn = getCDn() - cdNotProcessed;
            }
            ocd.fillListsOfInnerFiles(ocd);
            childList.add(ocd);
        }

        removeUselessChilds();

        //?
        /*for (FileProperties child : childList) {
         child.fillListsOfInnerFiles(child.getCurrentDir().getValue());
         System.out.print("└-");
         System.out.println(child.getCurrentDir().getValue().getName());
         child.testLists();
         }*/
    }

    public void fillListsOfInnerFiles(FileProperties fp) {
        //System.out.println(dir.getName());
        File currDirectory = fp.getCurrentDir().getValue();
        int audioNum = 0;
        if (currDirectory.isDirectory()) {
            File[] files = currDirectory.listFiles();
            for (File f : files) {
                if (f.isFile()) {
                    if (isAudio(f)) {
                        listOfAudioFiles.add(new AudioProperties(f, ++audioNum));
                    } else if (isImage(f)) {
                        listOfImageFiles.add(f);
                    } else {
                        listOfOtherFiles.add(f);
                    }
                }
            }

            if (!fp.listOfAudioFiles.isEmpty()) {
                Medium medium = new Medium(fp.listOfAudioFiles);
                medium.setCDn(CDn);
                medium.look();
                mediumList.add(medium);
            }
        }
    }

    private boolean removeUselessChilds() {
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

    private void testLists() {
        if (!listOfAudioFiles.isEmpty()) {
            System.out.println("--Audio:");
            for (AudioProperties file : listOfAudioFiles) {
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

    public int getCDn() {
        return numOfCD.getValue();
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

    public void setNumOfCD(int numOfCD) {
        this.numOfCD.setValue(numOfCD);
    }

    public void setImageAttribute(boolean isImage) {
        this.imageAttribute.setValue(isImage);
    }

    public void setVaAttribute(boolean isVa) {
        this.VaAttribute.setValue(isVa);
    }

    public ObservableList<FileProperties> getChildList() {
        return childList;
    }

    public ObservableList<AudioProperties> getListOfAudioFiles() {
        return listOfAudioFiles;
    }

    public void setListOfAudioFiles(ObservableList<AudioProperties> listOfAudioFiles) {
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

    public ObservableList<Medium> getMediumList() {
        return mediumList;
    }

}
