package Gears;

import OutEntities.ItemProperties;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class TagProcessor {

    ObservableList<ItemProperties> itemsList;
    
    public TagProcessor(ObservableList<ItemProperties> itemsList) {
        this.itemsList = itemsList;
        
        
    }
    
    class TestTask extends Task {
        
        @Override
        protected Object call() throws Exception {
            
            return null;
        }
    }
    
}
