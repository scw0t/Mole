package Gears;

import OutEntities.IncomingDirectory;
import OutEntities.Cluster;
import static View.MainGUI.initialDirectoryList;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class DirProcessor {

    private File initDir;
    private ObservableList<IncomingDirectory> parsedDirList;

    public DirProcessor() {
    }

    public DirProcessor(File initDir) {
        this.initDir = initDir;
    }

    public void init() {
        StringBuilder sb = new StringBuilder();
        parsedDirList = FXCollections.observableArrayList(initialDirectoryList);

        for (IncomingDirectory dirProperty : initialDirectoryList) {
            File dir = dirProperty.getValue();
            if (dir != null && dir.exists()) {
                removeExcessDirs(dir);
            }
        }
        
        for (IncomingDirectory dir : parsedDirList) {
            Cluster sd = new Cluster(dir.getValue());
            sd.lookUp();
        }
        

        /*System.out.println(parsedDirList.size());

        for (IncomingDirectory dir1 : parsedDirList) {
            String str2 = "";
            try {
                str2 += hasAudio(dir1.getValue()) ? "A+ " : "A- ";
                str2 += hasImages(dir1.getValue()) ? "I+ " : "I- ";
                str2 += hasInnerFolder(dir1.getValue()) ? "F+ " : "F- ";
                str2 += hasMultiCD(dir1.getValue()) ? "C+ " : "C- ";
            } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                Logger.getLogger(DirProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                str2 += dir1.getValue().getAbsolutePath();
                System.out.println(str2);
            }
        }*/
    }

    private void removeExcessDirs(File dir) {
        try {
            if ((!hasAudio(dir) && !hasMultiCD(dir)) && !hasInnerFolder(dir)) {
                for (int i = 0; i < parsedDirList.size(); i++) {
                    if (parsedDirList.get(i).getValue().getAbsolutePath().contains(dir.getAbsolutePath())) {
                        parsedDirList.remove(i);
                    }
                }
            }

            if (!hasAudio(dir) && !hasMultiCD(dir) && hasInnerFolder(dir)) {
                for (int i = 0; i < parsedDirList.size(); i++) {
                    if (parsedDirList.get(i).getValue().getAbsolutePath().equals(dir.getAbsolutePath())) {
                        parsedDirList.remove(i);
                        break;
                    }
                }
            }
            
            if (hasAudio(dir) && !hasMultiCD(dir) && hasMultiCD(dir.getParentFile())) {
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

    public static boolean hasMultiCD(File dir) {
        File[] directories = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        boolean multicd = false;
        if (directories.length > 1) {
            for (File directory : directories) {
                if (directory.getName().matches("(?i)^(cd |cd|disc|disc )\\d+")) {
                    multicd = true;
                    break;
                }
            }
        }

        return multicd;
    }

}
