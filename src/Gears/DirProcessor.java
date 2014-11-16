package Gears;

import OutEntities.ClusterModel;
import OutEntities.IncomingDirectory;
import OutEntities.FileProperties;
import View.MainGUI;
import static View.MainGUI.initialDirectoryList;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    public static ObservableList<FileProperties> entityList;

    public DirProcessor() {
        entityList = FXCollections.observableArrayList();
        parsedDirList = FXCollections.observableArrayList(initialDirectoryList);
    }

    public void go() {
        for (IncomingDirectory dirProperty : initialDirectoryList) {
            File dir = dirProperty.getValue();
            if (dir != null && dir.exists()) {
                removeExcessDirs(dir);
            }
        }

        for (IncomingDirectory dir : parsedDirList) {
            FileProperties entity = new FileProperties(dir.getValue());
            entity.lookForChildEntities();
            entityList.add(entity);
        }
        
        fillTable();
    }

    private void fillTable() {
        ObservableList<ClusterModel> clusters = FXCollections.observableArrayList();
        if (entityList != null && !entityList.isEmpty()) {
            for (FileProperties entity : entityList) {
                clusters.add(new ClusterModel(entity));
            }
            MainGUI.tableView.setItems(clusters);
            MainGUI.tableView.getCheckCol().prefWidthProperty().bind(MainGUI.tableView.widthProperty().multiply(0.07));
            MainGUI.tableView.getNameCol().prefWidthProperty().bind(MainGUI.tableView.widthProperty().multiply(0.8));
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
                    //System.out.println("File: " + file.toString());
                    /*FileVisitResult res;
                     if (!FilenameUtils.getExtension(file.getFileName().toString()).equals("mp3")) {
                     System.out.println(file.toString());
                     res = FileVisitResult.SKIP_SIBLINGS;
                     } else {
                     res = FileVisitResult.CONTINUE;
                     }*/

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

    private void removeExcessDirs(File dir) {
        try {
            // фильтр директории, без аудиофайлов
            /*for (int i = 0; i < parsedDirList.size(); i++) {
             if (!hasAudio(dir)) {
                    
             } else {
             break;
             }
             }*/

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

    /*public ObservableList<Entity> getEntityList() {
     return entityList;
     }

     public void setEntityList(ObservableList<Entity> entityList) {
     this.entityList = entityList;
     }*/
}
