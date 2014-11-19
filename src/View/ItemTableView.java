package View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemTableView<ItemModel> extends TableView<ItemModel>{

    private final TableColumn<ItemModel, Boolean> checkCol;
    private final TableColumn<ItemModel, String> nameCol;
    private TextArea textArea;

    public ItemTableView() throws FileNotFoundException {
        checkCol = new TableColumn<ItemModel, Boolean>("");
        nameCol = new TableColumn<ItemModel, String>("Name");
        checkCol.setGraphic(new ImageView(new Image(new FileInputStream("eye.png"))));
        checkCol.setSortable(false);
        
        checkCol.prefWidthProperty().bind(widthProperty().multiply(0.07));
        nameCol.prefWidthProperty().bind(widthProperty().multiply(0.8));
        /*checkCol.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("pressed");
            }
        });*/

        getColumns().addAll(checkCol, nameCol);

        checkCol.setCellValueFactory(new PropertyValueFactory<ItemModel, Boolean>("checked"));
        nameCol.setCellValueFactory(new PropertyValueFactory<ItemModel, String>("name"));

        checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
        checkCol.setEditable(true);
        setEditable(true);
        setStyle("-fx-focus-color: transparent;");

    }

    public TableColumn<ItemModel, Boolean> getCheckCol() {
        return checkCol;
    }

    public TableColumn<ItemModel, String> getNameCol() {
        return nameCol;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }
}
