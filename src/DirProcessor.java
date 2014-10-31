
import java.io.File;
import java.util.LinkedList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

public class DirProcessor {
    
    private File initDir;

    public DirProcessor(File initDir) {
        this.initDir = initDir;
    }
    
    public void init(){
        LinkedList<File> directories = (LinkedList) FileUtils.listFilesAndDirs(initDir, 
                new NotFileFilter(TrueFileFilter.INSTANCE), 
                DirectoryFileFilter.DIRECTORY);
        for (File dir : directories) {
            System.out.println(dir.getAbsolutePath());
        }
    }
    
}
