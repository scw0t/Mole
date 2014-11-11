package OutEntities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClusterModel {
    
    private BooleanProperty checked;
    private StringProperty name;
    private Entity entity;
    
    public ClusterModel(Entity entity){
        this.entity = entity;
        this.checked = new SimpleBooleanProperty(true);
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

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
    
}
