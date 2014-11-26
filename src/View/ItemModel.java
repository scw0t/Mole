package View;

import OutEntities.ItemProperties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ItemModel {
    
    private final BooleanProperty checked;
    private final StringProperty name;
    private final StringProperty progress;
    private ItemProperties itemProperty;
    
    /**
     *
     * @param entity
     */
    public ItemModel(ItemProperties entity){
        this.itemProperty = entity;
        this.checked = new SimpleBooleanProperty(true);
        this.progress = new SimpleStringProperty("done");
        this.name = new SimpleStringProperty(entity.getDirectoryName());
    }

    /**
     *
     * @return
     */
    public String getName(){
        return name.get();
    }
    
    /**
     *
     * @param name
     */
    public void setName(String name){
        this.name.set(name);
    }
    
    /**
     *
     * @return
     */
    public StringProperty nameProperty(){
        return name;
    }
    
    /**
     *
     * @return
     */
    public Boolean isChecked(){
        return checked.get();
    }
    
    /**
     *
     * @param checked
     */
    public void setCheck(boolean checked){
        this.checked.set(checked);
    }
    
    /**
     *
     * @return
     */
    public BooleanProperty checkedProperty(){
        return checked;
    }

    /**
     *
     * @return
     */
    public ItemProperties getItemProperty() {
        return itemProperty;
    }

    /**
     *
     * @param itemProperty
     */
    public void setItemProperty(ItemProperties itemProperty) {
        this.itemProperty = itemProperty;
    }

    /**
     *
     * @return
     */
    public String getProgress() {
        return progress.getValue();
    }

    /**
     *
     * @param progress
     */
    public void setProgress(String progress) {
        this.progress.setValue(progress);
    }
}
