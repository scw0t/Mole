package Gears;


import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.mp3.MP3File;

public class ImageWorker {
    
    private LogOutput logOutput;

    private LinkedList<File> imageList;
    private File initFolder;
    private File parentFolder;
    private File currentFolder;

    public ImageWorker() {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
    }

    public void process() {

    }

    public void moveImagesToCoverFolder() throws IOException {
        File coverFolder = new File(currentFolder.getAbsolutePath() + "\\Covers");

        if (coverFolder.mkdir()) {
            for (File file : imageList) {
                FileUtils.moveFileToDirectory(file, coverFolder, false);
            }
        }
    }

    public void setImageList(LinkedList<File> imageList) {
        this.imageList = imageList;
    }

    public void setInitFolder(File initFolder) {
        this.initFolder = initFolder;
    }

    public void setParentFolder(File parentFolder) {
        this.parentFolder = parentFolder;
    }

    public void setCurrentFolder(File currentFolder) {
        this.currentFolder = currentFolder;
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }

}
