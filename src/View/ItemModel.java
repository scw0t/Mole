package View;

import OutEntities.ItemProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ItemModel {
    
    private BooleanProperty checked;
    private StringProperty name;
    private StringProperty progress;
    private ItemProperties itemProperty;
    
    public ItemModel(ItemProperties entity){
        this.itemProperty = entity;
        this.checked = new SimpleBooleanProperty(true);
        this.progress = new SimpleStringProperty("done");
        this.name = new SimpleStringProperty(entity.getDirectoryName());
    }

    public String getName(){
        return name.get();
    }
    
    public void setName(String name){
        this.name.set(name);
    }
    
    public StringProperty nameProperty(){
        return name;
    }
    
    public Boolean isChecked(){
        return checked.get();
    }
    
    public void setCheck(boolean checked){
        this.checked.set(checked);
    }
    
    public BooleanProperty checkedProperty(){
        return checked;
    }

    public ItemProperties getItemProperty() {
        return itemProperty;
    }

    public void setItemProperty(ItemProperties itemProperty) {
        this.itemProperty = itemProperty;
    }

    public String getProgress() {
        return progress.getValue();
    }

    public void setProgress(String progress) {
        this.progress.setValue(progress);
    }
    
}
