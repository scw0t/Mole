
import java.io.File;

public class DirProcessor {
    
    private File initDir;

    public DirProcessor(File initDir) {
        this.initDir = initDir;
    }
    
    public void init(){
        for (File dir : MainGUI.dirList) {
            System.out.println(dir.getAbsolutePath());
        }
    }
    
}
