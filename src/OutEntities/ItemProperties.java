package OutEntities;

import static Gears.DirProcessor.hasAudio;
import static Gears.DirProcessor.hasImages;
import static Gears.DirProcessor.numberOfCD;
import java.io.File;
import java.io.IOException;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.lang.System.out;
import java.util.LinkedList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import static javafx.collections.FXCollections.observableArrayList;
import javafx.collections.ObservableList;
import javax.activation.MimetypesFileTypeMap;
import static org.apache.commons.io.FileUtils.listFilesAndDirs;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.filefilter.DirectoryFileFilter.DIRECTORY;
import org.apache.commons.io.filefilter.NotFileFilter;
import static org.apache.commons.io.filefilter.TrueFileFilter.INSTANCE;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

//Сущность, систематизирующая папки релиза, предназначенные для обработки и исключающая из нее лишние файлов 
public class ItemProperties {

    private File dir;

    private int cdN;
    private ObservableList<Medium> mediumList;
    private ObservableList<ItemProperties> childList; //список потомков

    private ObjectProperty<File> parentDir; //родительская директория
    private ObjectProperty<File> currentDir; //текущая директория

    private ObservableList<AudioProperties> listOfAudioFiles; //список аудиофайлов
    private ObservableList<File> listOfImageFiles; //список изображений
    private ObservableList<File> listOfOtherFiles; //список других файлов

    private SimpleStringProperty directoryName; //Имя директории
    private SimpleBooleanProperty audioAttribute; //Наличие аудио
    private SimpleIntegerProperty cdNAttribute; //наличие нескольких дисков
    private SimpleBooleanProperty imageAttribute; //наличие картинок
    private SimpleBooleanProperty vaAttribute; //признак сборника
    private SimpleBooleanProperty innerDirectoryAttribute; //наличие поддиректории с изображениями
    private ObservableList<File> listOfInnerDirectories;

    private ItemProperties item;

    /**
     *
     * @param dir
     */
    public ItemProperties(File dir) {
        this.dir = dir;

        cdN = 0;
        currentDir = new SimpleObjectProperty(this, "currentDir");
        directoryName = new SimpleStringProperty(this, "directoryName");
        audioAttribute = new SimpleBooleanProperty(this, "audioAttribute");
        cdNAttribute = new SimpleIntegerProperty(this, "cdN");
        imageAttribute = new SimpleBooleanProperty(this, "imageAttribute");
        vaAttribute = new SimpleBooleanProperty(this, "vaAttribute");
        vaAttribute.setValue(FALSE);
        innerDirectoryAttribute = new SimpleBooleanProperty(this, "innerDirectoryAttribute");
        innerDirectoryAttribute.setValue(FALSE);

        mediumList = observableArrayList();
        listOfAudioFiles = observableArrayList();
        listOfImageFiles = observableArrayList();
        listOfOtherFiles = observableArrayList();
        listOfInnerDirectories = observableArrayList();

        directoryName.setValue(dir.getName());

        try {
            setAudioAttribute(hasAudio(dir));
            setImageAttribute(hasImages(dir));
            setNumOfCD(numberOfCD(dir));
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
        }
    }

    public ItemProperties(ItemProperties root) {
        dir = root.getDir();
        cdN = root.getCdNPropertyValue();

        parentDir = root.getParentDir();
        currentDir = root.getCurrentDir();
        directoryName = root.getDirectoryNameProperty();
        audioAttribute = root.getAudioAttributeProperty();
        cdNAttribute = root.getCdNProperty();
        imageAttribute = root.getImageAttributeProperty();
        vaAttribute = root.getVaAttributeProperty();
        innerDirectoryAttribute = root.getInnerDirectoryAttributeProperty();

        mediumList = root.getMediumList();
        listOfAudioFiles = root.getListOfAudioFiles();
        listOfImageFiles = root.getListOfImageFiles();
        listOfOtherFiles = root.getListOfOtherFiles();
        listOfInnerDirectories = root.getListOfInnerDirectories();

        childList = root.getChildList();
    }

