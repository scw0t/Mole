import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;

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
    private boolean hasMultiCD = false;
    private boolean keyPressed = false;
    private IssuesChooser chooser;

    public DirWorker(File initFolder) {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        this.initFolder = initFolder;
        this.parentFolder = new File(initFolder.getAbsoluteFile() + "\\temp");
        initDepth = getFolderDepth(initFolder);
    }

    public void searchDirs() throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        final LinkedList<File> directories = (LinkedList) FileUtils.listFilesAndDirs(initFolder,
                new NotFileFilter(TrueFileFilter.INSTANCE),
                DirectoryFileFilter.DIRECTORY);

        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(new Task() {
            @Override
            protected Object call() throws Exception {
                for (final File dir : directories) {
                    keyPressed = false;
                    new Thread(new Task() {
                        @Override
                        public Object call() {
                            System.out.println("Doing some process");
                            try {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (dir.isDirectory()) {
                                            if (!getCurrentFolder(dir).equals("0")) {
                                                parentFolder = new File(getCurrentFolder(dir));
                                            }

                                            try {
                                                process(dir);
                                            } catch (IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException ex) {
                                                Logger.getLogger(DirWorker.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        if (getChooser() != null) {
                                            keyPressed = getChooser().getOkButton().isPressed();
                                        }
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                latch.countDown();
                            }

                            return null;
                        }
                    }).start();

                    while (!keyPressed) {
                        System.out.println("Await");
                        try {
                            latch.await();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(dir.getCanonicalFile());
                            } catch (IOException ex) {
                                Logger.getLogger(DirWorker.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            System.out.println("Done");
                        }
                    });
                }

                return null;
            }
        }).start();

        /*for (File file : directories) {
            if (file.isDirectory()) {
                if (!getCurrentFolder(file).equals("0")) {
                    parentFolder = new File(getCurrentFolder(file));
                }

                process(file);
            }
        }*/

    }

    private boolean process(File dir) throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        hasAudio = hasAudio(dir);
        hasImages = hasImages(dir);
        hasInnerFolder = hasInnerFolder(dir);
        hasMultiCD = hasMultiCD(dir);
        boolean processed = false;

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

        if (hasMultiCD) {
            processed = processAudio(dir, hasMultiCD);
        } else {
            if (hasAudio) {
                processed = processAudio(dir, hasMultiCD);
            }
        }
        return processed;
    }

    private boolean processAudio(File dir, boolean hasMultiCD) throws IOException,
            TagException,
            ReadOnlyFileException,
            InvalidAudioFrameException,
            CannotReadException {
        File[] directories = null;
        String[] filter = {"mp3"};
        if (hasMultiCD) {
            directories = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File current, String name) {
                    return new File(current, name).isDirectory();
                }
            });
        } else {
            directories = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mp3");
                }
            });
        }

        int cdCount = 0;

        HashMap<File, Integer> map = new HashMap<>();

        for (File file : directories) {
            if (hasMultiCD) {
                cdCount++;
                for (File mp3 : FileUtils.listFiles(file, filter, false)) {
                    map.put(mp3, cdCount);
                }
            } else {
                map.put(file, 1);
            }
        }

        AudioWorker audioWorker = new AudioWorker(map);
        audioWorker.setLogOutput(logOutput);
        audioWorker.setInitFolder(initFolder);
        audioWorker.setParentFolder(parentFolder);
        audioWorker.setFolderContent(dir.listFiles());
        audioWorker.setHasMultiCD(hasMultiCD);
        //keyPressed = audioWorker.isKeyPressed();
        boolean processed = audioWorker.process();
        setChooser(audioWorker.getChooser());
        return processed;
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

    private boolean hasMultiCD(File dir) {
        File[] directories = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        boolean multicd = false;
        if (directories.length > 1) {
            for (File directory : directories) {
                if (directory.getName().matches("(?i)^(cd |cd)\\d+")) {
                    multicd = true;
                }
            }
        }

        return multicd;
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

    public IssuesChooser getChooser() {
        return chooser;
    }

    public void setChooser(IssuesChooser chooser) {
        this.chooser = chooser;
    }

}
