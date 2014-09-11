
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

public class Process {
    
    private LogOutput logOutput;

    private File initDir;

    public Process(File initDir) {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        this.initDir = initDir;

    }

    public void init() throws IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, CannotReadException {
        Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
        DirWorker dirWorker = new DirWorker(initDir);
        dirWorker.setLogOutput(logOutput);
        dirWorker.searchDirs();
    }

    public void setLogOutput(LogOutput logOutput) {
        this.logOutput = logOutput;
    }
}
