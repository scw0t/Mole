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

public class ClusterTableView<ClusterModel> extends TableView<ClusterModel>{

    private final TableColumn<ClusterModel, Boolean> checkCol;
    private final TableColumn<ClusterModel, String> nameCol;
    private TextArea textArea;

    public ClusterTableView() throws FileNotFoundException {
        checkCol = new TableColumn<ClusterModel, Boolean>("");
        nameCol = new TableColumn<ClusterModel, String>("Name");
        checkCol.setGraphic(new ImageView(new Image(new FileInputStream("eye.png"))));
        checkCol.setSortable(false);
        /*checkCol.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("pressed");
            }
        });*/

        getColumns().addAll(checkCol, nameCol);

        checkCol.setCellValueFactory(new PropertyValueFactory<ClusterModel, Boolean>("checked"));
        nameCol.setCellValueFactory(new PropertyValueFactory<ClusterModel, String>("name"));

        checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
        checkCol.setEditable(true);
        setEditable(true);
        setStyle("-fx-focus-color: transparent;");

    }

    public TableColumn<ClusterModel, Boolean> getCheckCol() {
        return checkCol;
    }

    public TableColumn<ClusterModel, String> getNameCol() {
        return nameCol;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }
}
