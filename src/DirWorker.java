
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public class DirWorker {
    
    private LogOutput logOutput;

    private File initFolder;
    private File parentFolder;
    private int initDepth = 0;

    private boolean hasAudio = false;
    private boolean hasImages = false;
    private boolean hasInnerFolder = false;

    public DirWorker(File initFolder) {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        this.initFolder = initFolder;
        this.parentFolder = new File(initFolder.getAbsoluteFile() + "\\temp");
        initDepth = getFolderDepth(initFolder);
    }

    public void searchDirs() throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        LinkedList<File> directories = (LinkedList) FileUtils.listFilesAndDirs(initFolder,
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);

        for (Iterator iterator = directories.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if (file.isDirectory()) {
                if (!getCurrentFolder(file).equals("0")) {
                    parentFolder = new File(getCurrentFolder(file));
                }

                process(file);
            }

			//System.out.println();
			/*System.out.println(file.getAbsolutePath() 
             + " ##### " + hasAudio(file) 
             + " @@@@@ " + hasImages(file)
             + " ^^^^^ " + hasInnerFolder(file));*/
            //hasAudio(file);
        }

    }

    private void process(File dir) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        hasAudio = hasAudio(dir);
        hasImages = hasImages(dir);
        hasInnerFolder = hasInnerFolder(dir);

        if (hasImages) {

            String[] filter = {"jpg", "jpeg", "gif", "png"};
            LinkedList<File> imageList = (LinkedList) FileUtils.listFiles(dir, filter, false);

            ImageWorker imageWorker = new ImageWorker();
            imageWorker.setLogOutput(logOutput);
            imageWorker.setImageList(imageList);
            imageWorker.setParentFolder(parentFolder);
            imageWorker.setCurrentFolder(dir);
            imageWorker.setInitFolder(initFolder);

            if (hasAudio && hasInnerFolder) {
                if (imageList.size() > 1) {
                    imageWorker.moveImagesToCoverFolder();
                } else {
                    imageList.getFirst().renameTo(new File(
                            dir.getAbsolutePath() + "\\folder"
                            + FilenameUtils.EXTENSION_SEPARATOR
                            + "jpg"));
                }
            }

            if (hasAudio && !hasInnerFolder) {
                if (imageList.size() > 1) {
                    imageWorker.moveImagesToCoverFolder();
                } else {
                    imageList.getFirst().renameTo(new File(
                            dir.getAbsolutePath() + "\\folder"
                            + FilenameUtils.EXTENSION_SEPARATOR
                            + "jpg"));
                }
            }

            if (!hasAudio && !hasInnerFolder) {
                if (!dir.getName().equals("Covers")) {
                    //File file = new File(dir.getParentFile() + "\\Covers");
                    //System.out.println(file.getAbsolutePath());
                    dir.renameTo(new File(dir.getParentFile() + "\\Covers"));
                }
            }

            if (!hasAudio && hasInnerFolder) {
                if (imageList.size() > 1) {
                    imageWorker.moveImagesToCoverFolder();
                } else {
                    imageList.getFirst().renameTo(new File(
                            dir.getAbsolutePath() + "\\folder"
                            + FilenameUtils.EXTENSION_SEPARATOR
                            + "jpg"));
                }
            }
        }

        if (hasAudio) {
            String[] filter = {"mp3"};
            LinkedList<File> mp3List = (LinkedList) FileUtils.listFiles(dir, filter, false);
            AudioWorker audioWorker = new AudioWorker(mp3List);
            audioWorker.setLogOutput(logOutput);
            audioWorker.setInitFolder(initFolder);
            audioWorker.setParentFolder(parentFolder);
            audioWorker.process();
        }

    }

    private boolean hasAudio(File dir) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        String[] filter = {"mp3"};
        /*dir.getAbsoluteFile();
         if (dir.isDirectory()) {
         System.out.println("dir");
         } else {
         System.out.println("not dir");
         }*/
        LinkedList<File> mp3List = (LinkedList) FileUtils.listFiles(dir, filter, false);
        if (mp3List.size() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasImages(File dir) {
        String[] filter = {"jpg", "jpeg", "gif", "png"};
        LinkedList<File> imageList = (LinkedList) FileUtils.listFiles(dir, filter, false);
        if (imageList.size() != 0) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasInnerFolder(File dir) {
        LinkedList<File> folderList = (LinkedList) FileUtils.listFilesAndDirs(dir,
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        if (folderList.size() > 1) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasCDFolder(File dir) {
        LinkedList<File> folderList = (LinkedList) FileUtils.listFilesAndDirs(dir,
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);
        if (folderList.size() > 1) {
            return true;
        } else {
            return false;
        }
    }

    private int getFolderDepth(File dir) {
        return dir.getAbsolutePath().split("\\\\").length;
    }

    private String getCurrentFolder(File dir) {
        if (getFolderDepth(dir) == initDepth + 1) {
            return dir.getAbsolutePath();
        } else {
            return "0";
        }
    }

    private void printList(File[] list) {
        for (File f : list) {
            System.out.println(f.getName());
        }
        System.out.println("---------------------------------");
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }
}