    // сканирование директории
    // добавление потомков
    public void lookForChildEntities() {
        parentDir = new SimpleObjectProperty<>(this, "parentDir"); //инициализируем родителя при сканировании нового кластера
        parentDir.setValue(dir);
        currentDir.setValue(dir);
        fillListsOfInnerFiles(this); //сортируем файлы в родительской папке

        out.println(getDirectoryName());
        testLists();

        addChilds();

        System.out.println("------------------");

    }

    private void addChilds() {
        childList = observableArrayList();
        LinkedList<File> childLinkedList = (LinkedList) listFilesAndDirs(parentDir.getValue(),
                new NotFileFilter(INSTANCE), DIRECTORY);
        childLinkedList.removeFirst();

        int cdNotProcessed = getCdNPropertyValue();

        for (File child : childLinkedList) {
            ItemProperties ocd = new ItemProperties(child);
            ocd.setParentDir(parentDir);
            ocd.setCurrentDir(child);
            if (getCdNPropertyValue() > 0 && ocd.hasAudioAttribute()) {
                cdNotProcessed--;
                cdN = getCdNPropertyValue() - cdNotProcessed;
            }
            ocd.setCdN(cdN);
            ocd.fillListsOfInnerFiles(ocd);
            if (ocd.listOfImageFiles.size() > 0) {
                ocd.setImageAttribute(true);
            }

            if (ocd.listOfAudioFiles.size() > 0) {
                ocd.setAudioAttribute(true);
            }
            childList.add(ocd);
        }

        removeUselessChilds();

        attachCoversDirectory();
    }

