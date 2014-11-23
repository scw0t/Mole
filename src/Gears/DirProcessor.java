package Gears;

import View.ItemModel;
import OutEntities.IncomingDirectory;
import OutEntities.ItemProperties;
import View.Controller;
import static View.Controller.initialDirectoryList;
import View.TaskDialog;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public class DirProcessor {

    private ObservableList<IncomingDirectory> parsedDirList;
    public ObservableList<ItemProperties> itemPropertiesList;
    public TaskDialog dialog;

    public DirProcessor() {
        itemPropertiesList = FXCollections.observableArrayList();
        parsedDirList = FXCollections.observableArrayList(initialDirectoryList);
    }

    public void go() {
        Platform.runLater(() -> {
            ProgressTask task = new ProgressTask();
            dialog = new TaskDialog();
            dialog.show();
            dialog.getPathLabel1().textProperty().bind(task.messageProperty());
            dialog.getProgressBar().progressProperty().bind(task.progressProperty());
            Controller.tableView.itemsProperty().bind(task.valueProperty());
            Controller.tableView.getSelectionModel().clearSelection();
            //Controller.tableView.getSelectionModel().select(0);
            
            new Thread(task).start();
        });
    }

    private void removeExcessDirs(File dir) {
        try {
            if ((!hasAudio(dir) && numberOfCD(dir) == 0) && !hasInnerFolder(dir)) {
                for (int i = 0; i < parsedDirList.size(); i++) {
                    if (parsedDirList.get(i).getValue().getAbsolutePath().contains(dir.getAbsolutePath())) {
                        parsedDirList.remove(i);
                    }
                }
            }

            if (!hasAudio(dir) && numberOfCD(dir) == 0 && hasInnerFolder(dir)) {
                for (int i = 0; i < parsedDirList.size(); i++) {
                    if (parsedDirList.get(i).getValue().getAbsolutePath().equals(dir.getAbsolutePath())) {
                        parsedDirList.remove(i);
                        break;
                    }
                }
            }

            if (hasAudio(dir) && numberOfCD(dir) == 0 && numberOfCD(dir.getParentFile()) > 0) {
                for (int i = 0; i < parsedDirList.size(); i++) {
                    if (parsedDirList.get(i).getValue().getAbsolutePath().equals(dir.getAbsolutePath())) {
                        parsedDirList.remove(i);
                        //break;
                    }
                }
            }
        } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
            Logger.getLogger(DirProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean hasAudio(File dir) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        String[] filter = {"mp3", "MP3"};
        LinkedList<File> mp3List = (LinkedList) FileUtils.listFiles(dir, filter, false);
        return mp3List.size() != 0;
    }

    public static boolean hasImages(File dir) {
        String[] filter = {"jpg", "jpeg", "gif", "png"};
        LinkedList<File> imageList = (LinkedList) FileUtils.listFiles(dir, filter, false);
        return imageList.size() != 0;
    }

    public static boolean hasInnerFolder(File dir) {
        LinkedList<File> folderList = (LinkedList) FileUtils.listFilesAndDirs(dir,
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        return folderList.size() > 1;
    }

    public static int numberOfCD(File dir) {
        int num = 0;
        File[] directories = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        if (directories.length > 1) {
            for (File directory : directories) {
                if (directory.getName().matches("(?i)^(cd |cd|disc|disc )\\d+")) {
                    num++;
                }
            }
        }
        return num;
    }

    class ProgressTask extends Task<ObservableList<ItemModel>> {

        @Override
        protected ObservableList<ItemModel> call() throws Exception {
            for (int i = 0; i < initialDirectoryList.size(); i++) {
                File dir = initialDirectoryList.get(i).getValue();
                if (dir != null && dir.exists()) {
                    removeExcessDirs(dir);
                }
            }

            for (int i = 0; i < parsedDirList.size(); i++) {
                ItemProperties itemProps = new ItemProperties(parsedDirList.get(i).getValue());
                updateMessage(itemProps.getDirectoryName());
                itemProps.lookForChildEntities();
                itemPropertiesList.add(itemProps);
                updateProgress(i + 1, parsedDirList.size());
            }

            ObservableList<ItemModel> clusters = FXCollections.observableArrayList();

            if (itemPropertiesList != null && !itemPropertiesList.isEmpty()) {
                for (ItemProperties itemProps : itemPropertiesList) {
                    clusters.add(new ItemModel(itemProps));
                }
            }

            return clusters;
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            if (dialog.isShowing()) {
                dialog.close();
            }
        }

    }

    private void walk(File parent) {
        try {
            Path startPath = parent.toPath();
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                        BasicFileAttributes attrs) {
                    FileVisitResult res = FileVisitResult.CONTINUE;
                    File[] list = dir.toFile().listFiles();
                    System.out.println("Curr dir: " + dir.toString());
                    for (File f : list) {
                        if (FilenameUtils.getExtension(f.getName()).toLowerCase().equals("mp3")) {
                            System.out.println(f.getAbsolutePath());
                            res = FileVisitResult.CONTINUE;
                        } else {
                            res = FileVisitResult.CONTINUE;
                        }
                    }
                    //System.out.println("Dir: " + dir.toString());
                    return res;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
