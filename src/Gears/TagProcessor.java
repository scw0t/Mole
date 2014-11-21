package Gears;

import OutEntities.ItemProperties;
import javafx.concurrent.Task;

public class TagProcessor {

    ItemProperties itemsProps;
    
    public TagProcessor(ItemProperties itemsProps) {
        this.itemsProps = itemsProps;
        
        
    }
    
    class TestTask extends Task {
        
        @Override
        protected Object call() throws Exception {
            
            return null;
        }
    }
    
}