    /**
     *
     * @param fp
     */
    public void fillListsOfInnerFiles(ItemProperties fp) {
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
                Medium medium = new Medium(fp);
                medium.look();
                if (medium.getCdN() == 0 && cdN != 0) {
                    medium.setCdN(cdN);
                }
                if (medium.getArtist().equals("VA")) {
                    setVaAttribute(true);
                }
                mediumList.add(medium);
            }
        }
    }

    private boolean removeUselessChilds() {
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

        return true;
    }

    private void testLists() {
        if (!listOfAudioFiles.isEmpty()) {
            listOfAudioFiles.stream().forEach((file) -> {
                out.println(file.getFile().getAbsolutePath());
            });
        }

        if (!listOfImageFiles.isEmpty()) {
            listOfImageFiles.stream().forEach((file) -> {
                out.println(file.getAbsolutePath());
            });
        }

        if (!listOfOtherFiles.isEmpty()) {
            listOfOtherFiles.stream().forEach((file) -> {
                out.println(file.getAbsolutePath());
            });
        }
    }

    private void attachCoversDirectory() {
        if (!listOfAudioFiles.isEmpty()) {
            LinkedList<File> directories = (LinkedList) listFilesAndDirs(getListOfAudioFiles().get(0).getFile().getParentFile(),
                    new NotFileFilter(INSTANCE), DIRECTORY);
            directories.removeFirst();
            directories.stream().filter((d) -> (hasImages(d))).forEach((d) -> {
                listOfInnerDirectories.add(d);
            });
            if (!listOfInnerDirectories.isEmpty()) {
                innerDirectoryAttribute.setValue(TRUE);
            }
        }
    }

    public void refreshData(String artist, String album, String type) {
        if (childList.isEmpty() && hasAudioAttribute()) {
            for (Medium medium : mediumList) {
                medium.setAlbum(album);
                medium.setArtist(artist);
                medium.setType(type);
            }
        } else {
            if (!mediumList.isEmpty()) {
                for (Medium medium : mediumList) {
                    medium.setAlbum(album);
                    medium.setArtist(artist);
                    medium.setType(type);
                }
            } else {
                for (ItemProperties child : childList) {
                    for (Medium medium : child.getMediumList()) {
                        medium.setAlbum(album);
                        medium.setArtist(artist);
                        medium.setType(type);
                    }
                }
            }

        }
    }

    public boolean isImageFolder() {
        return this.hasImageAttribute() && !this.hasAudioAttribute();
    }

    private boolean isAudio(File file) {
        return getExtension(file.toString()).toLowerCase().equals("mp3");
    }

    private boolean isImage(File file) {
        String mimetype = new MimetypesFileTypeMap().getContentType(file);
        String type = mimetype.split("/")[0];
        return type.equals("image");
    }

    /**
     *
     * @return
     */
    public String getDirectoryName() {
        return directoryName.getValue();
    }

    public SimpleStringProperty getDirectoryNameProperty() {
        return directoryName;
    }

    /**
     *
     * @return
     */
    public boolean hasAudioAttribute() {
        return audioAttribute.getValue();
    }

    public SimpleBooleanProperty getAudioAttributeProperty() {
        return audioAttribute;
    }

    /**
     *
     * @return
     */
    public int getCdNPropertyValue() {
        return cdNAttribute.getValue();
    }

    public SimpleIntegerProperty getCdNProperty() {
        return cdNAttribute;
    }

    /**
     *
     * @return
     */
    public boolean hasImageAttribute() {
        return imageAttribute.getValue();
    }

    public SimpleBooleanProperty getImageAttributeProperty() {
        return imageAttribute;
    }

    /**
     *
     * @return
     */
    public boolean hasVaAttribute() {
        return vaAttribute.getValue();
    }

    public SimpleBooleanProperty getVaAttributeProperty() {
        return vaAttribute;
    }

    /**
     *
     * @return
     */
    public ObjectProperty<File> getParentDir() {
        return parentDir;
    }

    /**
     *
     * @param parentDir
     */
    public void setParentDir(ObjectProperty<File> parentDir) {
        this.parentDir = parentDir;
    }

    /**
     *
     * @param isAudio
     */
    public void setAudioAttribute(boolean isAudio) {
        this.audioAttribute.setValue(isAudio);
    }

    /**
     *
     * @param numOfCD
     */
    public void setNumOfCD(int numOfCD) {
        this.cdNAttribute.setValue(numOfCD);
    }

    /**
     *
     * @param isImage
     */
    public void setImageAttribute(boolean isImage) {
        this.imageAttribute.setValue(isImage);
    }

    /**
     *
     * @param isVa
     */
    public void setVaAttribute(boolean isVa) {
        this.vaAttribute.setValue(isVa);
    }

    /**
     *
     * @return
     */
    public ObservableList<ItemProperties> getChildList() {
        return childList;
    }

    /**
     *
     * @return
     */
    public ObservableList<AudioProperties> getListOfAudioFiles() {
        return listOfAudioFiles;
    }

    /**
     *
     * @param listOfAudioFiles
     */
    public void setListOfAudioFiles(ObservableList<AudioProperties> listOfAudioFiles) {
        this.listOfAudioFiles = listOfAudioFiles;
    }

    /**
     *
     * @return
     */
    public ObservableList<File> getListOfImageFiles() {
        return listOfImageFiles;
    }

    /**
     *
     * @param listOfImageFiles
     */
    public void setListOfImageFiles(ObservableList<File> listOfImageFiles) {
        this.listOfImageFiles = listOfImageFiles;
    }

    /**
     *
     * @return
     */
    public ObservableList<File> getListOfOtherFiles() {
        return listOfOtherFiles;
    }

    /**
     *
     * @param listOfOtherFiles
     */
    public void setListOfOtherFiles(ObservableList<File> listOfOtherFiles) {
        this.listOfOtherFiles = listOfOtherFiles;
    }

    /**
     *
     * @return
     */
    public ObjectProperty<File> getCurrentDir() {
        return currentDir;
    }

    /**
     *
     * @param dir
     */
    public void setCurrentDir(File dir) {
        this.currentDir.set(dir);
    }

    /**
     *
     * @return
     */
    public ObservableList<Medium> getMediumList() {
        return mediumList;
    }

    /**
     *
     * @return
     */
    public boolean hasInnerDirectoryAttribute() {
        return innerDirectoryAttribute.getValue();
    }

    public SimpleBooleanProperty getInnerDirectoryAttributeProperty() {
        return innerDirectoryAttribute;
    }

    public void setCdN(int cdN) {
        this.cdN = cdN;
    }

    public ObservableList<File> getListOfInnerDirectories() {
        return listOfInnerDirectories;
    }

    public File getDir() {
        return dir;
    }

    public int getCdN() {
        return cdN;
    }
}
