package View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ItemTableView<ItemModel> extends TableView<ItemModel>{

    private final TableColumn<ItemModel, Boolean> checkCol;
    private final TableColumn<ItemModel, String> nameCol;
    private final TableColumn<ItemModel, String> progressCol;

    public ItemTableView() throws FileNotFoundException {
        checkCol = new TableColumn<>("");
        nameCol = new TableColumn<>("Name");
        progressCol = new TableColumn<>("");
        checkCol.setGraphic(new ImageView(new Image(new FileInputStream("eye.png"))));
        checkCol.setSortable(false);
        progressCol.setGraphic(new ImageView(new Image(new FileInputStream("progress.png"))));
        progressCol.setSortable(false);
        
        checkCol.prefWidthProperty().bind(widthProperty().multiply(0.07));
        nameCol.prefWidthProperty().bind(widthProperty().multiply(0.86));
        progressCol.prefWidthProperty().bind(widthProperty().multiply(0.07));
        /*checkCol.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("pressed");
            }
        });*/

        getColumns().addAll(checkCol, nameCol, progressCol);

        checkCol.setCellValueFactory(new PropertyValueFactory<>("checked"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        progressCol.setCellValueFactory(new PropertyValueFactory<>("progress"));

        checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
        checkCol.setEditable(true);
        progressCol.setEditable(false);
        setEditable(true);
        setStyle("-fx-focus-color: transparent;");

    }

    public TableColumn<ItemModel, Boolean> getCheckCol() {
        return checkCol;
    }

    public TableColumn<ItemModel, String> getNameCol() {
        return nameCol;
    }

}
