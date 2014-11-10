package View;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class ClusterTableView<Cluster> extends TableView {

        public ClusterTableView() {
            
            TableColumn<Cluster, String> clusterNameCol = new TableColumn<>("Cluster");
            clusterNameCol.setCellValueFactory(new PropertyValueFactory<>("id")); 
            //clusterNameCol.setCellValueFactory(new ClusterValueFactory(initDirPath));
            clusterNameCol.setCellFactory(new ClusterCellFactory());
            this.getColumns().add(clusterNameCol);
        }
        
        class ClusterValueFactory extends PropertyValueFactory<Cluster, String>{

            public ClusterValueFactory(String property) {
                super(property);
            }
            
        }

        class ClusterCellFactory implements Callback<TableColumn<Cluster, String>, TableCell<Cluster, String>> {

            @Override
            public TableCell<Cluster, String> call(TableColumn<Cluster, String> param) {
                return new ClusterCell();
            }

            class ClusterCell extends TableCell<Cluster, String> {
                
                public ClusterCell() {
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty); 
                    if (item == null || empty) {
                        setText("12");
                    } else{
                        setText("145");
                    }
                    
                }

            }

        }

    }
